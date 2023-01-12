package gov.hhs.cdc.trustedintermediary.wrappers;

import java.util.Set;

public interface YamlCombiner {
    String combineYaml(Set<String> yamlStrings) throws YamlCombinerException;
}
