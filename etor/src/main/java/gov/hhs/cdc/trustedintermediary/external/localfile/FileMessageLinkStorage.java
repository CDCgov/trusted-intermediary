package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import javax.inject.Inject;

public class FileMessageLinkStorage implements MessageLinkStorage {

    private static final FileMessageLinkStorage INSTANCE = new FileMessageLinkStorage();

    @Inject Formatter formatter;
    @Inject Logger logger;

    static final Path MESSAGE_LINK_DIRECTORY;

    static {
        try {
            Path userTempPath = Paths.get(System.getProperty("java.io.tmpdir"));
            MESSAGE_LINK_DIRECTORY = userTempPath.resolve("cdctimessagelink");
            FileAttribute<?> onlyOwnerAttrs =
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rwx------"));
            Files.createDirectories(MESSAGE_LINK_DIRECTORY, onlyOwnerAttrs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FileMessageLinkStorage() {}

    public static FileMessageLinkStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public MessageLink getMessageLink(String submissionId) throws MessageLinkException {
        return null;
    }

    @Override
    public void saveMessageLink(Set<String> messageIds, int linkId) throws MessageLinkException {}
}
