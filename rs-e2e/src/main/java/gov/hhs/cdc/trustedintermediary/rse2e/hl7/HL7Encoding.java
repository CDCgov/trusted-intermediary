package gov.hhs.cdc.trustedintermediary.rse2e.hl7;

import java.util.Map;

/** The HL7Encoding class represents the encoding characters used in an HL7 message. */
public record HL7Encoding(Map<String, Character> characters) {
    public static final String DEFAULT_SEGMENT_DELIMITER = "\n";
    public static final char DEFAULT_FIELD_DELIMITER = '|';
    public static final char DEFAULT_COMPONENT_DELIMITER = '^';
    public static final char DEFAULT_REPETITION_DELIMITER = '~';
    public static final char DEFAULT_ESCAPE_CHARACTER = '\\';
    public static final char DEFAULT_SUBCOMPONENT_DELIMITER = '&';

    public static final String FIELD_DELIMITER_NAME = "field";
    public static final String COMPONENT_DELIMITER_NAME = "component";
    public static final String REPETITION_DELIMITER_NAME = "repetition";
    public static final String ESCAPE_CHARACTER_NAME = "escape";
    public static final String SUBCOMPONENT_DELIMITER_NAME = "subcomponent";

    private static final Map<String, Character> DEFAULT_CHARACTERS =
            Map.of(
                    FIELD_DELIMITER_NAME, DEFAULT_FIELD_DELIMITER,
                    COMPONENT_DELIMITER_NAME, DEFAULT_COMPONENT_DELIMITER,
                    REPETITION_DELIMITER_NAME, DEFAULT_REPETITION_DELIMITER,
                    ESCAPE_CHARACTER_NAME, DEFAULT_ESCAPE_CHARACTER,
                    SUBCOMPONENT_DELIMITER_NAME, DEFAULT_SUBCOMPONENT_DELIMITER);

    public static HL7Encoding defaultEncoding() {
        return new HL7Encoding(DEFAULT_CHARACTERS);
    }

    public static HL7Encoding fromEncodingField(String encodingField) {
        if (encodingField == null || encodingField.isEmpty()) {
            return defaultEncoding();
        }

        return new HL7Encoding(
                Map.of(
                        FIELD_DELIMITER_NAME, DEFAULT_FIELD_DELIMITER,
                        COMPONENT_DELIMITER_NAME, encodingField.charAt(0),
                        REPETITION_DELIMITER_NAME, encodingField.charAt(1),
                        ESCAPE_CHARACTER_NAME, encodingField.charAt(2),
                        SUBCOMPONENT_DELIMITER_NAME, encodingField.charAt(3)));
    }

    public char getCharacter(String type) {
        return characters.get(type);
    }

    public char getEscapeCharacter() {
        return characters.get(ESCAPE_CHARACTER_NAME);
    }

    public char getFieldDelimiter() {
        return characters.get(FIELD_DELIMITER_NAME);
    }

    public char getComponentDelimiter() {
        return characters.get(COMPONENT_DELIMITER_NAME);
    }

    public char getRepetitionDelimiter() {
        return characters.get(REPETITION_DELIMITER_NAME);
    }

    public char getSubcomponentDelimiter() {
        return characters.get(SUBCOMPONENT_DELIMITER_NAME);
    }

    public char[] getOrderedDelimiters() {
        return new char[] {
            getCharacter(FIELD_DELIMITER_NAME),
            getCharacter(COMPONENT_DELIMITER_NAME),
            getCharacter(REPETITION_DELIMITER_NAME),
            getCharacter(SUBCOMPONENT_DELIMITER_NAME)
        };
    }
}
