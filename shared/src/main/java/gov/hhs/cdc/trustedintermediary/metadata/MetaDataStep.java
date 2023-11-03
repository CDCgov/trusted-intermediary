package gov.hhs.cdc.trustedintermediary.metadata;

/** List of steps where we log metadata events */
public enum MetaDataStep {
    RECEIVED_FROM_REPORT_STREAM,

    ORDER_CONVERTED_TO_OML,

    CONTACT_SECTION_ADDED_TO_PATIENT,

    SENT_TO_REPORT_STREAM
}
