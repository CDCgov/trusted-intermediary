package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;
import java.util.stream.Stream;
import javax.inject.Inject;

/** Implements the {@link PartnerMetadataStorage} using local files. */
public class FilePartnerMetadataStorage implements PartnerMetadataStorage {

    private static final FilePartnerMetadataStorage INSTANCE = new FilePartnerMetadataStorage();

    @Inject Formatter formatter;
    @Inject Logger logger;

    private static final Path METADATA_DIRECTORY;

    static {
        try {
            Path userTempPath = Paths.get(System.getProperty("java.io.tmpdir"));
            METADATA_DIRECTORY = userTempPath.resolve("cdctimetadata");
            FileAttribute<?> onlyOwnerAttrs =
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rwx------"));
            Files.createDirectories(METADATA_DIRECTORY, onlyOwnerAttrs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FilePartnerMetadataStorage() {}

    public static FilePartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<PartnerMetadata> readMetadata(final String submissionId)
            throws PartnerMetadataException {
        try {
            Path filePath = searchFilePath(submissionId);
            if (filePath == null || !Files.exists(filePath)) {
                logger.logWarning("Metadata file not found: {}", filePath);
                return Optional.empty();
            }
            String content = Files.readString(filePath);
            PartnerMetadata metadata =
                    formatter.convertJsonToObject(content, new TypeReference<>() {});
            return Optional.ofNullable(metadata);
        } catch (IOException | FormatterProcessingException e) {
            throw new PartnerMetadataException("Unable to read the metadata file", e);
        }
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) throws PartnerMetadataException {
        Path metadataFilePath =
                getFilePath(metadata.receivedSubmissionId() + "|" + metadata.sentSubmissionId());
        try {
            String content = formatter.convertToJsonString(metadata);
            Files.writeString(metadataFilePath, content);
            logger.logInfo(
                    "Saved metadata for {} to {}",
                    metadata.receivedSubmissionId(),
                    metadataFilePath);
        } catch (IOException | FormatterProcessingException e) {
            throw new PartnerMetadataException(
                    "Error saving metadata for " + metadata.receivedSubmissionId(), e);
        }
    }

    private Path getFilePath(String metadataId) {
        return METADATA_DIRECTORY.resolve(metadataId + ".json");
    }

    private Path searchFilePath(String metadataId) throws IOException {

        Path path = null;

        try (Stream<Path> fileList = Files.list(METADATA_DIRECTORY)) {
            path =
                    fileList.filter(
                                    metadataPath -> {
                                        String fileName = metadataPath.getFileName().toString();
                                        return fileName.startsWith(metadataId)
                                                || fileName.endsWith(metadataId + ".json");
                                    })
                            .findFirst()
                            .orElse(null);
        }

        return path;
    }
}
