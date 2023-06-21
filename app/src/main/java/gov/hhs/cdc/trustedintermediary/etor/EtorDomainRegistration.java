package gov.hhs.cdc.trustedintermediary.etor;

import gov.hhs.cdc.trustedintermediary.auth.AuthRequestValidator;
import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint;
import gov.hhs.cdc.trustedintermediary.domainconnector.UnableToReadOpenApiSpecificationException;
import gov.hhs.cdc.trustedintermediary.etor.demographics.ConvertAndSendLabOrderUsecase;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderConverter;
import gov.hhs.cdc.trustedintermediary.etor.demographics.LabOrderSender;
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsController;
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsResponse;
import gov.hhs.cdc.trustedintermediary.etor.demographics.UnableToSendLabOrderException;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiLabOrderConverter;
import gov.hhs.cdc.trustedintermediary.external.localfile.LocalFileLabOrderSender;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamLabOrderSender;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;
import javax.inject.Inject;

/**
 * The domain connector for the ETOR domain. It connects it with the larger trusted intermediary.
 */
public class EtorDomainRegistration implements DomainConnector {

    @Inject PatientDemographicsController patientDemographicsController;
    @Inject ConvertAndSendLabOrderUsecase convertAndSendLabOrderUsecase;
    @Inject Logger logger;
    @Inject AuthRequestValidator authValidator;

    private final Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> endpoints =
            Map.of(
                    new HttpEndpoint("POST", "/v1/etor/demographics"), this::handleDemographics,
                    new HttpEndpoint("POST", "/v1/etor/orders"), this::handleOrders);

    @Override
    public Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
        ApplicationContext.register(
                PatientDemographicsController.class, PatientDemographicsController.getInstance());
        ApplicationContext.register(
                ConvertAndSendLabOrderUsecase.class, ConvertAndSendLabOrderUsecase.getInstance());
        ApplicationContext.register(LabOrderConverter.class, HapiLabOrderConverter.getInstance());

        if (ApplicationContext.getEnvironment().equalsIgnoreCase("local")) {
            ApplicationContext.register(
                    LabOrderSender.class, LocalFileLabOrderSender.getInstance());
        } else {
            ApplicationContext.register(
                    LabOrderSender.class, ReportStreamLabOrderSender.getInstance());
        }

        return endpoints;
    }

    @Override
    public String openApiSpecification() throws UnableToReadOpenApiSpecificationException {
        String fileName = "openapi_etor.yaml";
        try (InputStream openApiStream =
                getClass().getClassLoader().getResourceAsStream(fileName)) {
            return new String(openApiStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UnableToReadOpenApiSpecificationException(
                    "Failed to open OpenAPI specification for " + fileName, e);
        }
    }

    DomainResponse handleDemographics(DomainRequest request) {

        // Validate token
        try {
            if (!authValidator.isValidAuthenticatedRequest(request)) {
                var errorMessage = "The request failed the authentication check";
                logger.logError(errorMessage);
                return patientDemographicsController.constructResponse(401, errorMessage);
            }
        } catch (SecretRetrievalException | IllegalArgumentException e) {
            logger.logFatal("Unable to validate whether the request is authenticated", e);
            return patientDemographicsController.constructResponse(500, e);
        }

        var demographics = patientDemographicsController.parseDemographics(request);

        try {
            convertAndSendLabOrderUsecase.convertAndSend(demographics);
        } catch (UnableToSendLabOrderException e) {
            logger.logError("Unable to send lab order", e);
            return patientDemographicsController.constructResponse(400, e);
        }

        PatientDemographicsResponse patientDemographicsResponse =
                new PatientDemographicsResponse(demographics);

        return patientDemographicsController.constructResponse(patientDemographicsResponse);
    }

    private DomainResponse handleOrders(DomainRequest domainRequest) {
        // Validate token

        // var orders = OrdersController.ParseOrders(request);

        // convertAndSendLabOrderUseCase.covertAndSend(order);

        // return OrderController.constructResponse(OrdersResponse);
        return new DomainResponse(200);
    }
}
