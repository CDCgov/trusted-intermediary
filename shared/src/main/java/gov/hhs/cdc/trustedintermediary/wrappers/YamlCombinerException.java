package gov.hhs.cdc.trustedintermediary.wrappers;

/** Custom exception class use to catch YAML combining exceptions */
public class YamlCombinerException extends Exception {

    public YamlCombinerException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
