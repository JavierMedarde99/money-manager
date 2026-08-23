package com.money.manager.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RateLimiterServiceTest {

    private final RateLimiterService rateLimiterService = new RateLimiterService();

    @Test
    void allowsUpToMaxRequestsThenBlocks() {
        String key = "login:1.2.3.4:agent";

        for (int i = 0; i < 5; i++) {
            assertThat(rateLimiterService.isAllowed(key, 5, 60_000))
                    .as("request %d should be allowed", i + 1)
                    .isTrue();
        }

        assertThat(rateLimiterService.isAllowed(key, 5, 60_000)).isFalse();
    }

    @Test
    void tracksClientsIndependently() {
        for (int i = 0; i < 5; i++) {
            rateLimiterService.isAllowed("login:1.2.3.4:agentA", 5, 60_000);
        }
        assertThat(rateLimiterService.isAllowed("login:1.2.3.4:agentA", 5, 60_000)).isFalse();
        assertThat(rateLimiterService.isAllowed("login:1.2.3.4:agentB", 5, 60_000)).isTrue();
    }

    @Test
    void separateKeysForLoginAndRegister() {
        for (int i = 0; i < 10; i++) {
            rateLimiterService.isAllowed("login:1.2.3.4:agent", 5, 60_000);
        }
        assertThat(rateLimiterService.isAllowed("login:1.2.3.4:agent", 5, 60_000)).isFalse();
        assertThat(rateLimiterService.isAllowed("register:1.2.3.4:agent", 10, 60_000)).isTrue();
    }

    @Test
    void registerAllowsMoreRequestsThanLogin() {
        for (int i = 0; i < 5; i++) {
            rateLimiterService.isAllowed("login:1.2.3.4:agent", 5, 60_000);
        }
        assertThat(rateLimiterService.isAllowed("login:1.2.3.4:agent", 5, 60_000)).isFalse();

        for (int i = 0; i < 10; i++) {
            assertThat(rateLimiterService.isAllowed("register:1.2.3.4:agent", 10, 60_000))
                    .as("register request %d should be allowed", i + 1)
                    .isTrue();
        }
        assertThat(rateLimiterService.isAllowed("register:1.2.3.4:agent", 10, 60_000)).isFalse();
    }
}
