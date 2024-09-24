package gov.hhs.cdc.trustedintermediary.wrappers;

import java.io.InputStream;

public record HL7FileStream(String fileName, InputStream inputStream) {}
