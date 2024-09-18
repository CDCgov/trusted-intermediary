package gov.hhs.cdc.trustedintermediary.rse2e;

import java.io.InputStream;

public record HL7FileStream(String fileName, InputStream inputStream) {}
