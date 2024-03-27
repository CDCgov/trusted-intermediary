package gov.hhs.cdc.trustedintermediary.external.hapi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.utils.FHIRPathEngine;

/** Concrete implementation that calls the Hapi FHIR library. */
public class HapiFhirImplementation implements HapiFhir {

    private static final HapiFhirImplementation INSTANCE = new HapiFhirImplementation();
    private static final FhirContext CONTEXT = FhirContext.forR4();
    private static final FHIRPathEngine PATH_ENGINE =
            new FHIRPathEngine(new HapiWorkerContext(CONTEXT, CONTEXT.getValidationSupport()));

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
     * Evaluate a FHIR Path expression for a given Resource to find if the expression has matches
     *
     * @param resource FHIR resource the evaluation starts from.
     * @param expression FHIR Path statement to run evaluations on.
     * @return True if the expression has at least one match for the given root, else false.
     */
    @Override
    public Boolean evaluateCondition(Object resource, String expression) {
        var node = PATH_ENGINE.parse(expression);

        var result =
                PATH_ENGINE.evaluateToBoolean(
                        (Resource) resource, (Resource) resource, (Resource) resource, node);

        //        var result =
        //                PATH_ENGINE.evaluateFirst((IBaseResource) resource, expression,
        // BooleanType.class);
        //        return result.map(BooleanType::booleanValue).orElse(false);
        return result;
    }
}
