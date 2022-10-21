package gov.hhs.cdc.trustedintermediary.context;

import static org.reflections.scanners.Scanners.SubTypes;

import java.util.Set;
import org.reflections.Reflections;

/**
 * A helper class that helps in Java reflection and interacts with the org.reflections library. This
 * class is package private by choice and should be accessed through the ApplicationContext or other
 * appropriate package public classes.
 */
class Reflection {

    private Reflection() {}

    public static <T> Set<Class<? extends T>> getImplementors(Class<T> interfaze) {
        var reflections = new Reflections("gov.hhs.cdc.trustedintermediary");
        return (Set<Class<? extends T>>)
                reflections.get(SubTypes.of(interfaze).as((Class<? extends T>) Class.class));
    }
}
