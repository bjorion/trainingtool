package org.jorion.trainingtool.repository;

import org.jorion.trainingtool.entity.Training;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("local")
public class TrainingRepositoryTest {

    @Autowired
    @SuppressWarnings("unused")
    private TestEntityManager em;

    @Autowired
    private ITrainingRepository repo;

    @Test
    void testFindAvailableTrainings() {

        LocalDate date = LocalDate.of(2021, 6, 1);
        List<Training> list = repo.findAvailableTrainings(date);
        assertFalse(list.isEmpty());
        for (Training t : list) {
            assertTrue(t.isEnabled());
            assertTrue(t.getEnabledFrom() == null || t.getEnabledFrom().isBefore(date));
            assertTrue(t.getEnabledUntil() == null || t.getEnabledUntil().isAfter(date));
        }
    }
}
