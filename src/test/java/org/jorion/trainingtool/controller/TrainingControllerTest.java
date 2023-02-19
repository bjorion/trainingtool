package org.jorion.trainingtool.controller;

import org.jorion.trainingtool.common.EntityUtils;
import org.jorion.trainingtool.entity.Training;
import org.jorion.trainingtool.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingController trainingController;

    @Test
    @SuppressWarnings("unchecked")
    void testShowTraining() {

        Training training = EntityUtils.createTrainingBuilder().build();
        List<Training> trainings = new ArrayList<>();
        trainings.add(training);
        ExtendedModelMap model = new ExtendedModelMap();
        when(trainingService.findAllTrainings(false)).thenReturn(trainings);

        assertEquals("trainings", trainingController.showTrainings(model));
        List<Training> result = (List<Training>) model.getAttribute(Constants.MD_TRAININGS);
        assertFalse(result.isEmpty());
        assertEquals(training.getId(), result.get(0).getId());
    }
}
