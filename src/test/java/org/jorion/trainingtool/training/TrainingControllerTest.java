package org.jorion.trainingtool.training;

import org.jorion.trainingtool.common.controller.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import java.util.ArrayList;
import java.util.List;

import static org.jorion.trainingtool.training.RandomTraining.buildTraining;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingController trainingController;

    @Test
    @SuppressWarnings("unchecked")
    void testShowTraining() {

        var training = buildTraining().build();
        var trainings = new ArrayList<Training>();
        trainings.add(training);
        var model = new ExtendedModelMap();
        when(trainingService.findAllTrainings(false)).thenReturn(trainings);

        assertEquals("trainings", trainingController.showTrainings(model));
        var result = (List<Training>) model.getAttribute(Constants.MD_TRAININGS);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(training.getId(), result.getFirst().getId());
    }
}
