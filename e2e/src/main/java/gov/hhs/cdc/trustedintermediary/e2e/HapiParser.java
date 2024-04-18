package gov.hhs.cdc.trustedintermediary.e2e;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhirpath.IFhirPath;

public class HapiParser {
    private final IFhirPath engine;
    private static final FhirContext CONTEXT = FhirContext.forR4();

    public HapiParser() {
        engine = CONTEXT.newFhirPath();
        engine.setEvaluationContext(new HapiParserContext());
    }
}
