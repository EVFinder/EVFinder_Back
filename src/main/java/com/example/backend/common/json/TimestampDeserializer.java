package com.example.backend.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.cloud.Timestamp;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

// 타임스탬프 역직렬화
public class TimestampDeserializer extends JsonDeserializer<Timestamp> {
    @Override
    public Timestamp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String isoString = p.getText(); // ISO8601 String
        Instant instant = Instant.parse(isoString); 
        return Timestamp.of(Date.from(instant));
    }
}
