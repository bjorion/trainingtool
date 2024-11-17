package org.jorion.trainingtool.training;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingMapperTest {

    private static final TrainingMapper MAPPER = TrainingMapper.INSTANCE;

    @Test
    void testUpdateTraining() {

        var srcTraining = Training.builder().id(1L).enabled(true).build();
        var dstTraining = new Training();
        MAPPER.updateTraining(srcTraining, dstTraining);
        assertNull(dstTraining.getId());
        assertTrue(dstTraining.isEnabled());
    }
}
