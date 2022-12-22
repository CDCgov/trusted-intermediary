package gov.hhs.cdc.trustedintermediary.wrappers;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Formatter {

    <T> T convertToObject(String input, Class<T> clazz) throws JsonProcessingException;

    String convertToString(Object obj) throws JsonProcessingException;
}
