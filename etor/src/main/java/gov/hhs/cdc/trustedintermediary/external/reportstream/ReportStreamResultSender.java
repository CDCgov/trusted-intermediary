package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageSender;
import gov.hhs.cdc.trustedintermediary.etor.messages.UnableToSendMessageException;
import gov.hhs.cdc.trustedintermediary.etor.results.Result;
import java.util.Optional;

public class ReportStreamResultSender implements MessageSender<Result<?>> {
    @Override
    public Optional<String> send(Result<?> result) throws UnableToSendMessageException {
        // todo: implement in #616
        return Optional.empty();
    }
}
