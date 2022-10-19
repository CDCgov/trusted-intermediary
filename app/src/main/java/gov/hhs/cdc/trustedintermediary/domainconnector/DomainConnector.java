package gov.hhs.cdc.trustedintermediary.domainconnector;

import java.util.Map;
import java.util.function.Function;

public interface DomainConnector {
    Map<String, Function<DomainRequest, DomainResponse>> domainRegistration();
}
