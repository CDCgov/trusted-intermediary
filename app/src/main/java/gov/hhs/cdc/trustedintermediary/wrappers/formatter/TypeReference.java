package gov.hhs.cdc.trustedintermediary.wrappers.formatter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This interface represents a generic type <code>T</code>. It is used to specify precisely the type
 * returned from the formatter when marshalled.
 *
 * @param <T> the generic type
 */
public interface TypeReference<T> {
    default Type getType() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        return type.getActualTypeArguments()[0];
    }
}
