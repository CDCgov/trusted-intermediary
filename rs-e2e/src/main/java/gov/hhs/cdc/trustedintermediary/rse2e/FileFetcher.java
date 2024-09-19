package gov.hhs.cdc.trustedintermediary.rse2e;

import gov.hhs.cdc.trustedintermediary.external.hapi.HL7FileStream;
import java.util.List;

public interface FileFetcher {
    List<HL7FileStream> fetchFiles();
}
