package gov.hhs.cdc.trustedintermediary.metadata;



/**
 * List of steps where we log metadata events
 */

//TODO: Double check below steps
public enum MetaDataStep {
    RECEIVED_FROM_REPORT_STREAMS,
    SENT_TO_REPORT_STREAM,
    SENT_TO_PHL
}
