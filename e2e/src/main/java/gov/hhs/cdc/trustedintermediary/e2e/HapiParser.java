package gov.hhs.cdc.trustedintermediary.e2e;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhirpath.IFhirPath;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle;

public class HapiParser {
    private static final FhirContext CONTEXT = FhirContext.forR4();
    private static final IFhirPath PATH_ENGINE;

    static {
        PATH_ENGINE = CONTEXT.newFhirPath();
        PATH_ENGINE.setEvaluationContext(new HapiParserContext());
    }

    public static Bundle parse(final String fhirResource) {
        IParser resourceParser = CONTEXT.newJsonParser();
        return resourceParser.parseResource(Bundle.class, fhirResource);
    }

    public static String getStringFromFhirPath(IBaseResource resource, String expression) {
        var result = PATH_ENGINE.evaluateFirst(resource, expression, Base.class);
        return result.map(Base::primitiveValue).orElse("");
    }
}
