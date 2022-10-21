package gov.hhs.cdc.trustedintermediary.external.helpers;

import static org.reflections.scanners.Scanners.SubTypes;

import java.util.Set;
import org.reflections.Reflections;

public class Reflection {

    private Reflection() {}

    public static <T> Set<Class<? extends T>> getImplementors(Class<T> interfaze) {
        var reflections = new Reflections("gov.hhs.cdc.trustedintermediary");
        return (Set<Class<? extends T>>)
                reflections.get(SubTypes.of(interfaze).as((Class<? extends T>) Class.class));
    }
}
