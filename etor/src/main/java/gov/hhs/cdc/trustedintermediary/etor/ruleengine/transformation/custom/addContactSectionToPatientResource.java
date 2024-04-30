package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiOrderConverter;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;

public class addContactSectionToPatientResource implements CustomFhirTransformation {
    private final List<Coding> CODING_LIST =
            List.of(
                    new Coding(
                            "http://terminology.hl7.org/CodeSystem/v3-RoleCode", "MTH", "mother"));

    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();

        var patients = HapiOrderConverter.findAllPatients(bundle);

        patients.forEach(
                p -> {
                    var myContact = p.addContact();
                    var motherRelationship = myContact.addRelationship();
                    motherRelationship.setCoding(CODING_LIST);

                    var mothersMaidenNameExtension =
                            p.getExtensionByUrl(
                                    "http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName");
                    if (mothersMaidenNameExtension != null) {
                        myContact.setName(p.castToHumanName(mothersMaidenNameExtension.getValue()));
                    }
                    myContact.setTelecom(p.getTelecom());
                    myContact.setAddress(p.getAddressFirstRep());
                });
    }
}
