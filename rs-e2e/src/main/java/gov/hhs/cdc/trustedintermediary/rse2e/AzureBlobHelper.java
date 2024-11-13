package gov.hhs.cdc.trustedintermediary.rse2e;

import java.time.LocalDate;

public class AzureBlobHelper {

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
