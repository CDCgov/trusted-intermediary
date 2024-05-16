package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import java.util.Map;

/**
 * Represents a transformation rule method.
 *
 * @param name The name of the transformation.
 * @param args The arguments to pass to the transformation method.
 */
public record TransformationRuleMethod(String name, Map<String, String> args) {}
