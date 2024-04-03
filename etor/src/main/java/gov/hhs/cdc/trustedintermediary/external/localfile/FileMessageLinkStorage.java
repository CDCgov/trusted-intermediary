package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class FileMessageLinkStorage implements MessageLinkStorage {

    private static final FileMessageLinkStorage INSTANCE = new FileMessageLinkStorage();

    @Inject Formatter formatter;
    @Inject Logger logger;

    static final Path MESSAGE_LINK_FILE_PATH;
    private static int messageLinkId;

    static {
        try {
            Path userTempPath = Paths.get(System.getProperty("java.io.tmpdir"));
            MESSAGE_LINK_FILE_PATH = userTempPath.resolve("cdctimessagelink.json");
            Files.writeString(
                    MESSAGE_LINK_FILE_PATH,
                    "[]",
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            messageLinkId = 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FileMessageLinkStorage() {}

    public static FileMessageLinkStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized MessageLink getMessageLink(String submissionId)
            throws MessageLinkException {
        try {
            Set<MessageLink> messageLinks = readMessageLinks();
            return messageLinks.stream()
                    .filter(link -> link.messageId().equals(submissionId))
                    .findFirst()
                    .orElseThrow(
                            () ->
                                    new MessageLinkException(
                                            "MessageLink not found for submissionId: "
                                                    + submissionId));
        } catch (IOException | FormatterProcessingException e) {
            throw new MessageLinkException("Error retrieving message links", e);
        }
    }

    @Override
    public synchronized void saveMessageLink(Set<String> messageIds, int linkId)
            throws MessageLinkException {
        try {
            Set<MessageLink> messageLinks = readMessageLinks();
            Set<MessageLink> newLinks =
                    messageIds.stream()
                            .map(messageId -> new MessageLink(messageLinkId++, linkId, messageId))
                            .collect(Collectors.toSet());
            messageLinks.addAll(newLinks);
            writeMessageLinks(messageLinks);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (FormatterProcessingException e) {
            throw new MessageLinkException("Error saving message links", e);
        }
    }

    private Set<MessageLink> readMessageLinks() throws IOException, FormatterProcessingException {
        String messageLinkContent = Files.readString(MESSAGE_LINK_FILE_PATH);
        return formatter.convertJsonToObject(messageLinkContent, new TypeReference<>() {});
    }

    private void writeMessageLinks(Set<MessageLink> messageLinks)
            throws IOException, FormatterProcessingException {
        String json = formatter.convertToJsonString(messageLinks);
        Files.writeString(MESSAGE_LINK_FILE_PATH, json, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
