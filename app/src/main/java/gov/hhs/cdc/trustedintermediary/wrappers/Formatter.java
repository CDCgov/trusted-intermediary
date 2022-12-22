package gov.hhs.cdc.trustedintermediary.wrappers;

public interface Formatter {

    <T> T convertToObject(String input, Class<T> clazz) throws FormatterProcessingException;

    String convertToString(Object obj) throws FormatterProcessingException;
}
