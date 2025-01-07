package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The HL7FileMatcher class is responsible for matching input and output HL7 files based on the
 * control ID (MSH-10).
 */
public class HL7FileMatcher {

    private static final HL7FileMatcher INSTANCE = new HL7FileMatcher();

    private HL7FileMatcher() {}

    public static HL7FileMatcher getInstance() {
        return INSTANCE;
    }

    public Map<HL7Message, HL7Message> matchFiles(
            List<HL7FileStream> outputFiles, List<HL7FileStream> inputFiles)
            throws HL7FileMatcherException {
        // We pair up output and input files based on the control ID, which is in MSH-10
        // Any files (either input or output) that don't have a match are logged
        Map<String, HL7Message> inputMap = parseAndMapMessageByControlId(inputFiles);
        Map<String, HL7Message> outputMap = parseAndMapMessageByControlId(outputFiles);

        Set<String> inputKeys = inputMap.keySet();
        Set<String> outputKeys = outputMap.keySet();
        Set<String> unmatchedKeys = new HashSet<>();
        unmatchedKeys.addAll(Sets.difference(inputKeys, outputKeys));
        unmatchedKeys.addAll(Sets.difference(outputKeys, inputKeys));

        if (!unmatchedKeys.isEmpty()) {
            throw new HL7FileMatcherException(
                    "Found no match for messages with the following MSH-10 values: "
                            + unmatchedKeys);
        }

        return inputKeys.stream().collect(Collectors.toMap(inputMap::get, outputMap::get));
    }

    public Map<String, HL7Message> parseAndMapMessageByControlId(List<HL7FileStream> files)
            throws HL7FileMatcherException {

        Map<String, HL7Message> messageMap = new HashMap<>();

        for (HL7FileStream hl7FileStream : files) {
            String fileName = hl7FileStream.fileName();
            try (InputStream inputStream = hl7FileStream.inputStream()) {
                String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                HL7Message message = HL7Message.parse(content);
                String msh10 = message.getIdentifier();
                if (msh10 == null || msh10.isEmpty()) {
                    throw new HL7FileMatcherException(
                            String.format("MSH-10 is empty for file: %s", fileName));
                }
                messageMap.put(msh10, message);
            } catch (IOException e) {
                throw new HL7FileMatcherException(
                        String.format("Failed to read file: %s", fileName), e);
            }
        }

        return messageMap;
    }
}
