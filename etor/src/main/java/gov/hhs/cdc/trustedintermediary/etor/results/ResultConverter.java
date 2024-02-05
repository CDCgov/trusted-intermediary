package gov.hhs.cdc.trustedintermediary.etor.results;

import gov.hhs.cdc.trustedintermediary.etor.orders.Order;

public interface ResultConverter {
	Result<?> addEtorProcessingTag(Result<?> message);
}
