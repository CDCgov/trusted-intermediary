package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

/**
 * This interface represents an HL7 message. It's used as a wrapper to decouple dependency on third
 * party libraries.
 *
 * @param <T> the type of the underlying message type
 */
public interface HL7Message<T> {
    T getMessage();
}
