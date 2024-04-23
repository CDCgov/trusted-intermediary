package gov.hhs.cdc.trustedintermediary.etor;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponseHelper;
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint;
import gov.hhs.cdc.trustedintermediary.domainconnector.UnableToReadOpenApiSpecificationException;
import gov.hhs.cdc.trustedintermediary.etor.demographics.ConvertAndSendDemographicsUsecase;
import gov.hhs.cdc.trustedintermediary.etor.demographics.Demographics;
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsController;
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsResponse;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkStorage;
import gov.hhs.cdc.trustedintermediary.etor.messages.MessageRequestHandler;
import gov.hhs.cdc.trustedintermediary.etor.messages.SendMessageHelper;
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataConverter;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataOrchestrator;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.etor.operationoutcomes.FhirMetadata;
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderController;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderConverter;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderResponse;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender;
import gov.hhs.cdc.trustedintermediary.etor.orders.SendOrderUseCase;
import gov.hhs.cdc.trustedintermediary.etor.results.Result;
import gov.hhs.cdc.trustedintermediary.etor.results.ResultController;
import gov.hhs.cdc.trustedintermediary.etor.results.ResultConverter;
import gov.hhs.cdc.trustedintermediary.etor.results.ResultResponse;
import gov.hhs.cdc.trustedintermediary.etor.results.ResultSender;
import gov.hhs.cdc.trustedintermediary.etor.results.SendResultUseCase;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleEngine;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.RuleLoader;
import gov.hhs.cdc.trustedintermediary.external.database.DatabaseMessageLinkStorage;
import gov.hhs.cdc.trustedintermediary.external.database.DatabasePartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.external.database.DbDao;
import gov.hhs.cdc.trustedintermediary.external.database.PostgresDao;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiMessageConverterHelper;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiMessageHelper;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiOrderConverter;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiPartnerMetadataConverter;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiResultConverter;
import gov.hhs.cdc.trustedintermediary.external.localfile.FileMessageLinkStorage;
import gov.hhs.cdc.trustedintermediary.external.localfile.FilePartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.external.localfile.MockRSEndpointClient;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamEndpointClient;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamOrderSender;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamResultSender;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamSenderHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;

/**
 * The domain connector for the ETOR domain. It connects it with the larger trusted intermediary. It
 * houses the request processing logic for the demographics and orders endpoints.
 */
public class EtorDomainRegistration implements DomainConnector {

    static final String DEMOGRAPHICS_API_ENDPOINT = "/v1/etor/demographics";
    static final String ORDERS_API_ENDPOINT = "/v1/etor/orders";
    static final String METADATA_API_ENDPOINT = "/v1/etor/metadata/{id}";
    static final String RESULTS_API_ENDPOINT = "/v1/etor/results";

    static final String CONSOLIDATED_SUMMARY_API_ENDPOINT = "/v1/etor/metadata/summary/{sender}";

    @Inject PatientDemographicsController patientDemographicsController;
    @Inject OrderController orderController;
    @Inject ConvertAndSendDemographicsUsecase convertAndSendDemographicsUsecase;
    @Inject SendOrderUseCase sendOrderUseCase;

    @Inject ResultController resultController;

    @Inject SendResultUseCase sendResultUseCase;

    @Inject Logger logger;
    @Inject DomainResponseHelper domainResponseHelper;
    @Inject PartnerMetadataOrchestrator partnerMetadataOrchestrator;
    @Inject PartnerMetadataConverter partnerMetadataConverter;

    @Inject HapiFhir fhir;

    private final Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> endpoints =
            Map.of(
                    new HttpEndpoint("POST", DEMOGRAPHICS_API_ENDPOINT, true),
                            this::handleDemographics,
                    new HttpEndpoint("POST", ORDERS_API_ENDPOINT, true), this::handleOrders,
                    new HttpEndpoint("GET", METADATA_API_ENDPOINT, true), this::handleMetadata,
                    new HttpEndpoint("POST", RESULTS_API_ENDPOINT, true), this::handleResults,
                    new HttpEndpoint("GET", CONSOLIDATED_SUMMARY_API_ENDPOINT, true),
                            this::handleConsolidatedSummary);

    @Override
    public Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
        // Demographics
        ApplicationContext.register(
                PatientDemographicsController.class, PatientDemographicsController.getInstance());
        ApplicationContext.register(
                ConvertAndSendDemographicsUsecase.class,
                ConvertAndSendDemographicsUsecase.getInstance());
        // Orders
        ApplicationContext.register(OrderConverter.class, HapiOrderConverter.getInstance());
        ApplicationContext.register(OrderController.class, OrderController.getInstance());
        ApplicationContext.register(SendOrderUseCase.class, SendOrderUseCase.getInstance());
        ApplicationContext.register(OrderSender.class, ReportStreamOrderSender.getInstance());
        // Results
        ApplicationContext.register(ResultConverter.class, HapiResultConverter.getInstance());
        ApplicationContext.register(ResultController.class, ResultController.getInstance());
        ApplicationContext.register(SendResultUseCase.class, SendResultUseCase.getInstance());
        ApplicationContext.register(ResultSender.class, ReportStreamResultSender.getInstance());
        // Message
        ApplicationContext.register(
                HapiMessageConverterHelper.class, HapiMessageConverterHelper.getInstance());
        ApplicationContext.register(
                ReportStreamSenderHelper.class, ReportStreamSenderHelper.getInstance());
        ApplicationContext.register(HapiMessageHelper.class, HapiMessageHelper.getInstance());
        // Metadata
        ApplicationContext.register(
                PartnerMetadataOrchestrator.class, PartnerMetadataOrchestrator.getInstance());
        ApplicationContext.register(
                PartnerMetadataConverter.class, HapiPartnerMetadataConverter.getInstance());
        // Validation rules
        ApplicationContext.register(RuleLoader.class, RuleLoader.getInstance());
        ApplicationContext.register(RuleEngine.class, RuleEngine.getInstance());

        ApplicationContext.register(SendMessageHelper.class, SendMessageHelper.getInstance());

        if (ApplicationContext.getProperty("DB_URL") != null) {
            ApplicationContext.register(DbDao.class, PostgresDao.getInstance());
            ApplicationContext.register(
                    PartnerMetadataStorage.class, DatabasePartnerMetadataStorage.getInstance());
            ApplicationContext.register(
                    MessageLinkStorage.class, DatabaseMessageLinkStorage.getInstance());
        } else if (ApplicationContext.getEnvironment().equalsIgnoreCase("local")) {
            ApplicationContext.register(
                    PartnerMetadataStorage.class, FilePartnerMetadataStorage.getInstance());
            ApplicationContext.register(
                    MessageLinkStorage.class, FileMessageLinkStorage.getInstance());
        }
        if (ApplicationContext.getEnvironment().equalsIgnoreCase("local")) {
            ApplicationContext.register(RSEndpointClient.class, MockRSEndpointClient.getInstance());
        } else {
            ApplicationContext.register(
                    RSEndpointClient.class, ReportStreamEndpointClient.getInstance());
        }

        return endpoints;
    }

    @Override
    public String openApiSpecification() throws UnableToReadOpenApiSpecificationException {
        String fileName = "openapi_etor.yaml";
        try {
            return openApiStream(fileName);
        } catch (IOException e) {
            throw new UnableToReadOpenApiSpecificationException(
                    "Failed to open OpenAPI specification for " + fileName, e);
        }
    }

    public String openApiStream(String fileName) throws IOException {
        InputStream openApiStream = getClass().getClassLoader().getResourceAsStream(fileName);
        return new String(openApiStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    DomainResponse handleDemographics(DomainRequest request) {
        Demographics<?> demographics;

        try {
            demographics = patientDemographicsController.parseDemographics(request);
            convertAndSendDemographicsUsecase.convertAndSend(demographics);
        } catch (FhirParseException e) {
            logger.logError("Unable to parse demographics request", e);
            return domainResponseHelper.constructErrorResponse(400, e);
        } catch (UnableToSendMessageException e) {
            logger.logError("Unable to send demographics", e);
            return domainResponseHelper.constructErrorResponse(400, e);
        }

        PatientDemographicsResponse patientDemographicsResponse =
                new PatientDemographicsResponse(demographics);

        return domainResponseHelper.constructOkResponse(patientDemographicsResponse);
    }

    DomainResponse handleOrders(DomainRequest request) {
        return handleMessageRequest(
                request,
                receivedSubmissionId -> {
                    Order<?> orders = orderController.parseOrders(request);
                    sendOrderUseCase.convertAndSend(orders, receivedSubmissionId);
                    return domainResponseHelper.constructOkResponse(new OrderResponse(orders));
                },
                "order",
                true);
    }

    DomainResponse handleResults(DomainRequest request) {
        return handleMessageRequest(
                request,
                receivedSubmissionId -> {
                    Result<?> results = resultController.parseResults(request);
                    sendResultUseCase.convertAndSend(results, receivedSubmissionId);
                    return domainResponseHelper.constructOkResponse(new ResultResponse(results));
                },
                "results",
                false);
    }

    DomainResponse handleMetadata(DomainRequest request) {
        try {
            String metadataId = request.getPathParams().get("id");
            Optional<PartnerMetadata> metadata =
                    partnerMetadataOrchestrator.getMetadata(metadataId);

            if (metadata.isEmpty()) {
                return domainResponseHelper.constructErrorResponse(
                        404, "Metadata not found for ID: " + metadataId);
            }

            Set<String> messageIdsToLink =
                    partnerMetadataOrchestrator.findMessagesIdsToLink(metadataId);

            // Remove the metadataId from the set of messageIdsToLink to avoid showing it in the
            // partner metadata's linked ids
            messageIdsToLink.remove(metadataId);

            FhirMetadata<?> responseObject =
                    partnerMetadataConverter.extractPublicMetadataToOperationOutcome(
                            metadata.get(), metadataId, messageIdsToLink);

            return domainResponseHelper.constructOkResponseFromString(
                    fhir.encodeResourceToJson(responseObject.getUnderlyingOutcome()));
        } catch (PartnerMetadataException e) {
            String errorMessage = "Unable to retrieve requested metadata";
            logger.logError(errorMessage, e);
            return domainResponseHelper.constructErrorResponse(500, errorMessage);
        }
    }

    DomainResponse handleConsolidatedSummary(DomainRequest request) {

        Map<String, Map<String, Object>> metadata;
        try {
            String senderName = request.getPathParams().get("sender");

            metadata = partnerMetadataOrchestrator.getConsolidatedMetadata(senderName);

        } catch (Exception e) {
            var errorString = "Unable to retrieve consolidated orders";
            logger.logError(errorString, e);
            return domainResponseHelper.constructErrorResponse(500, errorString);
        }

        return domainResponseHelper.constructOkResponse(metadata);
    }

    protected DomainResponse handleMessageRequest(
            DomainRequest request,
            MessageRequestHandler<DomainResponse> requestHandler,
            String messageType,
            boolean doUpdatePartnerMetadata) {
        String receivedSubmissionId = getReceivedSubmissionId(request);
        boolean markMetadataAsFailed = false;
        String errorMessage = "";

        try {
            return requestHandler.handle(receivedSubmissionId);
        } catch (FhirParseException e) {
            errorMessage = "Unable to parse " + messageType + " request";
            logger.logError(errorMessage, e);
            markMetadataAsFailed = true;
            return domainResponseHelper.constructErrorResponse(400, e);
        } catch (UnableToSendMessageException e) {
            errorMessage = "Unable to send " + messageType;
            logger.logError(errorMessage, e);
            markMetadataAsFailed = true;
            return domainResponseHelper.constructErrorResponse(400, e);
        } finally {
            if (doUpdatePartnerMetadata && markMetadataAsFailed) {
                try {
                    partnerMetadataOrchestrator.setMetadataStatusToFailed(
                            receivedSubmissionId, errorMessage);
                } catch (PartnerMetadataException innerE) {
                    logger.logError("Unable to update metadata status", innerE);
                }
            }
        }
    }

    protected String getReceivedSubmissionId(DomainRequest request) {

        String receivedSubmissionId = request.getHeaders().get("recordid");
        if (receivedSubmissionId == null || receivedSubmissionId.isEmpty()) {
            logger.logError("Missing required header or empty: RecordId");
            return null;
        }
        return receivedSubmissionId;
    }
}
