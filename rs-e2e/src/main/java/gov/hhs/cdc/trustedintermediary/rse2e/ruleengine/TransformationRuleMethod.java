package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

import java.util.Map;

/**
 * Represents a transformation rule method.
 *
 * @param name The name of the transformation.
 * @param args The arguments to pass to the transformation method.
 */
public record TransformationRuleMethod(String name, Map<String, String> args) {}
