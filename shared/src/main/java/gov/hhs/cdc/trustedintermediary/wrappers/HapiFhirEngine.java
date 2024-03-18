package gov.hhs.cdc.trustedintermediary.wrappers;

import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.ExpressionNode;
import org.hl7.fhir.r4.utils.FHIRPathEngine;

public interface HapiFhirEngine {
    ExpressionNode parsePath(String fhirPath);

    List<Base> evaluate(IBaseResource root, String expression);

    Boolean evaluateCondition(IBaseResource root, String expression) throws Exception;

    IWorkerContext getContext();

    FHIRPathEngine getEngine();
}
