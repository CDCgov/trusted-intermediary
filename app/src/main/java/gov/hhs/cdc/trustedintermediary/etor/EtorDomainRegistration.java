package gov.hhs.cdc.trustedintermediary.etor;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint;
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

    private final Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> endpoints =
            Map.of(new HttpEndpoint("POST", "/v1/etor/demographics"), this::handleOrder);

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
    public String openApiSpecification() {
        try (InputStream openApiStream =
                getClass().getClassLoader().getResourceAsStream("openapi_etor.yaml")) {
            return new String(openApiStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    DomainResponse handleOrder(DomainRequest request) {

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
}
