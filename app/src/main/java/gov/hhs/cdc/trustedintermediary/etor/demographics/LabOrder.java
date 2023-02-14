package gov.hhs.cdc.trustedintermediary.etor.demographics;
/*
   Interface to wrap a third-party lab order class (Ex: Hapi FHIR Bundle)
*/
public interface LabOrder<T> {
    T getUnderlyingOrder();
}
