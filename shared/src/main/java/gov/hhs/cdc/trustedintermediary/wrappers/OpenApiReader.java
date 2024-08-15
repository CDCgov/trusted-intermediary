package gov.hhs.cdc.trustedintermediary.wrappers;

import gov.hhs.cdc.trustedintermediary.domainconnector.UnableToReadOpenApiSpecificationException;

/** Wrapper for classes that load OpenApi documentation. */
public interface OpenApiReader {
    String openAsString(String fileName) throws UnableToReadOpenApiSpecificationException;
}
