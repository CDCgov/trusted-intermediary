package gov.hhs.cdc.trustedintermediary.domainconnector;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import java.util.Map;
import javax.inject.Inject;

public class DomainResponseHelper {
    static final String CONTENT_TYPE_LITERAL = "Content-Type";
    static final String APPLICATION_JSON_LITERAL = "application/json";

    @Inject Formatter formatter;
    @Inject Logger logger;

    private DomainResponseHelper() {}

    public static DomainResponseHelper getInstance() {
        return new DomainResponseHelper();
    }

    public DomainResponse constructResponse(Object objectResponseBody) {
        logger.logInfo("Constructing the response");
        var response = new DomainResponse(200);

        try {
            var responseBody = formatter.convertToJsonString(objectResponseBody);
            response.setBody(responseBody);
        } catch (FormatterProcessingException e) {
            logger.logError("Error constructing an orders response", e);
            return constructGenericInternalServerErrorResponse();
        }

        response.setHeaders(Map.of(CONTENT_TYPE_LITERAL, APPLICATION_JSON_LITERAL));

        return response;
    }

    public static DomainResponse constructGenericInternalServerErrorResponse() {
        var domainResponse = new DomainResponse(500);
        domainResponse.setBody("An internal server error occurred");
        return domainResponse;
    }
}
