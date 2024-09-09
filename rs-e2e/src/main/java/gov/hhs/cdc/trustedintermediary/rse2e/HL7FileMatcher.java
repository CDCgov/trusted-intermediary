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
        // Create maps to hold the MSH-10 values and corresponding HL7 message string
        Map<String, Message> inputMap = new HashMap<>();
        Map<String, Message> outputMap = new HashMap<>();
        Map<Message, Message> messageMap = new HashMap<>();

        inputMap = mapMessageByControlId(inputFiles);
        outputMap = mapMessageByControlId(outputFiles);
        /*
               make a map of MSH-10 to file contents for the input and output files
               then use the key from the input map to look up the matching buddy from the output map
               What should we do with any unmatched input and/or output files?
        */
        // Identify any unmatched keys
        // Get the key sets from both maps
        Set<String> inputKeys = new HashSet<>(inputMap.keySet());
        Set<String> outputKeys = new HashSet<>(outputMap.keySet());

        // Find keys that exist in the input files but not the output
        Set<String> uniqueInputKeys = new HashSet<>(inputKeys);
        uniqueInputKeys.removeAll(outputKeys); // Remove keys present in map2

        // Find keys that exist in the output files but not the input
        Set<String> uniqueOutputKeys = new HashSet<>(outputKeys);
        uniqueOutputKeys.removeAll(inputKeys); // Remove keys present in map1

        if (uniqueOutputKeys.size() > 0 || uniqueInputKeys.size() > 0) {
            // TODO - log the mismatched stuff here
        }

        inputKeys.retainAll(outputKeys);
        // loop through the input map and pair up matched values from the output map
        for (String key : inputKeys) {
            Message inputMessage = inputMap.get(key);
            Message outputMessage = outputMap.get(key);
            messageMap.put(inputMessage, outputMessage);
        }

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
