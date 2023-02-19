package org.jorion.trainingtool.entity;

import org.jorion.trainingtool.common.EntityUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrainingTest {

    @Test
    void testIsAvailable() {

        Training t;
        LocalDate now = LocalDate.now();
        LocalDate yesterday = now.minusDays(1);
        LocalDate tomorrow = now.plusDays(1);

        t = EntityUtils.createTrainingBuilder().enabled(true).enabledFrom(null).enabledUntil(null).build();
        assertTrue(t.isAvailable());

        t = EntityUtils.createTrainingBuilder().enabled(false).enabledFrom(null).enabledUntil(null).build();
        assertFalse(t.isAvailable());

        t = EntityUtils.createTrainingBuilder().enabled(true).enabledFrom(now).enabledUntil(now).build();
        assertTrue(t.isAvailable());

        t = EntityUtils.createTrainingBuilder().enabled(true).enabledFrom(yesterday).enabledUntil(yesterday).build();
        assertFalse(t.isAvailable());

        t = EntityUtils.createTrainingBuilder().enabled(true).enabledFrom(yesterday).enabledUntil(null).build();
        assertTrue(t.isAvailable());

        t = EntityUtils.createTrainingBuilder().enabled(true).enabledFrom(tomorrow).enabledUntil(tomorrow).build();
        assertFalse(t.isAvailable());

        t = EntityUtils.createTrainingBuilder().enabled(true).enabledFrom(null).enabledUntil(tomorrow).build();
        assertTrue(t.isAvailable());
    }
}
