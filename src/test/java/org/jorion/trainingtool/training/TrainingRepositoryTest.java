package org.jorion.trainingtool.training;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("local")
public class TrainingRepositoryTest {

    @Autowired
    private ITrainingRepository repo;

    @Test
    void testFindAvailableTrainings() {

        var date = LocalDate.of(2021, 6, 1);
        var availableTrainings = repo.findAvailableTrainings(date);
        assertFalse(availableTrainings.isEmpty());
        for (Training t : availableTrainings) {
            assertTrue(t.isEnabled());
            assertTrue(t.getEnabledFrom() == null || t.getEnabledFrom().isBefore(date));
            assertTrue(t.getEnabledUntil() == null || t.getEnabledUntil().isAfter(date));
        }
    }
}
