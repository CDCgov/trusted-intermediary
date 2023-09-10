package gov.hhs.cdc.trustedintermediary.external.formatter;

/**
 * Signifies something that can convert objects to {@link String}s and {@link String}s back to
 * objects.
 */
public interface Formatter {

    <T> T convertJsonToObject(String input, TypeReference<T> typeReference)
            throws FormatterProcessingException;

    <T> T convertYamlToObject(String input, TypeReference<T> typeReference)
            throws FormatterProcessingException;

    String convertToJsonString(Object obj) throws FormatterProcessingException;
}
