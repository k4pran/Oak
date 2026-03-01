package io.ryanjames.oak.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.awt.Color;
import java.io.IOException;

public class ColorDeserializer extends JsonDeserializer<Color> {

    @Override
    public Color deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        String value = p.getValueAsString();

        if (value == null || value.isBlank()) {
            return null;
        }

        if (value.startsWith("#")) {
            value = value.substring(1);
        }

        long hex = Long.parseLong(value, 16);

        if (value.length() == 6) {
            return new Color((int) hex);
        } else if (value.length() == 8) {
            return new Color(
                    (int)((hex >> 16) & 0xFF),
                    (int)((hex >> 8) & 0xFF),
                    (int)(hex & 0xFF),
                    (int)((hex >> 24) & 0xFF)
            );
        }

        throw new IOException("Invalid color format: #" + value);
    }
}
