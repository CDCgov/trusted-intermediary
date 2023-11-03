package gov.hhs.cdc.trustedintermediary.etor.metadata;

import gov.hhs.cdc.trustedintermediary.metadata.MetaDataStep;

public enum EtorMetaDataStep implements MetaDataStep {
    RECEIVED_FROM_REPORT_STREAM,

    ORDER_CONVERTED_TO_OML,

    CONTACT_SECTION_ADDED_TO_PATIENT,

    SENT_TO_REPORT_STREAM
}
