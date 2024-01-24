package gov.hhs.cdc.trustedintermediary.etor.results;

import gov.hhs.cdc.trustedintermediary.etor.message.UnableToSendMessageException;
import java.util.Optional;

public interface ResultSender {
    Optional<String> sendResult(Result<?> result) throws UnableToSendMessageException;
}
