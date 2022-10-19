package gov.hhs.cdc.trustedintermediary.domainconnector;

import java.util.Map;
import java.util.function.Function;

public interface DomainConnector {
    Map<HttpVerbPath, Function<DomainRequest, DomainResponse>> domainRegistration();
}
