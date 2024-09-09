package gov.hhs.cdc.trustedintermediary.rse2e;

import java.io.InputStream;
import java.util.List;

public interface FileFetcher {
    List<InputStream> fetchFiles();
}
