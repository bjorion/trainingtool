package org.jorion.trainingtool.service;

import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.entity.Training;
import org.jorion.trainingtool.mapper.TrainingMapper;
import org.jorion.trainingtool.repository.ITrainingRepository;
import org.jorion.trainingtool.service.validator.TrainingValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TrainingService {

    @Autowired
    private ITrainingRepository trainingRepository;

    /**
     * Return a list of trainings.
     *
     * @param availableOnly limit the list to the enabled trainings
     * @return A list of available trainings, or a list of all trainings.
     */
    public List<Training> findAllTrainings(boolean availableOnly) {

        List<Training> trainings;
        if (availableOnly) {
            trainings = trainingRepository.findAvailableTrainings(LocalDate.now());
        } else {
            trainings = trainingRepository.findAll();
        }
        return trainings;
    }

    /**
     * Return the training identified by the given id, as long as it's available.
     *
     * @param id the training id
     * @return the training, or null if not found or if the training was found, but is not available
     */
    public Training findAvailableTrainingById(Long id) {

        return trainingRepository.findById(id).filter(Training::isAvailable).orElse(null);
    }

    /**
     * Return the training identified by the given id.
     *
     * @param id the training id
     * @return the training, or null if not found
     */
    public Training findTrainingById(Long id) {
        return trainingRepository.findById(id).orElse(null);
    }

    /**
     * @return A list of invalid fields
     */
    public List<String> checkBusinessErrors(Training training) {
        return new TrainingValidator(training).validate();
    }

    /**
     * Update or Create a training.
     *
     * @param dtoTraining the training to update/create
     * @return The training updated or inserted
     */
    @Transactional
    public Training saveTraining(Training dtoTraining) {

        log.debug("Saving training id [{}]", dtoTraining.getId());

        Training training;
        if (dtoTraining.getId() == null) {
            training = dtoTraining;
        } else {
            training = findTrainingById(dtoTraining.getId());
            TrainingMapper.INSTANCE.updateTraining(dtoTraining, training);
        }

        Training savedTraining = trainingRepository.save(training);
        log.debug("Saved training id [{}]", savedTraining.getId());
        return savedTraining;
    }

    /**
     * Delete a training (for good).
     *
     * @param training the training to delete
     * @return true if the registration was deleted
     */
    public boolean deleteTraining(Training training) {

        trainingRepository.delete(training);
        trainingRepository.flush();
        log.info("Registration deleted [{}]", training.getId());
        return true;
    }

}
