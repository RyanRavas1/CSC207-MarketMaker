package com.marketmaker.config;

import com.marketmaker.config.exceptions.MissingEnvironmentVariableException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class EnvLoaderTest {

    @Test
    void getReturnsEnvironmentVariableOrDotEnvVariable() {
        // Test System.getenv fallback if PATH exists
        String path = System.getenv("PATH");
        if (path != null) {
            assertEquals(path, EnvLoader.get("PATH"));
        }
    }

    @Test
    void throwsMissingEnvironmentVariableExceptionWhenKeyNotFound() {
        MissingEnvironmentVariableException ex = assertThrows(
                MissingEnvironmentVariableException.class,
                () -> EnvLoader.get("NON_EXISTENT_ENV_KEY_12345_XYZ")
        );
        assertTrue(ex.getMessage().contains("NON_EXISTENT_ENV_KEY_12345_XYZ"));
    }

    @Test
    void privateConstructorInvocation() throws Exception {
        Constructor<EnvLoader> constructor = EnvLoader.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        EnvLoader instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
