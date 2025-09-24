package com.example.backend.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.cloud.Timestamp;

import java.io.IOException;
import java.time.Instant;

// 타임스탬프 직렬화
public class TimestampSerializer extends JsonSerializer<Timestamp> {
    @Override
    public void serialize(Timestamp value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null) {
            // Firestore Timestamp → ISO8601 문자열
            Instant instant = value.toDate().toInstant();
            gen.writeString(instant.toString()); // 예: 2025-09-23T12:00:00Z
        } else {
            gen.writeNull();
        }
    }
}
