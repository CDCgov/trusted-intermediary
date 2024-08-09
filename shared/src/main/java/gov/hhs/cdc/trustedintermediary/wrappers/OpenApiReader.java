package gov.hhs.cdc.trustedintermediary.wrappers;

import gov.hhs.cdc.trustedintermediary.domainconnector.UnableToReadOpenApiSpecificationException;
import java.nio.charset.Charset;

/** Wrapper for classes that load OpenApi documentation. */
public interface OpenApiReader {
    String openAsString(String fileName, Charset charset)
            throws UnableToReadOpenApiSpecificationException;
}
