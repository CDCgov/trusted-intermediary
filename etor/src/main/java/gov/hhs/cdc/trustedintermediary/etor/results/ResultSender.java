package gov.hhs.cdc.trustedintermediary.etor.results;

import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import java.util.Optional;

/** Interface for sending a lab order. */
public interface ResultSender {
    Optional<String> send(Result<?> result) throws UnableToSendMessageException;
}
