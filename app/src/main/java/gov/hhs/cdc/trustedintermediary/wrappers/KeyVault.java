package gov.hhs.cdc.trustedintermediary.wrappers;

/** This interface provides a skeleton for any key-vault, ex: Azure key vault */
public interface KeyVault {

    String getKey(String secretName);
}
