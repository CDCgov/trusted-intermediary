package gov.hhs.cdc.trustedintermediary.rse2e;

import java.util.List;

public interface FileFetcher {
    List<HL7FileStream> fetchFiles();
}
