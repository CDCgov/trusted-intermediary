package gov.hhs.cdc.trustedintermediary.rse2e.external.hapi;

import com.google.common.collect.Sets;
import gov.hhs.cdc.trustedintermediary.rse2e.HL7FileStream;
import gov.hhs.cdc.trustedintermediary.rse2e.HL7Message;
import gov.hhs.cdc.trustedintermediary.rse2e.HL7Parser;
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
 * The HapiHL7FileMatcher class is responsible for matching input and output HL7 files based on the
 * control ID (MSH-10).
 */
public class HapiHL7FileMatcher {

    private static final HapiHL7FileMatcher INSTANCE = new HapiHL7FileMatcher();

    private HapiHL7FileMatcher() {}

    public static HapiHL7FileMatcher getInstance() {
        return INSTANCE;
    }

    public Map<HL7Message, HL7Message> matchFiles(
            List<HL7FileStream> outputFiles, List<HL7FileStream> inputFiles)
            throws HapiHL7FileMatcherException {
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
            throw new HapiHL7FileMatcherException(
                    "Found no match for messages with the following MSH-10 values: "
                            + unmatchedKeys);
        }

        return inputKeys.stream().collect(Collectors.toMap(inputMap::get, outputMap::get));
    }

    public Map<String, HL7Message> parseAndMapMessageByControlId(List<HL7FileStream> files)
            throws HapiHL7FileMatcherException {

        Map<String, HL7Message> messageMap = new HashMap<>();

        for (HL7FileStream hl7FileStream : files) {
            String fileName = hl7FileStream.fileName();
            try (InputStream inputStream = hl7FileStream.inputStream()) {
                String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                HL7Message message = HL7Parser.parse(content);
                String msh10 = message.getIdentifier();
                if (msh10 == null || msh10.isEmpty()) {
                    throw new HapiHL7FileMatcherException(
                            String.format("MSH-10 is empty for file: %s", fileName));
                }
                messageMap.put(msh10, message);
                //                } catch (HL7Exception e) {
                //                    throw new HapiHL7FileMatcherException(
                //                            String.format("Failed to parse HL7 message from file:
                // %s", fileName),
                //                            e);
            } catch (IOException e) {
                throw new HapiHL7FileMatcherException(
                        String.format("Failed to read file: %s", fileName), e);
            }
        }

        return messageMap;
    }
}
