package org.jorion.trainingtool.mapper;

import org.jorion.trainingtool.entity.Training;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrainingMapperTest {

    private static final TrainingMapper MAPPER = TrainingMapper.INSTANCE;

    @Test
    void testUpdateTraining() {

        Training src = Training.builder().id(1L).enabled(true).build();
        Training dst = new Training();
        MAPPER.updateTraining(src, dst);
        assertNull(dst.getId());
        assertTrue(dst.isEnabled());
    }
}
