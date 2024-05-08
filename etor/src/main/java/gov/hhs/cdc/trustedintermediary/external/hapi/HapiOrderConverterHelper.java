package gov.hhs.cdc.trustedintermediary.external.hapi;

import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Patient;

/** Helper class with transformation methods that take an order Bundle and modifies it */
public class HapiOrderConverterHelper {

    private static final Coding OML_CODING =
            new Coding(
                    "http://terminology.hl7.org/CodeSystem/v2-0003",
                    "O21",
                    "OML - Laboratory order");

    private static final List<Coding> CODING_LIST =
            List.of(
                    new Coding(
                            "http://terminology.hl7.org/CodeSystem/v3-RoleCode", "MTH", "mother"));

    public static void convertToOmlOrder(Bundle order) {
        var messageHeader = HapiMessageConverterHelper.findOrInitializeMessageHeader(order);
        messageHeader.setEvent(OML_CODING);
    }

    public static void addContactSectionToPatientResource(Bundle order) {
        HapiHelper.resourcesInBundle(order, Patient.class)
                .forEach(
                        p -> {
                            var myContact = p.addContact();
                            var motherRelationship = myContact.addRelationship();
                            motherRelationship.setCoding(CODING_LIST);

                            var mothersMaidenNameExtension =
                                    p.getExtensionByUrl(
                                            "http://hl7.org/fhir/StructureDefinition/patient-mothersMaidenName");
                            if (mothersMaidenNameExtension != null) {
                                myContact.setName(
                                        p.castToHumanName(mothersMaidenNameExtension.getValue()));
                            }
                            myContact.setTelecom(p.getTelecom());
                            myContact.setAddress(p.getAddressFirstRep());
                        });
    }
}
