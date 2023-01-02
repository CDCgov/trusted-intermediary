/**
 * ApplicationContext class works similar to a factory. The idea is to use this class in conjunction
 * with annotations that will be used to inject the implementations.
 */
package gov.hhs.cdc.trustedintermediary.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;

/**
 * Registers, retrieves and injects dependencies and will handle retrieving environmental constants
 * *
 */
public class ApplicationContext {

    protected static final Map<Class<?>, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    protected ApplicationContext() {}

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
        injectRegisteredImplementations(false);
    }

    protected static void injectRegisteredImplementations(boolean skipMissingImplementations) {
        var fields = Reflection.getFieldsAnnotatedWith(Inject.class);

        fields.forEach(
                field -> {
                    var fieldType = field.getType();
                    var declaringClass = field.getDeclaringClass();

                    var declaringClassesToTry = new ArrayList<Class<?>>();
                    declaringClassesToTry.add(declaringClass);
                    declaringClassesToTry.addAll(Arrays.asList(declaringClass.getInterfaces()));

                    Object fieldImplementation;
                    try {
                        fieldImplementation = getImplementation(fieldType);
                    } catch (IllegalArgumentException exception) {
                        if (skipMissingImplementations) {
                            System.err.println(
                                    "Ignoring failure to get implementations to inject into a field");
                            return;
                        }

                        throw exception;
                    }

                    Object declaringClassImplementation =
                            declaringClassesToTry.stream()
                                    .map(
                                            possibleDeclaringClass -> {
                                                Object possibleDeclaringClassImplementation;

                                                try {
                                                    possibleDeclaringClassImplementation =
                                                            getImplementation(
                                                                    possibleDeclaringClass);
                                                } catch (IllegalArgumentException exception) {
                                                    return null;
                                                }

                                                return possibleDeclaringClassImplementation;
                                            })
                                    .filter(Objects::nonNull)
                                    .findFirst()
                                    .orElse(null);

                    if (declaringClassImplementation == null) {
                        if (skipMissingImplementations) {
                            System.err.println(
                                    "Ignoring failure to get implementations to inject into a field");
                            return;
                        }

                        throw new IllegalArgumentException(
                                "Unable to find an implementation for "
                                        + declaringClass
                                        + " given the class itself or its implemented interfaces for injection");
                    }

                    field.trySetAccessible();

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
