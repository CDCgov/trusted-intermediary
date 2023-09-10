package gov.hhs.cdc.trustedintermediary.external.formatter;

/** Custom exception class use to catch formatting processing exceptions */
public class FormatterProcessingException extends Exception {

    public FormatterProcessingException(String errorMessage, Throwable e) {
        super(errorMessage, e);
    }
}
