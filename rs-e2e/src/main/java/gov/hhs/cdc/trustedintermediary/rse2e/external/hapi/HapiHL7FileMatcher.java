package gov.hhs.cdc.trustedintermediary.rse2e.external.hapi;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import com.google.common.collect.Sets;
import gov.hhs.cdc.trustedintermediary.rse2e.HL7FileStream;
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

    public Map<HapiHL7Message, HapiHL7Message> matchFiles(
            List<HL7FileStream> outputFiles, List<HL7FileStream> inputFiles)
            throws HapiHL7FileMatcherException {
        // We pair up output and input files based on the control ID, which is in MSH-10
        // Any files (either input or output) that don't have a match are logged
        Map<String, HapiHL7Message> inputMap = parseAndMapMessageByControlId(inputFiles);
        Map<String, HapiHL7Message> outputMap = parseAndMapMessageByControlId(outputFiles);

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

    public Map<String, HapiHL7Message> parseAndMapMessageByControlId(List<HL7FileStream> files)
            throws HapiHL7FileMatcherException {

        Map<String, HapiHL7Message> messageMap = new HashMap<>();

        try (HapiContext context = new DefaultHapiContext()) {
            Parser parser = context.getPipeParser();

            for (HL7FileStream hl7FileStream : files) {
                String fileName = hl7FileStream.fileName();
                try (InputStream inputStream = hl7FileStream.inputStream()) {
                    String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    Message message = parser.parse(content);
                    HapiHL7Message hapiHL7Message = new HapiHL7Message(message);
                    String msh10 = hapiHL7Message.getMessageIdentifier();
                    if (msh10 == null || msh10.isEmpty()) {
                        throw new HapiHL7FileMatcherException(
                                String.format("MSH-10 is empty for file: %s", fileName));
                    }
                    messageMap.put(msh10, hapiHL7Message);
                } catch (HL7Exception e) {
                    throw new HapiHL7FileMatcherException(
                            String.format("Failed to parse HL7 message from file: %s", fileName),
                            e);
                } catch (IOException e) {
                    throw new HapiHL7FileMatcherException(
                            String.format("Failed to read file: %s", fileName), e);
                }
            }
        } catch (IOException e) {
            throw new HapiHL7FileMatcherException("Failed to close input stream", e);
        }

        return messageMap;
    }
}
