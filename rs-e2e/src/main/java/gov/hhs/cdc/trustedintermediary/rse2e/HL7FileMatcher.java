package gov.hhs.cdc.trustedintermediary.rse2e;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import ca.uhn.hl7v2.parser.Parser;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HL7FileMatcher {

    public static Map<Message, Message> matchFiles(
            List<InputStream> inputFiles, List<InputStream> outputFiles) {

        Map<String, Message> inputMap = mapMessageByControlId(inputFiles);
        Map<String, Message> outputMap = mapMessageByControlId(outputFiles);
        Map<Message, Message> messageMap = new HashMap<>();

        Set<String> unmatchedInputKeys = new HashSet<>(inputMap.keySet());
        unmatchedInputKeys.removeAll(outputMap.keySet());

        Set<String> unmatchedOutputKeys = new HashSet<>(outputMap.keySet());
        unmatchedOutputKeys.removeAll(inputMap.keySet());

        if (!unmatchedInputKeys.isEmpty() || !unmatchedOutputKeys.isEmpty()) {
            // TODO - log the mismatched stuff here
        }

        inputMap.keySet().retainAll(outputMap.keySet());
        inputMap.forEach(
                (key, inputMessage) -> {
                    Message outputMessage = outputMap.get(key);
                    messageMap.put(inputMessage, outputMessage);
                });

        return messageMap;
    }

    public static Map<String, Message> mapMessageByControlId(List<InputStream> files) {

        HapiContext context = new DefaultHapiContext();
        Parser parser = context.getGenericParser();

        Map<String, Message> messageMap = new HashMap<>();

        for (InputStream inputFile : files) {
            try {
                String content = new String(inputFile.readAllBytes());
                Message message = parser.parse(content);
                MSH mshSegment = (MSH) message.get("MSH");
                String msh10 = mshSegment.getMessageControlID().getValue();
                if (msh10 == null || msh10.isEmpty()) {
                    throw new IllegalArgumentException("MSH-10 is empty");
                }
                messageMap.put(msh10, message);
            } catch (IOException | HL7Exception e) {
                // TODO - log exceptions
                // TODO - some transformations may make the messages unparseable - what do we want
                // to do then?
                // CA's transformation for MSH-9 for ORU messages is one example
            }
        }

        return messageMap;
    }
}
