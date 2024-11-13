package org.jorion.trainingtool.training;

import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.common.controller.AbstractController;
import org.jorion.trainingtool.common.controller.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
public class TrainingController extends AbstractController {

    @Autowired
    private TrainingService trainingService;

    /**
     * Show all the existing trainings.
     *
     * @param model the attribute holder for the page
     * @return "trainings"
     */
    @GetMapping("/trainings")
    public String showTrainings(Model model) {

        List<Training> trainings = trainingService.findAllTrainings(false);
        model.addAttribute(Constants.MD_TRAININGS, trainings);
        return "trainings";
    }

    /**
     * Create or modify a training.
     *
     * @param trainingId the training id (optional) - if null, a new training will be created
     * @param model      the attribute holder for the page
     * @return "training-edit"
     */
    @GetMapping("/edit-training")
    public String editTraining(@RequestParam(name = "id", required = false) Long trainingId,
                               Model model, HttpSession session) {

        Training training = null;

        if (trainingId != null) {
            training = trainingService.findTrainingById(trainingId);
            if (training == null) {
                log.error("Training [{}] not found", trainingId);
                setSessionEvent(session);
                return "redirect:home";
            }
        }
        if (training == null) {
            training = new Training();
        }

        model.addAttribute(Constants.MD_TRAINING, training);
        model.addAttribute(Constants.MD_ERRORS, Collections.EMPTY_LIST);
        return "training-edit";
    }

    /**
     * Save or update the given training.
     *
     * @return "trainings"
     */
    @PostMapping("/save-training")
    public String saveTraining(Training frmTraining, Model model) {

        Long trainingId = frmTraining.getId();
        if (trainingId != null) {
            Training training = trainingService.findTrainingById(trainingId);
            if (training == null) {
                log.error("Training [{}] does not exist", trainingId);
                return "redirect:trainings";
            }
        }

        List<String> errors = trainingService.checkBusinessErrors(frmTraining);
        if (!errors.isEmpty()) {
            model.addAttribute(Constants.MD_TRAINING, frmTraining);
            model.addAttribute(Constants.MD_ERRORS, errors);
            return "training-edit";
        }

        trainingService.saveTraining(frmTraining);
        return "redirect:trainings";
    }

    /**
     * Delete the given training.
     *
     * @param trainingId the training id
     * @param model      the attribute holder for the page
     * @param session    the HTTP session
     * @return "home"
     */
    @GetMapping("/delete-training")
    public String deleteTraining(@RequestParam("id") Long trainingId, Model model, HttpSession session) {

        Training training = trainingService.findTrainingById(trainingId);
        if (training == null) {
            log.warn("Cannot delete training [{}]", trainingId);
        } else {
            trainingService.deleteTraining(training);
        }
        return "redirect:trainings";
    }
}
