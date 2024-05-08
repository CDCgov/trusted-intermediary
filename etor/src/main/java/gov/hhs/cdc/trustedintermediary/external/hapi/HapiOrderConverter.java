package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import gov.hhs.cdc.trustedintermediary.etor.orders.OrderConverter;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.List;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Patient;

/**
 * Converts an order to identify as an HL7v2 OML in the {@link MessageHeader}. Also helps in moving
 * around data in the order.
 */
public class HapiOrderConverter implements OrderConverter {
    private static final HapiOrderConverter INSTANCE = new HapiOrderConverter();
    private static final Coding OML_CODING =
            new Coding(
                    "http://terminology.hl7.org/CodeSystem/v2-0003",
                    "O21",
                    "OML - Laboratory order");

    private static final List<Coding> CODING_LIST =
            List.of(
                    new Coding(
                            "http://terminology.hl7.org/CodeSystem/v3-RoleCode", "MTH", "mother"));

    @Inject Logger logger;

    @Inject HapiMessageConverterHelper hapiMessageConverterHelper;

    public static HapiOrderConverter getInstance() {
        return INSTANCE;
    }

    private HapiOrderConverter() {}

    @Override
    public Order<?> convertToOmlOrder(Order<?> order) {
        logger.logInfo("Converting order to have OML metadata");

        var hapiOrder = (Order<Bundle>) order;
        var orderBundle = hapiOrder.getUnderlyingResource();
        var messageHeader = hapiMessageConverterHelper.findOrInitializeMessageHeader(orderBundle);

        messageHeader.setEvent(OML_CODING);

        return new HapiOrder(orderBundle);
    }

    @Override
    public Order<?> addContactSectionToPatientResource(Order<?> order) {
        logger.logInfo("Adding contact section in Patient resource");

        var hapiOrder = (Order<Bundle>) order;
        var orderBundle = hapiOrder.getUnderlyingResource();

        HapiHelper.resourcesInBundle(orderBundle, Patient.class)
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

        return new HapiOrder(orderBundle);
    }

    @Override
    public Order<?> addEtorProcessingTag(Order<?> message) {
        var hapiOrder = (Order<Bundle>) message;
        var messageBundle = hapiOrder.getUnderlyingResource();

        hapiMessageConverterHelper.addEtorTagToBundle(messageBundle);

        return new HapiOrder(messageBundle);
    }
}
