package gov.hhs.cdc.trustedintermediary.rse2e;

import java.io.InputStream;

/**
 * The HL7FileStream class represents a file stream that contains HL7 data and the corresponding
 * file name.
 */
public record HL7FileStream(String fileName, InputStream inputStream) {}
