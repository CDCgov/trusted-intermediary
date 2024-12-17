package gov.hhs.cdc.trustedintermediary.rse2e;

import java.util.List;

/**
 * The FileFetcher interface represents a component responsible for fetching files. Implementations
 * of this interface should provide a way to retrieve a list of HL7FileStream objects.
 */
public interface FileFetcher {
    List<HL7FileStream> fetchFiles(boolean isAutomatedTest);
}
