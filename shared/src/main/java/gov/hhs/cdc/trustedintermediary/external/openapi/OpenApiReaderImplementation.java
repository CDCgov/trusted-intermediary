package gov.hhs.cdc.trustedintermediary.external.openapi;

import gov.hhs.cdc.trustedintermediary.domainconnector.UnableToReadOpenApiSpecificationException;
import gov.hhs.cdc.trustedintermediary.wrappers.OpenApiReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Basic implementation of a Reader for OpenApi to promote code reuse. If additional functionality
 * is needed, consider implementing a library and wrapping it.
 */
public class OpenApiReaderImplementation implements OpenApiReader {
    private static final OpenApiReaderImplementation INSTANCE = new OpenApiReaderImplementation();

    public static OpenApiReaderImplementation getInstance() {
        return INSTANCE;
    }

    /**
     * Loads the stream of an OpenApi file and returns the output as a string.
     *
     * @param fileName Name of the file to load
     * @return File contents as string
     * @throws UnableToReadOpenApiSpecificationException If there is an issue loading the Api file
     */
    @Override
    public String openAsString(String fileName) throws UnableToReadOpenApiSpecificationException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
        try {
            return new String(
                    Objects.requireNonNull(stream).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            throw new UnableToReadOpenApiSpecificationException(
                    "Failed to open OpenAPI specification for " + fileName, e);
        }
    }
}
