package gov.hhs.cdc.trustedintermediary.wrappers;

/**
 * Signifies something that can convert objects to {@link String}s and {@link String}s back to
 * objects. It makes no assumption as to what the string format is.
 */
public interface Formatter {

    <T> T convertJsonToObject(String input, Class<T> clazz) throws FormatterProcessingException;

    <T> T convertYamlToObject(String input, Class<T> clazz) throws FormatterProcessingException;

    String convertToString(Object obj) throws FormatterProcessingException;
}
