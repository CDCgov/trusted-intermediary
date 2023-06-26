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
 * The domain connector for the ETOR domain. It connects it with the larger trusted intermediary. It
 * houses the request processing logic for the demographics and orders endpoints.
 */
public class EtorDomainRegistration implements DomainConnector {

    static final String DEMOGRAPHICS_API_ENDPOINT = "/v1/etor/demographics";
    static final String ORDERS_API_ENDPOINT = "/v1/etor/orders";

    @Inject PatientDemographicsController patientDemographicsController;
    @Inject ConvertAndSendLabOrderUsecase convertAndSendLabOrderUsecase;
    @Inject Logger logger;
    @Inject AuthRequestValidator authValidator;

    private final Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> endpoints =
            Map.of(
                    new HttpEndpoint("POST", DEMOGRAPHICS_API_ENDPOINT), this::handleDemographics,
                    new HttpEndpoint("POST", ORDERS_API_ENDPOINT), this::handleOrders);

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

    DomainResponse handleOrders(DomainRequest request) {
        //  Validate token
        try {
            if (!authValidator.isValidAuthenticatedRequest(request)) {
                var errorMessage = "The request failed the authentication check";
                logger.logError(errorMessage);

                // ordersController.constructResponse(401, errorMessage)
                return new DomainResponse(401);
            }
        } catch (SecretRetrievalException | IllegalArgumentException e) {
            logger.logFatal("Unable to validate whether the request is authenticated", e);

            // return ordersController.constructResponse(500, e)
            return new DomainResponse(500);
        }

        //  var orders = ordersController.ParseOrders(request)
        //  convertAndSendLabOrderUseCase.covertAndSend(orders)
        //  return ordersController.constructResponse(OrdersResponse)
        return new DomainResponse(200);
    }
}
