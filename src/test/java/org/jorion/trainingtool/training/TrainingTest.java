package org.jorion.trainingtool.training;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.jorion.trainingtool.training.RandomTraining.buildTraining;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingTest {

    @Test
    void testIsAvailable() {

        Training training;
        LocalDate now = LocalDate.now();
        LocalDate yesterday = now.minusDays(1);
        LocalDate tomorrow = now.plusDays(1);

        training = buildTraining().enabled(true).enabledFrom(null).enabledUntil(null).build();
        assertTrue(training.isAvailable());

        training = buildTraining().enabled(false).enabledFrom(null).enabledUntil(null).build();
        assertFalse(training.isAvailable());

        training = buildTraining().enabled(true).enabledFrom(now).enabledUntil(now).build();
        assertTrue(training.isAvailable());

        training = buildTraining().enabled(true).enabledFrom(yesterday).enabledUntil(yesterday).build();
        assertFalse(training.isAvailable());

        training = buildTraining().enabled(true).enabledFrom(yesterday).enabledUntil(null).build();
        assertTrue(training.isAvailable());

        training = buildTraining().enabled(true).enabledFrom(tomorrow).enabledUntil(tomorrow).build();
        assertFalse(training.isAvailable());

        training = buildTraining().enabled(true).enabledFrom(null).enabledUntil(tomorrow).build();
        assertTrue(training.isAvailable());
    }
}
