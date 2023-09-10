package gov.hhs.cdc.trustedintermediary.context;

import static org.reflections.scanners.Scanners.FieldsAnnotated;
import static org.reflections.scanners.Scanners.SubTypes;

import java.lang.reflect.Field;
import java.util.Set;
import org.reflections.Reflections;

/**
 * A helper class that helps in Java reflection and interacts with the org.reflections library. This
 * class is package private by choice and should be accessed through the ApplicationContext or other
 * appropriate package public classes.
 */
class Reflection {

    private static final Reflections REFLECTIONS =
            new Reflections("gov.hhs.cdc.trustedintermediary", SubTypes, FieldsAnnotated);

    private Reflection() {}

    public static <T> Set<Class<? extends T>> getImplementors(Class<T> interfaze) {
        return (Set<Class<? extends T>>)
                REFLECTIONS.get(SubTypes.of(interfaze).as((Class<? extends T>) Class.class));
    }

    public static Set<Field> getFieldsAnnotatedWith(Class<?> annotation) {
        return REFLECTIONS.get(FieldsAnnotated.with(annotation).as(Field.class));
    }
}
