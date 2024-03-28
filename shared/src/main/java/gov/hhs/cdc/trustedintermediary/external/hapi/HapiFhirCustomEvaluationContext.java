package gov.hhs.cdc.trustedintermediary.external.hapi;

import ca.uhn.fhir.fhirpath.IFhirPathEvaluationContext;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Reference;

public class HapiFhirCustomEvaluationContext implements IFhirPathEvaluationContext {

    /**
     * @param theReference Id-based reference to the resource we're attempting to resolve.
     * @param theContext Internally converted resource version of theReference.
     * @return Reference resource
     */
    @Override
    public IBase resolveReference(@Nonnull IIdType theReference, @Nullable IBase theContext) {
        if (theContext != null) {
            return ((Reference) theContext).getResource();
        }
        return IFhirPathEvaluationContext.super.resolveReference(theReference, null);
    }
}
