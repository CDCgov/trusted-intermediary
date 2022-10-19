/**
 * ApplicationContext class works similar to a factory. The idea is to use this class in conjunction
 * with annotations that will be used to inject the implementations. There is the option of using
 * the getImplementation method to get an implementation of a class. This is a double checked
 * locking singleton class.
 */
package gov.hhs.cdc.trustedintermediary.context;

import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {

    private static volatile ApplicationContext applicationContext = null;
    private static final Map<Class<?>, Object> OBJECT_MAP = new HashMap<>();

    private ApplicationContext() {}

    public static void register(Class<?> clazz, Object implementation) {
        OBJECT_MAP.put(clazz, implementation);
    }

    public static <T> T getImplementation(Class<T> clazz) {
        return (T) OBJECT_MAP.get(clazz);
    }

    public static ApplicationContext getInstance() {
        if (applicationContext == null) {
            synchronized (ApplicationContext.class) {
                if (applicationContext == null) {
                    applicationContext = new ApplicationContext();
                }
            }
        }
        return applicationContext;
    }
}
