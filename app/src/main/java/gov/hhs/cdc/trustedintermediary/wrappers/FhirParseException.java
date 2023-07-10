package gov.hhs.cdc.trustedintermediary.wrappers;

/**
 * This exception class gets triggered when an exception is thrown during parsing of a fhir bundle.
 * Some causes that will trigger it are: an empty payload, wrong payload format as it expects a fhir
 * bundle.
 */
public class FhirParseException extends Exception {
    public FhirParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
