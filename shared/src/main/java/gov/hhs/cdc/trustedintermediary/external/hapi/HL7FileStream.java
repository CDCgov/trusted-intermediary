package gov.hhs.cdc.trustedintermediary.external.hapi;

import java.io.InputStream;

public record HL7FileStream(String fileName, InputStream inputStream) {}
