/**
 * ApplicationContext class works similar to a factory. The idea is to use this class in conjunction
 * with annoations that will be used to inject the implementations. There is the option of using the
 * getImplementation method to get an implementation of a class. This is a double checked locking
 * singleton class.
 */
package gov.hhs.cdc.trustedintermediary.context;

public class ApplicationContext {

    private static volatile ApplicationContext applicationContext = null;
    private static final Map<Class<?>, Object> OBJECT_MAP = new HashMap<>();

    private ApplicationContext() {}

    public static void register(Class<?> clazz, Object implementation) {
        this.OBJECT_MAP.put(clazz, implementation);
    }

    public static <T> T getImplementation(Class<T> clazz) {
        return (T) this.OBJECT_MAP.get(clazz);
    }

    public static ApplicationContext getInstance() {
        if (this.applicationContext == null) {
            synchronized (ApplicationContext.class) {
                if (this.applicationContext == null)
                    this.applicationContext = new ApplicationContext();
            }
        }
        return this.applicationContext;
    }
}
