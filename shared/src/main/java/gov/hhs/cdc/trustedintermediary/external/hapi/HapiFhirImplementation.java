package gov.hhs.cdc.trustedintermediary.external.hapi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.conformance.ProfileUtilities;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.ExpressionNode;
import org.hl7.fhir.r4.utils.FHIRPathEngine;

/** Concrete implementation that calls the Hapi FHIR library. */
public class HapiFhirImplementation implements HapiFhir {

    private static final HapiFhirImplementation INSTANCE = new HapiFhirImplementation();

    private static final FhirContext CONTEXT = FhirContext.forR4();
    private final FHIRPathEngine pathEngine =
            new FHIRPathEngine(
                    (IWorkerContext) CONTEXT, (ProfileUtilities) CONTEXT.getValidationSupport());

    private HapiFhirImplementation() {}

    public static HapiFhirImplementation getInstance() {
        return INSTANCE;
    }

    @Override
    public <T extends IBaseResource> T parseResource(
            final String fhirResource, final Class<T> clazz) throws FhirParseException {
        IParser resourceParser = CONTEXT.newJsonParser();

        try {
            return resourceParser.parseResource(clazz, fhirResource);
        } catch (Exception e) {
            throw new FhirParseException(
                    "An error occurred while parsing the payload, make sure the payload is not empty and it has the correct format.",
                    e);
        }
    }

    @Override
    public String encodeResourceToJson(Object resource) {
        IParser encodeResourceParser = CONTEXT.newJsonParser();
        return encodeResourceParser.encodeResourceToString((IBaseResource) resource);
    }

    public ExpressionNode parsePath(String fhirPath) {
        if (fhirPath.isBlank()) {
            return null;
        }
        return pathEngine.parse(fhirPath);
    }

    public Boolean evaluateCondition(IBaseResource root, String expression) throws Exception {
        var expressionNode = parsePath(expression);
        var base = (Base) root;

        List<Base> value;
        if (expressionNode == null) {
            value = new ArrayList<>();
        } else {
            value = pathEngine.evaluate(base, expressionNode);
        }

        boolean retVal;
        if (value.size() == 1 && value.get(0).isBooleanPrimitive()) {
            retVal =
                    value.get(0)
                            .castToBoolean(base)
                            .booleanValue(); // not sure if resource is the right param here...
        } else if (value.isEmpty()) {
            // The FHIR utilities that test for booleans only return one if the resource exists
            // if the resource does not exist, they return []
            // for the purposes of the evaluating a schema condition that is the same as being false
            retVal = false;
        } else {
            throw new Exception("add here");
            // throw new FhirParseException("FHIR Path expression did not evaluate to a boolean
            // type: $expression", new Exception());
        }

        return retVal;
    }
}
