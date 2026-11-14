package com.alumnisearch.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Flexible Jackson deserializer for passoutYear that gracefully handles:
 * - Integer numeric values: e.g. 2020
 * - Numeric strings: e.g. "2020"
 * - Placeholders / blanks: e.g. "OPTIONAL_PASSOUT_YEAR", "", "null", null -> null
 */
public class PassoutYearDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();

        if (token == JsonToken.VALUE_NUMBER_INT) {
            return p.getIntValue();
        }

        if (token == JsonToken.VALUE_STRING) {
            String text = p.getText();
            if (text == null) {
                return null;
            }
            String trimmed = text.trim();
            if (trimmed.isEmpty()
                    || trimmed.equalsIgnoreCase("null")
                    || trimmed.equalsIgnoreCase("OPTIONAL_PASSOUT_YEAR")
                    || trimmed.equalsIgnoreCase("optional")) {
                return null;
            }

            try {
                return Integer.parseInt(trimmed);
            } catch (NumberFormatException ex) {
                // Return null if non-numeric string placeholder was supplied
                return null;
            }
        }

        if (token == JsonToken.VALUE_NULL) {
            return null;
        }

        return null;
    }
}
