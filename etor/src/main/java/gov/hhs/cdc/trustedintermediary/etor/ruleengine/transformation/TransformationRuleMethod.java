package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation;

import java.util.Map;

public record TransformationRuleMethod(String name, Map<String, String> args) {}
