package gov.hhs.cdc.trustedintermediary.external.hapi;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Meta;

import gov.hhs.cdc.trustedintermediary.wrappers.Logger;

import javax.inject.Inject;

public class HapiMessageConverterHelper {

	private static final HapiMessageConverterHelper INSTANCE = new HapiMessageConverterHelper();

	public static HapiMessageConverterHelper getInstance() {
		return INSTANCE;
	}

	@Inject Logger logger;

	private HapiMessageConverterHelper() {}

	public void addEtorTag(Bundle messageBundle) {
		var messageHeaderOptional =
			HapiHelper.resourcesInBundle(messageBundle, MessageHeader.class).findFirst();
		if (messageHeaderOptional.isPresent()) {
			var messageHeader = messageHeaderOptional.get();
			var meta = messageHeader.hasMeta() ? messageHeader.getMeta() : new Meta();

			meta.addTag(new Coding("http://localcodes.org/ETOR", "ETOR", "Processed by ETOR"));
			messageHeader.setMeta(meta);
		} else {
			logger.logInfo("No MessageHeader found in the Bundle to add the ETOR processing tag.");
		}
	}
}
