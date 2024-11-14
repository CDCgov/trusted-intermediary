package gov.hhs.cdc.trustedintermediary.rse2e;

import java.time.LocalDate;

/* The AzureBlobHelper is a utility class that provides helper methods for working with Azure Blob Storage. */
public class AzureBlobHelper {

    private AzureBlobHelper() {}

    public static String buildDatePathPrefix(LocalDate date) {
        return String.format(
                "%d/%02d/%02d/", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    public static String createDateBasedPath(LocalDate date, String originalName) {
        return buildDatePathPrefix(date) + originalName;
    }

    public static boolean isInDateFolder(String blobPath, LocalDate creationDate) {
        String expectedPath = buildDatePathPrefix(creationDate);
        return blobPath.startsWith(expectedPath);
    }
}
