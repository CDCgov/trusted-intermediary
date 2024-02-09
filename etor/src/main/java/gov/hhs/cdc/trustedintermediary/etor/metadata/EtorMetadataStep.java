package gov.hhs.cdc.trustedintermediary.etor.metadata;

/** Etor specific steps from the general shared metadata steps. Keep enum in alphabetical order */
public enum EtorMetadataStep {
    CONTACT_SECTION_ADDED_TO_PATIENT,
    ETOR_PROCESSING_TAG_ADDED_TO_MESSAGE_HEADER,
    ORDER_CONVERTED_TO_OML,
    RECEIVED_FROM_REPORT_STREAM,
    SENT_TO_REPORT_STREAM
}
