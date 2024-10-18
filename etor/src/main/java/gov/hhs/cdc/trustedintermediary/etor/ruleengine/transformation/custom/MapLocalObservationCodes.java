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
        var observations = HapiHelper.resourcesInBundle(bundle, Observation.class);

        for (Observation obv : observations.toList()) {
            var codingList = obv.getCode().getCoding();

            if (codingList.size() != 1) {
                continue;
            }

            var coding = codingList.get(0);
            if (!HapiHelper.hasDefinedCoding(
                    coding, HapiHelper.EXTENSION_ALT_CODING, HapiHelper.LOCAL_CODE)) {
                continue;
            }

            var identifier = codingMap.get(coding.getCode());
            if (identifier == null) {
                logUnmappedLocalCode(bundle, coding);
                continue;
            }

            var mappedCoding = getMappedCoding(identifier);

            // Add the mapped code as the first in the list, ahead of the existing alternate code
            codingList.add(0, mappedCoding);
        }
    }

    private void logUnmappedLocalCode(Bundle bundle, Coding coding) {
        var msh41Identifier = HapiHelper.getMSH4_1Identifier(bundle);
        var msh41Value = msh41Identifier != null ? msh41Identifier.getValue() : null;

        logger.logWarning(
                "Unmapped local code detected: '{}', from sender: '{}', message Id: '{}'",
                coding.getCode(),
                msh41Value,
                HapiHelper.getMessageControlId(bundle));
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

        // Should fail if null
        // TODO: Determine how we want to handle if args is not structured properly and update the
        // "When bad args" tests to reflect the approach.
        var argsCodingMap = (Map<String, Map<String, String>>) args.get("codingMap");

        for (Map.Entry<String, Map<String, String>> entry : argsCodingMap.entrySet()) {
            var localCode = entry.getKey();
            var value = entry.getValue();
            var mappedCode =
                    new IdentifierCode(
                            value.get("code"), value.get("display"), value.get("codingSystem"));

            codingMap.put(localCode, mappedCode);
        }

        return codingMap;
    }
}
