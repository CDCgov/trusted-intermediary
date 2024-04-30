package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.UrlType;

/**
 * Helper class with a variety of utilities to use on a FHIR bundle message. It adds the 'ETOR' tag
 * to a FHIR bundle of type: OML, ORU It also creates the messageHeader resource in a FHIR bundle
 * message.
 */
public class HapiMessageConverterHelper {

    private static final Coding OML_CODING =
            new Coding(
                    "http://terminology.hl7.org/CodeSystem/v2-0003",
                    "O21",
                    "OML - Laboratory order");

    private static final List<Coding> CODING_LIST =
            List.of(
                    new Coding(
                            "http://terminology.hl7.org/CodeSystem/v3-RoleCode", "MTH", "mother"));

    private static final HapiMessageConverterHelper INSTANCE = new HapiMessageConverterHelper();

    public static HapiMessageConverterHelper getInstance() {
        return INSTANCE;
    }

    @Inject Logger logger;

    private HapiMessageConverterHelper() {}

    /**
     * Adds the `ETOR` code to any message provided
     *
     * @param messageBundle the in coming message in a FHIR bundle
     */
    public void addEtorTagToBundle(Bundle messageBundle) {
        var messageHeader = findOrInitializeMessageHeader(messageBundle);
        var meta = messageHeader.hasMeta() ? messageHeader.getMeta() : new Meta();

        var systemValue = "http://localcodes.org/ETOR";
        var codeValue = "ETOR";
        var displayValue = "Processed by ETOR";

        if (meta.getTag(systemValue, codeValue) == null) {
            meta.addTag(new Coding(systemValue, codeValue, displayValue));
        }

        messageHeader.setMeta(meta);
    }

    /**
     * Checks if the FHIR bundle has a messageHeader, and it creates one if it is missing
     *
     * @param bundle the in coming message in a FHIR bundle
     * @return returns existing MessageHeader resource or a newly created one
     */
    public static MessageHeader findOrInitializeMessageHeader(Bundle bundle) {
        var messageHeader =
                HapiHelper.resourcesInBundle(bundle, MessageHeader.class).findFirst().orElse(null);
        if (messageHeader == null) {
            messageHeader = new MessageHeader();
            bundle.addEntry(new Bundle.BundleEntryComponent().setResource(messageHeader));
        }
        return messageHeader;
    }

    /**
     * Finds the first patient in bundle resource or returns null
     *
     * @param bundle
     * @return Patient if found
     */
    public static Patient findPatientOrNull(Bundle bundle) {
        return HapiHelper.resourcesInBundle(bundle, Patient.class).findFirst().orElse(null);
    }

    /**
     * Finds all patient resources inside a given bundle
     *
     * @param bundle Bundle to check
     * @return Stream list of patients.
     */
    public static Stream<Patient> findAllPatients(Bundle bundle) {
        return HapiHelper.resourcesInBundle(bundle, Patient.class);
    }

    public static MessageHeader createOmlMessageHeader() {
        var messageHeader = new MessageHeader();

        messageHeader.setId(UUID.randomUUID().toString());

        messageHeader.setEvent(OML_CODING);

        var meta = new Meta();

        // Adding processing id of 'P'
        meta.addTag("http://terminology.hl7.org/CodeSystem/v2-0103", "P", "Production");

        messageHeader.setMeta(meta);

        messageHeader.setSource(
                new MessageHeader.MessageSourceComponent(
                                new UrlType("https://reportstream.cdc.gov/"))
                        .setName("CDC Trusted Intermediary"));

        return messageHeader;
    }

    public static ServiceRequest createServiceRequest(
            final Patient patient, final Date orderDateTime) {
        var serviceRequest = new ServiceRequest();

        serviceRequest.setId(UUID.randomUUID().toString());

        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);

        serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);

        serviceRequest.setCode(
                new CodeableConcept(
                        new Coding("http://loinc.org", "54089-8", "Newborn Screening Panel")));

        serviceRequest.addCategory(
                new CodeableConcept(
                        new Coding("http://snomed.info/sct", "108252007", "Laboratory procedure")));

        serviceRequest.setSubject(new Reference(patient));

        serviceRequest.setAuthoredOn(orderDateTime);

        return serviceRequest;
    }

    public static Provenance createProvenanceResource(Date orderDate) {
        var provenance = new Provenance();

        provenance.setId(UUID.randomUUID().toString());
        provenance.setRecorded(orderDate);
        provenance.setActivity(new CodeableConcept(OML_CODING));

        return provenance;
    }
}
