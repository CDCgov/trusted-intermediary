/**
 * ApplicationContext class works similar to a factory. The idea is to use this class in conjunction
 * with annotations that will be used to inject the implementations.
 */
package gov.hhs.cdc.trustedintermediary.context;

import static org.reflections.scanners.Scanners.FieldsAnnotated;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import org.reflections.Reflections;

public class ApplicationContext {

    private static final Map<Class<?>, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private ApplicationContext() {}

    public static void register(Class<?> clazz, Object implementation) {
        OBJECT_MAP.put(clazz, implementation);
    }

    public static <T> T getImplementation(Class<T> clazz) {
        T object = (T) OBJECT_MAP.get(clazz);

        if (object == null) {
            throw new IllegalArgumentException("Couldn't find object for " + clazz.getName());
        }

        return object;
    }

    public static <T> Set<Class<? extends T>> getImplementors(Class<T> interfaze) {
        return Reflection.getImplementors(interfaze);
    }

    public static void injectRegisteredImplementations() {
        var reflections = new Reflections("gov.hhs.cdc.trustedintermediary");
        var fields = reflections.get(FieldsAnnotated.with(Inject.class).as(Field.class));

        fields.forEach(
                field -> {
                    var fieldType = field.getType();
                    var declaringClass = field.getDeclaringClass();

                    var fieldImplementation = getImplementation(fieldType);
                    var declaringClassImplementation = getImplementation(declaringClass);

                    try {
                        field.set(declaringClassImplementation, fieldImplementation);
                    } catch (IllegalAccessException | IllegalArgumentException exception) {
                        throw new IllegalArgumentException(
                                "Unable to inject " + fieldType + " into " + declaringClass,
                                exception);
                    }
                });
    }
}
