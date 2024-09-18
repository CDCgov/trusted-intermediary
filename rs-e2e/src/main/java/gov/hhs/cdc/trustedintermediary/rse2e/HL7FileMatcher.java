package gov.hhs.cdc.trustedintermediary.rse2e;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import ca.uhn.hl7v2.parser.Parser;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

public class HL7FileMatcher {

    private static final HL7FileMatcher INSTANCE = new HL7FileMatcher();

    @Inject Logger logger;

    private HL7FileMatcher() {}

    public static HL7FileMatcher getInstance() {
        return INSTANCE;
    }

    public Map<Message, Message> matchFiles(
            List<HL7FileStream> inputFiles, List<HL7FileStream> outputFiles) {
        // We pair up input and output files based on the control ID, which is in MSH-10
        // Any files (either input or output) that don't have a match are logged
        Map<String, Message> inputMap = mapMessageByControlId(inputFiles);
        Map<String, Message> outputMap = mapMessageByControlId(outputFiles);

        Set<String> unmatchedInputKeys = new HashSet<>(inputMap.keySet());
        unmatchedInputKeys.removeAll(outputMap.keySet());

        Set<String> unmatchedOutputKeys = new HashSet<>(outputMap.keySet());
        unmatchedOutputKeys.removeAll(inputMap.keySet());

        Set<String> unmatchedKeys = new HashSet<>();
        unmatchedKeys.addAll(unmatchedInputKeys);
        unmatchedKeys.addAll(unmatchedOutputKeys);

        if (!unmatchedKeys.isEmpty()) {
            logger.logError(
                    "Found no match for the following messages with MSH-10: " + unmatchedKeys);
        }

        Map<Message, Message> messageMap = new HashMap<>();
        inputMap.keySet().retainAll(outputMap.keySet());
        inputMap.forEach(
                (key, inputMessage) -> {
                    Message outputMessage = outputMap.get(key);
                    messageMap.put(inputMessage, outputMessage);
                });

        return messageMap;
    }

    public Map<String, Message> mapMessageByControlId(List<HL7FileStream> files) {

        Map<String, Message> messageMap = new HashMap<>();

        try (HapiContext context = new DefaultHapiContext()) {
            Parser parser = context.getPipeParser();

            for (HL7FileStream inputFile : files) {
                try (InputStream inputStream = inputFile.inputStream()) {
                    String content = new String(inputStream.readAllBytes());
                    Message message = parser.parse(content);
                    MSH mshSegment = (MSH) message.get("MSH");
                    String msh10 = mshSegment.getMessageControlID().getValue();
                    if (msh10 == null || msh10.isEmpty()) {
                        logger.logError("MSH-10 is empty for : " + inputFile.fileName());
                        continue;
                    }
                    messageMap.put(msh10, message);
                } catch (IOException | HL7Exception e) {
                    logger.logError(
                            "An error occurred while parsing the message: " + inputFile.fileName(),
                            e);
                }
            }
        } catch (IOException e) {
            logger.logError("An error occurred while reading the file", e);
        }

        return messageMap;
    }
}
