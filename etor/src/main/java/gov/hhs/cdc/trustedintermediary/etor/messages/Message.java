package gov.hhs.cdc.trustedintermediary.etor.messages;

/**
 * Interface to wrap third-party messages
 *
 * @param <T>
 */
public interface Message<T> {
	T getUnderlyingMessage();

	String getFhirResourceId();

	String getPatientId();
}
