package gov.hhs.cdc.trustedintermediary.external.hapi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhirpath.IFhirPath;
import ca.uhn.fhir.parser.IParser;
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.BooleanType;

/** Concrete implementation that calls the Hapi FHIR library. */
public class HapiFhirImplementation implements HapiFhir {

    private static final HapiFhirImplementation INSTANCE = new HapiFhirImplementation();
    private static final FhirContext CONTEXT = FhirContext.forR4();

    private static final IFhirPath PATH_ENGINE = createEngine();

    private HapiFhirImplementation() {}

    public static HapiFhirImplementation getInstance() {
        return INSTANCE;
    }

    /**
     * Creates FHIRPath engine with a custom evaluation context.
     *
     * @return Configured engine.
     */
    private static IFhirPath createEngine() {
        var engine = CONTEXT.newFhirPath();
        engine.setEvaluationContext(new HapiFhirCustomEvaluationContext());
        return engine;
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
     * Evaluate a FHIR Path expression for a given Resource to find if the expression has matches
     *
     * @param resource FHIR resource the evaluation starts from.
     * @param expression FHIR Path statement to run evaluations on.
     * @return True if the expression has at least one match for the given root, else false.
     */
    @Override
    public Boolean evaluateCondition(Object resource, String expression) {
        var result =
                PATH_ENGINE.evaluateFirst((IBaseResource) resource, expression, BooleanType.class);
        return result.map(BooleanType::booleanValue).orElse(false);
    }

    /**
     * Retrieves a string result by evaluating a specified FHIRPath expression against a given FHIR
     * resource. This method simplifies accessing textual data within FHIR resources by directly
     * returning the string representation of the first matching element found by the FHIRPath
     * expression. If no match is found, or the first result cannot be represented as a string, an
     * empty string is returned.
     *
     * @param resource The FHIR resource upon which the FHIRPath expression will be evaluated. This
     *     resource acts as the context for the FHIRPath evaluation.
     * @param expression The FHIRPath expression to be evaluated against the resource. The
     *     expression should be crafted to select textual data or elements that can be represented
     *     as text.
     * @return The string representation of the first matching result of the FHIRPath expression
     *     evaluation. Returns an empty string if no matching element is found, or if the first
     *     result cannot be represented as a string.
     */
    @Override
    public String getStringFromFhirPath(Object resource, String expression) {
        var result = PATH_ENGINE.evaluateFirst((IBaseResource) resource, expression, Base.class);
        return result.map(Base::primitiveValue).orElse("");
    }
}
