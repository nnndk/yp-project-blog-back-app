package ru.yandex.practicum.unit.service.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.service.storage.name.UniqueFileNameService;
import ru.yandex.practicum.unit.configuration.UUIDUniqueFileNameServiceTestConfig;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(UUIDUniqueFileNameServiceTestConfig.class)
public class UUIDUniqueFileNameServiceTest {

    private static final String UUID_V4_REGEX =
            "[a-f0-9]{8}(?:-[a-f0-9]{4}){4}[a-f0-9]{8}";

    private static final Set<String> generatedNames = new HashSet<>();


    @Autowired
    private UniqueFileNameService service;


    @BeforeAll
    static void clear() {
        generatedNames.clear();
    }


    @Nested
    class GenerateFileName {

        @RepeatedTest(100)
        void returnsCorrectUUIDUniqueName() {
            String name = service.generateFileName();

            assertThat(service.generateFileName())
                    .hasSize(36)
                    .matches(UUID_V4_REGEX);
            assertThat(generatedNames.add(name))
                    .as("duplicate UUID generated: %s", name)
                    .isTrue();
        }
    }
}
