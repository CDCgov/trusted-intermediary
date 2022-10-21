package gov.hhs.cdc.trustedintermediary.domainconnector;

import java.util.Map;
import java.util.function.Function;

/**
 * Specifies how a domain will connect itself with the larger trusted intermediary.
 *
 * <p>To register a new domain with the trusted intermediary, you must... <br>
 * - Create a new class that implements this interface. <br>
 * - Have a constructor that doesn't take any arguments. This can be fulfilled by the default
 * contructor.
 *
 * <p>The trusted intermediary will automatically discover all implementers of this interface and
 * call the implemented methods as appropriate.
 */
public interface DomainConnector {
    /**
     * The trusted intermediary calls this method to register different HTTP endpoints and their
     * associated functions that will be invoked from an HTTP request.
     *
     * @return A mapping of endpoints to functions. The function will be invoked when an HTTP
     *     request comes in on the associated endpoint.
     */
    Map<HttpVerbPath, Function<DomainRequest, DomainResponse>> domainRegistration();
}
