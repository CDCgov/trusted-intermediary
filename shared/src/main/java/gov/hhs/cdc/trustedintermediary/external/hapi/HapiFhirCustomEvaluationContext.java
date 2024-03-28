package gov.hhs.cdc.trustedintermediary.external.hapi;

import ca.uhn.fhir.fhirpath.IFhirPathEvaluationContext;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IIdType;

public class HapiFhirCustomEvaluationContext implements IFhirPathEvaluationContext {

    @Override
    public IBase resolveReference(@Nonnull IIdType theReference, @Nullable IBase theContext) {
        return IFhirPathEvaluationContext.super.resolveReference(theReference, theContext);
    }
}
