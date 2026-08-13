package com.money.manager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class NoSecretsInConfigTest {

    private static final String OLD_JWT_KEY = "9a8b7c6d5e4f3g2h1i0j9k8l7m6n5o4p3q2r1s0t";

    @Test
    void propertiesMustNotContainPlainTextSecrets() throws IOException {
        try (Stream<Path> files = Files.list(Path.of("src/main/resources"))) {
            files.filter(path -> path.toString().endsWith(".properties")).forEach(this::assertNoSecrets);
        }
    }

    private void assertNoSecrets(Path file) {
        String content;
        try {
            content = Files.readString(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertFalse(content.contains(OLD_JWT_KEY),
                file + " contains the compromised JWT key in plain text");
        assertFalse(content.contains("password=postgres"),
                file + " contains the default DB password in plain text");
        assertFalse(content.matches("(?s).*jwt\\.key=[^$].*"),
                file + " defines jwt.key with a hardcoded value instead of an environment variable");

        content.lines()
                .filter(line -> line.startsWith("jwt.key="))
                .forEach(line -> assertTrue(line.contains("${JWT_KEY}"),
                        file + " must reference jwt.key from the JWT_KEY environment variable"));
    }
}
