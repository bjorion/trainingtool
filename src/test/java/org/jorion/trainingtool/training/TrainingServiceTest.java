package org.jorion.trainingtool.training;

import org.jorion.trainingtool.common.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceTest {

    @Mock
    private ITrainingRepository mockTrainingRepository;

    @Test
    void testFindAvailableTrainingById_found() {

        Training training = EntityUtils.createTrainingBuilder().id(100L).build();
        TrainingService service = new TrainingService();
        ReflectionTestUtils.setField(service, "trainingRepository", mockTrainingRepository);
        when(mockTrainingRepository.findById(eq(100L))).thenReturn(Optional.of(training));
        assertEquals(100L, service.findAvailableTrainingById(100L).getId().longValue());
    }

    @Test
    void testFindAvailableTrainingById_notFound() {

        TrainingService service = new TrainingService();
        ReflectionTestUtils.setField(service, "trainingRepository", mockTrainingRepository);
        when(mockTrainingRepository.findById(eq(100L))).thenReturn(Optional.empty());
        assertNull(service.findAvailableTrainingById(100L));
    }

    @Test
    void testFindAvailableTrainingById_foundNotAvailable() {

        LocalDate old = LocalDate.of(2000, 1, 1);
        Training training = EntityUtils.createTrainingBuilder().id(100L).enabledFrom(old).enabledUntil(old).build();
        TrainingService service = new TrainingService();
        ReflectionTestUtils.setField(service, "trainingRepository", mockTrainingRepository);
        when(mockTrainingRepository.findById(eq(100L))).thenReturn(Optional.of(training));
        assertNull(service.findAvailableTrainingById(100L));
    }

    @Test
    void testFindAllTrainings_availableOnly() {

        TrainingService service = new TrainingService();
        ReflectionTestUtils.setField(service, "trainingRepository", mockTrainingRepository);

        when(mockTrainingRepository.findAvailableTrainings(any())).thenReturn(new ArrayList<>());
        assertTrue(service.findAllTrainings(true).isEmpty());
        verify(mockTrainingRepository, times(1)).findAvailableTrainings(any());
    }

    @Test
    void testFindAllTrainings_availableOrNot() {

        TrainingService service = new TrainingService();
        ReflectionTestUtils.setField(service, "trainingRepository", mockTrainingRepository);

        when(mockTrainingRepository.findAll()).thenReturn(new ArrayList<>());
        assertTrue(service.findAllTrainings(false).isEmpty());
        verify(mockTrainingRepository, times(1)).findAll();
    }
}
