package gov.hhs.cdc.trustedintermediary.rse2e.external.hapi;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v251.segment.MSH;
import gov.hhs.cdc.trustedintermediary.wrappers.HealthData;

/**
 * Represents a HAPI HL7 message that implements the HealthData interface. This class provides a
 * wrapper around the HAPI Message object.
 */
public class HapiHL7Message implements HealthData<Message> {

    protected final Message underlyingData;

    public HapiHL7Message(Message innerResource) {
        this.underlyingData = innerResource;
    }

    @Override
    public Message getUnderlyingData() {
        return underlyingData;
    }

    @Override
    public String getIdentifier() {
        try {
            MSH mshSegment = (MSH) underlyingData.get("MSH");
            return mshSegment.getMessageControlID().getValue();
        } catch (HL7Exception e) {
            return null;
        }
    }
}
