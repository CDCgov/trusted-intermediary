package gov.hhs.cdc.trustedintermediary.rse2e.ruleengine;

import java.util.Map;

/**
 * Represents a transformation rule method.
 *
 * @param name The name of the assertion.
 * @param args The arguments to pass to the assertion method.
 */
public record AssertionRuleMethod(String name, Map<String, String> args) {}
