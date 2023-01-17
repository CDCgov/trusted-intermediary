package gov.hhs.cdc.trustedintermediary.etor;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint;
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsController;
import gov.hhs.cdc.trustedintermediary.etor.demographics.PatientDemographicsResponse;
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
    @Inject Logger logger;

    @Override
    public Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
        ApplicationContext.register(
                PatientDemographicsController.class, PatientDemographicsController.getInstance());
        return Map.of(new HttpEndpoint("POST", "/v1/etor/order"), this::handleOrder);
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

        logger.logInfo("Parsing request...");
        var order = patientDemographicsController.parseDemographics(request);

        PatientDemographicsResponse patientDemographicsResponse =
                new PatientDemographicsResponse(order);

        logger.logInfo("Constructing response...");
        return patientDemographicsController.constructResponse(patientDemographicsResponse);
    }
}
