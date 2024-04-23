package gov.hhs.cdc.trustedintermediary.e2e;

import ca.uhn.fhir.fhirpath.IFhirPathEvaluationContext;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Reference;

/** Custom evaluation context for FHIRPath engine. */
public class HapiParserContext implements IFhirPathEvaluationContext {

    /**
     * When a FHIR path includes the "resolve()" method, this function is called to parse that into
     * a Resource.
     *
     * @param theReference Id-based reference to the resource we're attempting to resolve.
     * @param theContext Internally converted resource version of theReference.
     * @return Reference resource if available, else null.
     */
    @Override
    public IBase resolveReference(@Nonnull IIdType theReference, @Nullable IBase theContext) {
        if (theContext != null) {
            if (theContext.getClass() == Reference.class) {
                return ((Reference) theContext).getResource();
            }
            return theContext;
        }
        return IFhirPathEvaluationContext.super.resolveReference(theReference, null);
    }
}
