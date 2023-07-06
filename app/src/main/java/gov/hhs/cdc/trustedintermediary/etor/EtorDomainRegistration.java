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
import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderController;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderConverter;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderResponse;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderSender;
import gov.hhs.cdc.trustedintermediary.etor.orders.SendOrderUseCase;
import gov.hhs.cdc.trustedintermediary.etor.orders.UnableToSendOrderException;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiOrderConverter;
import gov.hhs.cdc.trustedintermediary.external.localfile.LocalFileOrderSender;
import gov.hhs.cdc.trustedintermediary.external.reportstream.ReportStreamOrderSender;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
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
    @Inject OrderController orderController;
    @Inject ConvertAndSendDemographicsUsecase convertAndSendDemographicsUsecase;
    @Inject SendOrderUseCase sendOrderUseCase;
    @Inject Logger logger;
    @Inject DomainResponseHelper domainResponseHelper;

    private final Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> endpoints =
            Map.of(
                    new HttpEndpoint("POST", DEMOGRAPHICS_API_ENDPOINT, true),
                            this::handleDemographics,
                    new HttpEndpoint("POST", ORDERS_API_ENDPOINT, true), this::handleOrders);

    @Override
    public Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
        ApplicationContext.register(
                PatientDemographicsController.class, PatientDemographicsController.getInstance());
        ApplicationContext.register(
                ConvertAndSendDemographicsUsecase.class,
                ConvertAndSendDemographicsUsecase.getInstance());
        ApplicationContext.register(OrderConverter.class, HapiOrderConverter.getInstance());
        ApplicationContext.register(OrderController.class, OrderController.getInstance());
        ApplicationContext.register(SendOrderUseCase.class, SendOrderUseCase.getInstance());

        if (ApplicationContext.getEnvironment().equalsIgnoreCase("local")) {
            ApplicationContext.register(OrderSender.class, LocalFileOrderSender.getInstance());
        } else {
            ApplicationContext.register(OrderSender.class, ReportStreamOrderSender.getInstance());
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
        Demographics<?> demographics;

        try {
            demographics = patientDemographicsController.parseDemographics(request);
            convertAndSendDemographicsUsecase.convertAndSend(demographics);
        } catch (UnableToSendOrderException e) {
            logger.logError("Unable to convert and send demographics", e);
            return domainResponseHelper.constructErrorResponse(400, e);
        }

        PatientDemographicsResponse patientDemographicsResponse =
                new PatientDemographicsResponse(demographics);

        return domainResponseHelper.constructOkResponse(patientDemographicsResponse);
    }

    DomainResponse handleOrders(DomainRequest request) {
        Order<?> orders;

        try {
            orders = orderController.parseOrders(request);
            sendOrderUseCase.send(orders);
        } catch (UnableToSendOrderException e) {
            logger.logError("Unable to send lab order", e);
            return domainResponseHelper.constructErrorResponse(400, e);
        }

        OrderResponse orderResponse = new OrderResponse(orders);
        return domainResponseHelper.constructOkResponse(orderResponse);
    }
}
