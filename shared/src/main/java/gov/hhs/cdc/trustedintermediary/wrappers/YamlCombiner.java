package gov.hhs.cdc.trustedintermediary.wrappers;

import java.util.Set;

/** Combines YAML strings into one monolithic YAML string. */
public interface YamlCombiner {
    String combineYaml(Set<String> yamlStrings) throws YamlCombinerException;
}
