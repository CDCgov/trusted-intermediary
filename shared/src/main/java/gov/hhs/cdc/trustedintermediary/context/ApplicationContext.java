/**
 * ApplicationContext class works similar to a factory. The idea is to use this class in conjunction
 * with annotations that will be used to inject the implementations.
 */
package gov.hhs.cdc.trustedintermediary.context;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;

/**
 * Registers, retrieves and injects dependencies, and handles retrieving environmental constants and
 * OS-specific folder operations *
 */
public class ApplicationContext {

    protected static final Map<Class<?>, Object> OBJECT_MAP = new ConcurrentHashMap<>();
    protected static final Map<String, String> TEST_ENV_VARS = new ConcurrentHashMap<>();
    protected static final Set<Object> IMPLEMENTATIONS = new HashSet<>();

    protected ApplicationContext() {}

    public static void register(Class<?> clazz, Object implementation) {
        OBJECT_MAP.put(clazz, implementation);
        IMPLEMENTATIONS.add(implementation.getClass());
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

    public static void injectRegisteredImplementations(boolean skipMissingImplementations) {
        var fields = Reflection.getFieldsAnnotatedWith(Inject.class);

        fields.forEach(field -> injectIntoField(field, skipMissingImplementations));
    }

    public static void injectIntoNonSingleton(Object instance) {
        var fields = Reflection.getFieldsAnnotatedWithInstance(instance.getClass(), Inject.class);

        fields.forEach(field -> injectIntoField(field, instance, false));
    }

    private static void injectIntoField(
            Field field, Object instance, boolean skipMissingImplementations) {
        var fieldType = field.getType();

        Object fieldImplementation = getFieldImplementation(fieldType, skipMissingImplementations);
        if (fieldImplementation == null) {
            return;
        }

        field.trySetAccessible();
        try {
            field.set(instance, fieldImplementation);
        } catch (IllegalAccessException | IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "unable to inject " + fieldType + " into " + instance.getClass(), exception);
        }
    }

    private static void injectIntoField(Field field, boolean skipMissingImplementations) {
        var fieldType = field.getType();
        var declaringClass = field.getDeclaringClass();

        if (!IMPLEMENTATIONS.contains(declaringClass)) {
            // this field is in a class that isn't even registered in the app context, so there's no
            // need to try to inject
            return;
        }

        var declaringClassesToTry = new ArrayList<Class<?>>();
        declaringClassesToTry.add(declaringClass);
        declaringClassesToTry.addAll(Arrays.asList(declaringClass.getInterfaces()));

        Object fieldImplementation = getFieldImplementation(fieldType, skipMissingImplementations);
        if (fieldImplementation == null) {
            return;
        }

        Object declaringClassImplementation =
                getDeclaringClassImplementation(declaringClassesToTry, skipMissingImplementations);
        if (declaringClassImplementation == null) {
            return;
        }

        field.trySetAccessible();

        try {
            field.set(declaringClassImplementation, fieldImplementation);
        } catch (IllegalAccessException | IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unable to inject " + fieldType + " into " + declaringClass, exception);
        }
    }

    private static Object getFieldImplementation(
            Class<?> fieldType, boolean skipMissingImplementations) {
        Object fieldImplementation;

        try {
            fieldImplementation = getImplementation(fieldType);
        } catch (IllegalArgumentException exception) {
            if (skipMissingImplementations) {
                System.err.println(
                        "Ignoring failure to get implementations to inject into a field");
                return null;
            }

            throw exception;
        }

        return fieldImplementation;
    }

    private static Object getDeclaringClassImplementation(
            List<Class<?>> declaringClassesToTry, boolean skipMissingImplementations) {
        Object declaringClassImplementation =
                declaringClassesToTry.stream()
                        .map(
                                possibleDeclaringClass -> {
                                    Object possibleDeclaringClassImplementation;

                                    try {
                                        possibleDeclaringClassImplementation =
                                                getImplementation(possibleDeclaringClass);
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
                return null;
            }

            throw new IllegalArgumentException(
                    "Unable to find an implementation for "
                            + declaringClassesToTry.get(0)
                            + " given the class itself or its implemented interfaces for injection");
        }

        return declaringClassImplementation;
    }

    public static String getProperty(String key) {
        if (!TEST_ENV_VARS.isEmpty()) {
            return TEST_ENV_VARS.get(key);
        }
        return DotEnv.get(key);
    }

    public static String getProperty(String key, String defaultValue) {
        if (!TEST_ENV_VARS.isEmpty()) {
            return TEST_ENV_VARS.getOrDefault(key, defaultValue);
        }
        return DotEnv.get(key, defaultValue);
    }

    public static boolean isPropertyPresent(String key) {
        String value = getProperty(key);
        return value != null && !value.isBlank();
    }

    public static String getEnvironment() {
        return getProperty("ENV", "local");
    }

    private static Path getRootPath() {
        return Paths.get(System.getProperty("user.dir")).getParent();
    }

    private static Path getTempPath() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    private static boolean isPosixFileSystem(Path path) {
        return path.getFileSystem().supportedFileAttributeViews().contains("posix");
    }

    public static Path getExamplesPath() {
        return getRootPath().resolve("examples");
    }

    public static Path createTempFile(String fileName) throws IOException {
        Path tempFilePath = getTempPath().resolve(fileName);

        if (!Files.exists(tempFilePath)) {
            if (isPosixFileSystem(tempFilePath)) {
                FileAttribute<?> onlyOwnerAttrs =
                        PosixFilePermissions.asFileAttribute(
                                PosixFilePermissions.fromString("rwx------"));
                Files.createFile(tempFilePath, onlyOwnerAttrs);
            } else {
                Files.createFile(tempFilePath);
            }
        }
        return tempFilePath;
    }

    public static Path createTempDirectory(String subDirectoryName) throws IOException {
        Path tempDirectoryPath = getTempPath().resolve(subDirectoryName);

        if (isPosixFileSystem(tempDirectoryPath)) {
            FileAttribute<?> onlyOwnerAttrs =
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rwx------"));
            Files.createDirectories(tempDirectoryPath, onlyOwnerAttrs);
        } else {
            Files.createDirectories(tempDirectoryPath);
        }
        return tempDirectoryPath;
    }
}
