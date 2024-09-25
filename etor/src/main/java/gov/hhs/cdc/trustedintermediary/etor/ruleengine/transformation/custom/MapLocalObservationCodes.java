package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.messages.IdentifierCode;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
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

    private HashMap<String, IdentifierCode> codingMap;

    public MapLocalObservationCodes() {
        initMap();
    }

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        var bundle = (Bundle) resource.getUnderlyingResource();
        var observations = HapiHelper.resourcesInBundle(bundle, Observation.class);

        for (Observation obv : observations.toList()) {
            var codingList = obv.getCode().getCoding();

            if (codingList.size() != 1) {
                continue;
            }

            var coding = codingList.get(0);
            if (!HapiHelper.hasLocalCodeInAlternateCoding(coding)) {
                continue;
            }

            var identifier = codingMap.get(coding.getCode());
            if (identifier == null) {
                logUnmappedLocalCode(bundle, coding);
                continue;
            }

            var mappedCoding = getMappedCoding(identifier);
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

    /**
     * Initializes the local-to-LOINC/PLT hash map, customized for CDPH and UCSD. Currently, the
     * mapping is hardcoded for simplicity. If expanded to support additional entities, the
     * implementation may be updated to allow dynamic configuration via
     * transformation_definitions.json or a database-driven mapping.
     */
    private void initMap() {
        this.codingMap = new HashMap<>();
        codingMap.put(
                "99717-32",
                new IdentifierCode(
                        "85269-9",
                        "X-linked Adrenoleukodystrophy (X- ALD) newborn screen interpretation",
                        HapiHelper.LOINC_CODE));
        codingMap.put(
                "99717-33",
                new IdentifierCode(
                        "85268-1",
                        "X-linked Adrenoleukodystrophy (X- ALD) newborn screening comment-discussion",
                        HapiHelper.LOINC_CODE));
        codingMap.put(
                "99717-34",
                new IdentifierCode(
                        "PLT325",
                        "ABCD1 gene mutation found [Identifier] in DBS by Sequencing",
                        HapiHelper.PLT_CODE));
        codingMap.put(
                "99717-6",
                new IdentifierCode(
                        "53340-6",
                        "17-Hydroxyprogesterone [Moles/volume] in DBS",
                        HapiHelper.LOINC_CODE));
        // map.put("99717-35", new IdentifierCode("REQUEST_PLT", "REQUEST_PLT",
        // HapiHelper.PLT_CODE));
        // map.put("99717-36", new IdentifierCode("REQUEST_PLT", "REQUEST_PLT",
        // HapiHelper.PLT_CODE));
        codingMap.put(
                "99717-48",
                new IdentifierCode(
                        "PLT3258",
                        "IDUA gene mutations found [Identifier] in DBS by Sequencing",
                        HapiHelper.PLT_CODE));
        // map.put("99717-44", new IdentifierCode("REQUEST_PLT", "REQUEST_PLT",
        // HapiHelper.PLT_CODE));
        codingMap.put(
                "99717-50",
                new IdentifierCode(
                        "PLT3275",
                        "IDS gene mutations found [Identifier] in Dried Bloodspot by Molecular genetics method Nominal",
                        HapiHelper.PLT_CODE));
        codingMap.put(
                "99717-47",
                new IdentifierCode(
                        "PLT3252",
                        "GAA gene mutation found [Identifier] in DBS by Sequencing",
                        HapiHelper.PLT_CODE));
        // map.put("99717-46", new IdentifierCode("REQUEST_PLT", "REQUEST_PLT",
        // HapiHelper.PLT_CODE));
        // map.put("99717-60", new IdentifierCode("REQUEST_PLT", "REQUEST_PLT",
        // HapiHelper.PLT_CODE));
    }
}
