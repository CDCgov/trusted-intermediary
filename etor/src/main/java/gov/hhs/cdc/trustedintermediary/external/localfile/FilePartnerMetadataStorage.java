package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javax.inject.Inject;

/** Implements the {@link PartnerMetadataStorage} using local files. */
public class FilePartnerMetadataStorage implements PartnerMetadataStorage {

    private static final FilePartnerMetadataStorage INSTANCE = new FilePartnerMetadataStorage();

    @Inject Formatter formatter;
    @Inject Logger logger;

    private static final Path metadataDirectory;

    static {
        try {
            Path projectRoot = Paths.get(System.getProperty("user.dir"));
            metadataDirectory = projectRoot.getParent().resolve("data/metadata");
            Files.createDirectories(metadataDirectory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FilePartnerMetadataStorage() {}

    public static FilePartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<PartnerMetadata> readMetadata(final String uniqueId)
            throws PartnerMetadataException {
        Path filePath = getFilePath(uniqueId);
        try {
            if (!Files.exists(filePath)) {
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
        Path metadataFilePath = getFilePath(metadata.uniqueId());
        try {
            String content = formatter.convertToJsonString(metadata);
            Files.writeString(metadataFilePath, content);
            logger.logInfo("Saved metadata for " + metadata.uniqueId() + " to " + metadataFilePath);
        } catch (IOException | FormatterProcessingException e) {
            throw new PartnerMetadataException(
                    "Error saving metadata for " + metadata.uniqueId(), e);
        }
    }

    private Path getFilePath(String metadataId) {
        return metadataDirectory.resolve(metadataId + ".json");
    }
}
