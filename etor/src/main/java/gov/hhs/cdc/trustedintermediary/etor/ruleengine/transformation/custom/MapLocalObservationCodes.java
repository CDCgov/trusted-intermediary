package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.messages.IdentifierCode;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.HashMap;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.StringType;

/**
 * Maps local observation codes to LOINC/PLT codes. In order to map a code, the observation must
 * have a single local code with cwe-coding extension 'alt-coding'. The mapped LOINC or PLT code is
 * added to the observation as an additional observation coding with cwe-coding extension 'coding'.
 * When converted to/from HL7, these codings correspond to OBX-3.4/5/6 for the local code, and
 * OBX-3.1/2/3 for the LOINC/PLT code.
 */
public class MapLocalObservationCodes implements CustomFhirTransformation {
    protected final Logger logger = ApplicationContext.getImplementation(Logger.class);

    @Override
    public void transform(HealthData<?> resource, Map<String, Object> args) {
        var codingMap = getMapFromArgs(args);

        var bundle = (Bundle) resource.getUnderlyingData();
        var msh41Identifier =
                HapiHelper.getMSH4_1Identifier(bundle) != null
                        ? HapiHelper.getMSH4_1Identifier(bundle).getValue()
                        : null;
        var messageId = HapiHelper.getMessageControlId(bundle);
        var observations = HapiHelper.resourcesInBundle(bundle, Observation.class);

        observations
                .filter(this::hasValidCoding)
                .forEach(
                        observation ->
                                processCoding(observation, codingMap, msh41Identifier, messageId));
    }

    private boolean hasValidCoding(Observation observation) {
        var codingList = observation.getCode().getCoding();
        return codingList.size() == 1 && isLocalCode(codingList.get(0));
    }

    private boolean isLocalCode(Coding coding) {
        return HapiHelper.hasDefinedCoding(
                coding, HapiHelper.EXTENSION_ALT_CODING, HapiHelper.LOCAL_CODE);
    }

    private void processCoding(
            Observation observation,
            Map<String, IdentifierCode> codingMap,
            String msh41Identifier,
            String messageId) {
        var originalCoding = observation.getCode().getCoding().get(0);
        IdentifierCode identifier = codingMap.get(originalCoding.getCode());

        if (identifier == null) {
            logUnmappedLocalCode(originalCoding, msh41Identifier, messageId);
            return;
        }

        var mappedCoding = getMappedCoding(identifier);
        observation.getCode().getCoding().add(0, mappedCoding);
    }

    private String validateField(String field, String fieldName) {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("missing or empty required field " + fieldName);
        }
        return field;
    }

    private void logUnmappedLocalCode(Coding coding, String msh41Identifier, String messageId) {

        logger.logWarning(
                "Unmapped local code detected: '{}', from sender: '{}', message Id: '{}'",
                coding.getCode(),
                msh41Identifier,
                messageId);
    }

    private Coding getMappedCoding(IdentifierCode identifierCode) {
        var mappedCoding =
                new Coding(
                        HapiHelper.urlForCodeType(identifierCode.codingSystem()),
                        identifierCode.code(),
                        identifierCode.display());
        mappedCoding.addExtension(HapiHelper.EXTENSION_CWE_CODING, new StringType("coding"));

        mappedCoding.addExtension(
                HapiHelper.EXTENSION_CODING_SYSTEM, new StringType(identifierCode.codingSystem()));

        return mappedCoding;
    }

    private Map<String, IdentifierCode> getMapFromArgs(Map<String, Object> args) {
        var codingMap = new HashMap<String, IdentifierCode>();

        // Suppressing the unchecked cast warning. The assignment below will throw a
        // ClassCastException if it fails.
        @SuppressWarnings("unchecked")
        var argsCodingMap = (Map<String, Map<String, String>>) args.get("codingMap");

        for (Map.Entry<String, Map<String, String>> entry : argsCodingMap.entrySet()) {
            var localCode = entry.getKey();
            var mappedCode = getIdentifierCode(entry);

            codingMap.put(localCode, mappedCode);
        }

        return codingMap;
    }

    private IdentifierCode getIdentifierCode(Map.Entry<String, Map<String, String>> entry) {
        var value = entry.getValue();
        var code = validateField(value.get("code"), "code");
        var display = validateField(value.get("display"), "display");
        var codingSystem = validateField(value.get("codingSystem"), "codingSystem");

        return new IdentifierCode(code, display, codingSystem);
    }
}
