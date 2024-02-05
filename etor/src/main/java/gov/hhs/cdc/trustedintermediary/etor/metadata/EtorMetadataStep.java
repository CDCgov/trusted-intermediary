package gov.hhs.cdc.trustedintermediary.etor.metadata;

/** Etor specific steps from the general shared metadata steps */
public enum EtorMetadataStep {
    RECEIVED_FROM_REPORT_STREAM,

    ORDER_CONVERTED_TO_OML,

    CONTACT_SECTION_ADDED_TO_PATIENT,

    SENT_TO_REPORT_STREAM,
    ETOR_PROCESSING_TAG_ADDED_TO_MESSAGE_HEADER
}
