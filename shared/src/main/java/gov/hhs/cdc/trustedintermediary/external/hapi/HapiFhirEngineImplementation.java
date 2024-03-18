package gov.hhs.cdc.trustedintermediary.external.hapi;

import ca.uhn.fhir.context.FhirContext;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhirEngine;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.ExpressionNode;
import org.hl7.fhir.r4.utils.FHIRPathEngine;

public class HapiFhirEngineImplementation implements HapiFhirEngine {
    private final IWorkerContext context;

    private final FHIRPathEngine pathEngine;

    public HapiFhirEngineImplementation() {
        // FhirContext doesn't use IWorkerContext and needs to be wrapped in a HapiWorkerContext
        var fhirContext = FhirContext.forR4();
        context = new HapiWorkerContext(fhirContext, fhirContext.getValidationSupport());

        pathEngine = new FHIRPathEngine(context);
    }

    public ExpressionNode parsePath(String fhirPath) {
        if (fhirPath.isBlank()) {
            return null;
        }
        return pathEngine.parse(fhirPath);
    }

    public List<Base> evaluate(IBaseResource root, String expression) {
        var expressionNode = parsePath(expression);
        var base = (Base) root;

        List<Base> retVal;
        if (expressionNode == null) {
            retVal = new ArrayList<>();
        } else {
            try {
                retVal = pathEngine.evaluate(base, expressionNode);
            } catch (Exception e) {
                // log exception
                retVal = new ArrayList<>();
            }
        }

        return retVal;
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
            retVal = base.castToBoolean(value.get(0)).booleanValue();
        } else if (value.isEmpty()) {
            // The FHIR utilities that test for booleans only return one if the resource exists
            // if the resource does not exist, they return []
            // for the purposes of the evaluating a schema condition that is the same as being false
            retVal = false;
        } else {
            throw new Exception(
                    "add here"); // @todo consider defaulting to false and just logging the error
            // throw new FhirParseException("FHIR Path expression did not evaluate to a boolean
            // type: $expression", new Exception());
        }

        return retVal;
    }

    public IWorkerContext getContext() {
        return context;
    }

    public FHIRPathEngine getEngine() {
        return pathEngine;
    }
}
