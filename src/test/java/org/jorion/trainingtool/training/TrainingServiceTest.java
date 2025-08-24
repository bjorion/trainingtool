package org.jorion.trainingtool.training;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.jorion.trainingtool.training.RandomTraining.buildTraining;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private ITrainingRepository mockTrainingRepository;

    @Test
    void testFindAvailableTrainingById_found() {

        var training = buildTraining().id(100L).build();
        var service = new TrainingService();
        ReflectionTestUtils.setField(service, "trainingRepository", mockTrainingRepository);
        when(mockTrainingRepository.findById(eq(100L))).thenReturn(Optional.of(training));
        assertEquals(100L, service.findAvailableTrainingById(100L).getId().longValue());
    }

    @Test
    void testFindAvailableTrainingById_notFound() {

        var service = new TrainingService();
        ReflectionTestUtils.setField(service, "trainingRepository", mockTrainingRepository);
        when(mockTrainingRepository.findById(eq(100L))).thenReturn(Optional.empty());
        assertNull(service.findAvailableTrainingById(100L));
    }

    @Test
    void testFindAvailableTrainingById_foundNotAvailable() {

        var oldDate = LocalDate.of(2000, 1, 1);
        var training = buildTraining().id(100L).enabledFrom(oldDate).enabledUntil(oldDate).build();
        var service = new TrainingService();
        ReflectionTestUtils.setField(service, "trainingRepository", mockTrainingRepository);
        when(mockTrainingRepository.findById(eq(100L))).thenReturn(Optional.of(training));
        assertNull(service.findAvailableTrainingById(100L));
    }

    @Test
    void testFindAllTrainings_availableOnly() {

        var service = new TrainingService();
        ReflectionTestUtils.setField(service, "trainingRepository", mockTrainingRepository);

        when(mockTrainingRepository.findAvailableTrainings(any())).thenReturn(new ArrayList<>());
        assertTrue(service.findAllTrainings(true).isEmpty());
        verify(mockTrainingRepository, times(1)).findAvailableTrainings(any());
    }

    @Test
    void testFindAllTrainings_availableOrNot() {

        var service = new TrainingService();
        ReflectionTestUtils.setField(service, "trainingRepository", mockTrainingRepository);

        when(mockTrainingRepository.findAll()).thenReturn(new ArrayList<>());
        assertTrue(service.findAllTrainings(false).isEmpty());
        verify(mockTrainingRepository, times(1)).findAll();
    }
}
