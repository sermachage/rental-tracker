package com.rental;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Smoke test: proves Maven + JUnit are wired up correctly. */
class AppTest {

    @Test
    void bannerHasExpectedText() {
        assertEquals("=== Rental tracker ===", App.banner());
    }
}