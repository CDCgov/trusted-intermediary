package gov.hhs.cdc.trustedintermediary.hl7fhir;

import java.util.List;
import org.hl7.fhir.r4.model.Coding;

public class FhirValuesHelper {

    public static final List<Coding> CODING_LIST =
            List.of(
                    new Coding(
                            "http://terminology.hl7.org/CodeSystem/v3-RoleCode", "MTH", "mother"));

    public static final Coding OML_CODING =
            new Coding(
                    "http://terminology.hl7.org/CodeSystem/v2-0003",
                    "O21",
                    "OML - Laboratory order");
}
