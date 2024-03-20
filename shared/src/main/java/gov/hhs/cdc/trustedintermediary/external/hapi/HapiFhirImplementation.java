package gov.hhs.cdc.trustedintermediary.external.hapi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhirpath.IFhirPath;
import ca.uhn.fhir.parser.IParser;
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.BooleanType;

/** Concrete implementation that calls the Hapi FHIR library. */
public class HapiFhirImplementation implements HapiFhir {

    private static final HapiFhirImplementation INSTANCE = new HapiFhirImplementation();
    private static final FhirContext CONTEXT = FhirContext.forR4();
    private static final IFhirPath pathEngine = CONTEXT.newFhirPath();

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

    /**
     * Encode resource to JSON string.
     *
     * @param resource Object to encode into a string.
     * @return String-encoded resource.
     */
    @Override
    public String encodeResourceToJson(Object resource) {
        IParser encodeResourceParser = CONTEXT.newJsonParser();
        return encodeResourceParser.encodeResourceToString((IBaseResource) resource);
    }

    /**
     * Evaluate a FHIR Path expression for a given Resource to find matching elements.
     *
     * @param root FHIR resource the evaluation starts from.
     * @param expression FHIR Path statement to run evaluations on.
     * @return A list of matching Resources within root for the given expression.
     */
    @Override
    public List<Base> evaluate(IBaseResource root, String expression) {
        return pathEngine.evaluate(root, expression, Base.class);
    }

    /**
     * Evaluate a FHIR Path expression for a given Resource to find if the expression has matches
     *
     * @param root FHIR resource the evaluation starts from.
     * @param expression FHIR Path statement to run evaluations on.
     * @return True if the expression has at least one match for the given root, else false.
     */
    @Override
    public Boolean evaluateCondition(IBaseResource root, String expression) {
        var result = pathEngine.evaluateFirst(root, expression, BooleanType.class);
        return result.map(BooleanType::booleanValue).orElse(false);
    }
}
