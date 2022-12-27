package gov.hhs.cdc.trustedintermediary.wrappers;

/***
 * Custom exception class use to catch formatting processing exceptions
 */
public class FormatterProcessingException extends Exception {

    public FormatterProcessingException(String errorMessage) {
        super(errorMessage);
    }

    public FormatterProcessingException(String errorMessage, Throwable e) {
        super(errorMessage, e);
    }
}
