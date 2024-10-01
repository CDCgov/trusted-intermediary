package gov.hhs.cdc.trustedintermediary.rse2e.external.hapi;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import ca.uhn.hl7v2.parser.Parser;
import gov.hhs.cdc.trustedintermediary.rse2e.HL7FileStream;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

/**
 * The HapiHL7FileMatcher class is responsible for matching input and output HL7 files based on the
 * control ID (MSH-10).
 */
public class HapiHL7FileMatcher {

    private static final HapiHL7FileMatcher INSTANCE = new HapiHL7FileMatcher();

    @Inject Logger logger;

    private HapiHL7FileMatcher() {}

    public static HapiHL7FileMatcher getInstance() {
        return INSTANCE;
    }

    public Map<Message, Message> matchFiles(
            List<HL7FileStream> outputFiles, List<HL7FileStream> inputFiles) {
        // We pair up output and input files based on the control ID, which is in MSH-10
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

            for (HL7FileStream hl7FileStream : files) {
                try (InputStream inputStream = hl7FileStream.inputStream()) {
                    String content = new String(inputStream.readAllBytes());
                    Message message = parser.parse(content);
                    MSH mshSegment = (MSH) message.get("MSH");
                    String msh10 = mshSegment.getMessageControlID().getValue();
                    if (msh10 == null || msh10.isEmpty()) {
                        logger.logError("MSH-10 is empty for : " + hl7FileStream.fileName());
                        continue;
                    }
                    messageMap.put(msh10, message);
                } catch (IOException | HL7Exception e) {
                    logger.logError(
                            "An error occurred while parsing the message: "
                                    + hl7FileStream.fileName(),
                            e);
                }
            }
        } catch (IOException e) {
            logger.logError("An error occurred while reading the file", e);
        }

        return messageMap;
    }
}
