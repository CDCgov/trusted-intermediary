package gov.hhs.cdc.trustedintermediary.etor;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainConnector;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint;
import java.util.Map;
import java.util.function.Function;

/**
 * The domain connector for the ETOR domain. It connects it with the larger trusted intermediary.
 */
public class DomainRegistration implements DomainConnector {

    @Override
    public Map<HttpEndpoint, Function<DomainRequest, DomainResponse>> domainRegistration() {
        return Map.of(new HttpEndpoint("POST", "/v1/etor/order"), this::handleOrder);
    }

    DomainResponse handleOrder(DomainRequest request) {
        var response = new DomainResponse(200);

        response.setBody("DogCow requsted a lab order");

        return response;
    }
}
