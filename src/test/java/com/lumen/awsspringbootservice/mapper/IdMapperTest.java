package com.lumen.awsspringbootservice.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {
                IdMapperImpl.class
        },
        properties = {"spring.profiles.active=test"}
)
@DisplayName("IdMapper Unit Tests")
class IdMapperTest {

    @Autowired
    private IdMapper idMapper;

    @Nested
    @DisplayName("String to UUID Mapping")
    class StringToUuidMapping {

        @Test
        @DisplayName("Should map valid String to UUID")
        void shouldMapStringToUuid() {
            // given
            UUID expected = UUID.randomUUID();
            String uuidStr = expected.toString();

            // when
            UUID result = idMapper.map(uuidStr);

            // then
            assertNotNull(result);
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Should return null when input String is null")
        void shouldReturnNullWhenStringIsNull() {
            // when
            UUID result = idMapper.map((String) null);

            // then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("UUID to String Mapping")
    class UuidToStringMapping {

        @Test
        @DisplayName("Should map valid UUID to String")
        void shouldMapUuidToString() {
            // given
            UUID uuid = UUID.randomUUID();

            // when
            String result = idMapper.map(uuid);

            // then
            assertNotNull(result);
            assertEquals(uuid.toString(), result);
        }

        @Test
        @DisplayName("Should return null when input UUID is null")
        void shouldReturnNullWhenUuidIsNull() {
            // when
            String result = idMapper.map((UUID) null);

            // then
            assertNull(result);
        }
    }
}