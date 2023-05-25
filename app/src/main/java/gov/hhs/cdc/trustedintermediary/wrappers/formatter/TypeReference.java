package gov.hhs.cdc.trustedintermediary.wrappers.formatter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface TypeReference<T> {
    default Type getType() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        return type.getActualTypeArguments()[0];
    }
}
