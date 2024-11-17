package org.jorion.trainingtool.registration;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jorion.trainingtool.infra.AbstractController;
import org.jorion.trainingtool.infra.ControllerConstants;
import org.jorion.trainingtool.training.Training;
import org.jorion.trainingtool.training.TrainingService;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.user.User;
import org.jorion.trainingtool.user.UserService;
import org.jorion.trainingtool.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class RegistrationController extends AbstractController {

    @Autowired
    private UserService userService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private TrainingService trainingService;

    /**
     * Show an existing registration.
     *
     * @param regId   the registration id
     * @param model   the attribute holder for the page
     * @param session the HTTP session
     * @return "registration" if the user has access to the registration, "home" otherwise
     */
    @GetMapping("/registration")
    public String showRegistration(@RequestParam("id") Long regId, Model model, HttpSession session) {

        User user = findUser(session);
        Registration registration = registrationService.findById(user, regId);
        if (registration == null) {
            log.warn("Cannot show registration [{}] for user [{}]", regId, user.getUserName());
            setSessionEvent(session);
            return "redirect:home";
        }
        User member = registration.getMember();

        model.addAttribute(ControllerConstants.MD_USER, user);
        model.addAttribute(ControllerConstants.MD_MEMBER, member);
        model.addAttribute(ControllerConstants.MD_REGISTRATION, registration);

        // possible actions
        model.addAttribute("approvable", RegistrationService.isApprovable(user, registration));
        model.addAttribute("submittable", RegistrationService.isSubmittable(user, registration));
        model.addAttribute("acceptable", RegistrationService.isAcceptable(user, registration));
        model.addAttribute("editable", RegistrationService.isEditable(user, registration));
        model.addAttribute("deletable", RegistrationService.isDeletable(user, registration));
        model.addAttribute("refusable", RegistrationService.isRefusable(user, registration));
        model.addAttribute("rejectable", RegistrationService.isRejectable(user, registration));

        // if data is NOT valid, the user will NOT be able to approve the registration
        boolean sctr = !StringUtils.isBlank(member.getSector());
        boolean mtvn = RegistrationValidator.isJustificationValid(user, registration);
        boolean ssin = RegistrationValidator.isSsinValid(user, registration);
        boolean date = RegistrationValidator.isDateValid(user, registration);
        boolean bill = RegistrationValidator.isBillabilityValid(user, registration);
        boolean all = sctr && mtvn && ssin && date && bill;

        model.addAttribute("sctrValid", sctr);
        model.addAttribute("mtvnValid", mtvn);
        model.addAttribute("ssinValid", ssin);
        model.addAttribute("dateValid", date);
        model.addAttribute("billValid", bill);
        model.addAttribute("allValid", all);

        return "registration";
    }

    /**
     * The user selects a pre-defined training.
     *
     * @param trainingId the selected training id
     * @return "redirect:edit-registration"
     */
    @GetMapping("/select-training-registration")
    public String selectTrainings(@RequestParam("id") Long trainingId, Model model, HttpSession session) {

        model.addAttribute(ControllerConstants.MD_TRAINING_ID, trainingId);
        return "redirect:edit-registration";
    }

    /**
     * Create a new registration for edit or modify an existing one.
     *
     * @param regId   the registration id (optional) - if null, this is new registration being created
     * @param model   the attribute holder for the page
     * @param session the HTTP session
     * @return "registration-edit" if successful
     */
    @GetMapping("/edit-registration")
    public String editRegistration(@RequestParam(name = "id", required = false) Long regId,
                                   @RequestParam(name = "trainingId", required = false) Long trainingId,
                                   Model model, HttpSession session) {

        // user
        User user = findUser(session);

        // training
        Training training = null;
        if (trainingId != null) {
            training = trainingService.findAvailableTrainingById(trainingId);
            if (training == null) {
                log.warn("Cannot find training [{}]. Is it enabled?", trainingId);
                setSessionEvent(session);
                return "redirect:home";
            }
        }

        // registration
        Registration registration;
        if (regId != null) {
            registration = registrationService.findById(user, regId);
            if (registration == null) {
                log.error("Registration [{}] not found for user [{}]", regId, user.getUserName());
                return "redirect:home";
            }
            if (!RegistrationService.isEditable(user, registration)) {
                log.error("Registration [{}] not editable for user [{}]", regId, user.getUserName());
                return "redirect:home";
            }
        } else {
            if (training != null) {
                registration = RegistrationMapper.INSTANCE.toRegistration(training);
                registration.setTrainingId(trainingId);
            } else {
                registration = new Registration();
            }
        }

        // member
        User member;
        if (registration.getMember() != null) {
            member = registration.getMember();
        } else {
            member = getMemberFromSession(session, user);
            registration.setMember(member);
        }
        setMemberToSession(session, member);

        model.addAttribute(ControllerConstants.MD_USER, user);
        model.addAttribute(ControllerConstants.MD_MEMBER, member);
        model.addAttribute(ControllerConstants.MD_REGISTRATION, registration);
        model.addAttribute(ControllerConstants.MD_TRAINING_ID, trainingId);
        model.addAttribute(ControllerConstants.MD_TRAINING_CONTENT, (training == null) ? null : training.getContent());
        model.addAttribute(ControllerConstants.MD_TRAININGS, findAllTrainings(regId));
        model.addAttribute(ControllerConstants.MD_SECTORS, userService.getSectors());
        model.addAttribute(ControllerConstants.MD_ERRORS, Collections.EMPTY_LIST);
        return "registration-edit";
    }

    /**
     * Save a new or an existing registration.
     * <p>
     * This way of working has some weaknesses. The member's username and the registration id come from hidden fields in
     * the page. That means somebody could manually modify those values and change another registration.
     *
     * @param frmMember       a user object containing member's data
     * @param frmRegistration a registration object containing the registration data
     * @param model           the attribute holder for the page
     * @param session         the HTTP session
     * @return "redirect:registration" if successful
     */
    @PostMapping("/save-registration")
    public String saveRegistration(User frmMember, Registration frmRegistration, Model model, HttpSession session) {

        Long regId = frmRegistration.getId();
        User user = findUser(session);
        User member = getMemberFromSession(session, null);

        Registration registration;
        if (regId != null) {
            // existing registration
            registration = registrationService.findById(user, regId);
            if (registration == null) {
                log.error("Registration [{}] does not exist", regId);
                return "redirect:home";
            }
            member = registration.getMember();
            frmRegistration.setStatus(registration.getStatus());
            if (!RegistrationService.isEditable(user, registration)) {
                log.error("Registration [{}] save not allowed for user [{}]", regId, user.getUserName());
                return "redirect:home";
            }
        }
        Assert.notNull(member, "'Member' should not be null");
        String username = member.getUserName();

        // Checking business errors
        List<String> errors = registrationService.checkBusinessErrors(user, frmMember, frmRegistration);
        if (!errors.isEmpty()) {
            member.convertFrom(frmMember);
            model.addAttribute(ControllerConstants.MD_USER, user);
            model.addAttribute(ControllerConstants.MD_MEMBER, member);
            model.addAttribute(ControllerConstants.MD_TRAINING_ID, frmRegistration.getTrainingId());
            model.addAttribute(ControllerConstants.MD_TRAININGS, findAllTrainings(regId));
            model.addAttribute(ControllerConstants.MD_SECTORS, userService.getSectors());
            model.addAttribute(ControllerConstants.MD_REGISTRATION, frmRegistration);
            model.addAttribute(ControllerConstants.MD_ERRORS, errors);
            return "registration-edit";
        }

        frmRegistration.setPrice(StrUtils.parsePositiveFloat(frmRegistration.getPrice()));
        frmMember.setUserName(username);

        member = userService.updateUser(frmMember);
        frmRegistration.setMember(member);
        if (user.equals(member)) {
            // this will force a refresh of the current user from the session
            removeUserFromSession(session);
        }

        Registration savedRegistration = registrationService.saveRegistration(user, frmRegistration);
        return "redirect:registration?id=" + savedRegistration.getId();
    }

    /**
     * The user selects a pre-defined training.
     *
     * @param trainingId the selected training id
     * @return "redirect:edit-registration"
     */
    @GetMapping("/select-training-registrations")
    public String selectTraining(@RequestParam("id") Long trainingId, Model model, HttpSession session) {

        model.addAttribute(ControllerConstants.MD_TRAINING_ID, trainingId);
        return "redirect:edit-registrations";
    }

    /**
     * Create a new registration for multiple members.
     *
     * @param model   the attribute holder for the page
     * @param session the HTTP session
     * @return "registrations-edit"
     */
    @GetMapping("/edit-registrations")
    public String editRegistrations(@RequestParam(name = "trainingId", required = false) Long trainingId,
                                    Model model, HttpSession session) {

        // user
        User user = findUser(session);
        String[] usernames = (String[]) session.getAttribute(SESSION_USERNAMES);
        log.info("New registration requested from [{}], for #users [{}]", user.getUserName(), usernames.length);

        // training
        Training training = null;
        if (trainingId != null) {
            training = trainingService.findAvailableTrainingById(trainingId);
            if (training == null) {
                log.warn("Cannot find training [{}]. Is it enabled?", trainingId);
            }
        }

        // registration
        Registration registration = new Registration();
        if (training != null) {
            registration = RegistrationMapper.INSTANCE.toRegistration(training);
            registration.setTrainingId(trainingId);
        }

        model.addAttribute(ControllerConstants.MD_USER, user);
        model.addAttribute(ControllerConstants.MD_USERNAMES, usernames);
        model.addAttribute(ControllerConstants.MD_TRAINING_ID, trainingId);
        model.addAttribute(ControllerConstants.MD_TRAININGS, trainingService.findAllTrainings(true));
        model.addAttribute(ControllerConstants.MD_TRAINING_CONTENT, (training == null) ? null : training.getContent());
        model.addAttribute(ControllerConstants.MD_REGISTRATION, registration);
        model.addAttribute(ControllerConstants.MD_ERRORS, new ArrayList<>());
        return "registrations-edit";
    }

    /**
     * A supervisor assigns a new registration to several members at the same time.
     * <p>
     * The list of usernames is in the session.
     *
     * @param frmRegistration the registration to save
     * @param model           the attribute holder for the page
     * @param session         the HTTP session
     * @return "redirect:home" if successful
     */
    @PostMapping("/save-registrations")
    public String saveRegistrations(Registration frmRegistration, Model model, HttpSession session) {

        User user = findUser(session);
        String[] usernames = (String[]) session.getAttribute(SESSION_USERNAMES);

        if (usernames == null || usernames.length == 0) {
            log.warn("No usernames found in the session");
            return "redirect:home";
        }

        List<String> errors = registrationService.checkBusinessErrors(user, null, frmRegistration);
        errors.remove("billable");
        if (!errors.isEmpty()) {
            model.addAttribute(ControllerConstants.MD_USER, user);
            model.addAttribute(ControllerConstants.MD_USERNAMES, usernames);
            model.addAttribute(ControllerConstants.MD_TRAINING_ID, frmRegistration.getTrainingId());
            model.addAttribute(ControllerConstants.MD_TRAININGS, trainingService.findAllTrainings(true));
            model.addAttribute(ControllerConstants.MD_REGISTRATION, frmRegistration);
            model.addAttribute(ControllerConstants.MD_ERRORS, errors);
            return "registrations-edit";
        }

        frmRegistration.setPrice(StrUtils.parsePositiveFloat(frmRegistration.getPrice()));
        registrationService.saveRegistrations(user, frmRegistration, usernames);
        setSessionEvent(session, RegistrationEvent.ASSIGN);
        return "redirect:home";
    }

    /**
     * The supervisor approves (or the member submits) the registration.
     *
     * @param regId   the registration id
     * @param model   the attribute holder for the page
     * @param session the HTTP session
     * @return "redirect:home"
     */
    @GetMapping("/submit-registration")
    public String submitRegistration(@RequestParam("id") Long regId, Model model, HttpSession session) {

        log.debug("Submit/approve registration [{}]", regId);
        User user = findUser(session);
        Registration registration = registrationService.findById(user, regId);
        if (registration == null) {
            log.warn("Cannot submit/approve registration [{}] for user [{}]", regId, user.getUserName());
            return "redirect:home";
        }

        registrationService.updateRegistrationStatus(user, regId, RegistrationEvent.SUBMIT, null);
        setSessionEvent(session, RegistrationEvent.SUBMIT);
        return "redirect:home";
    }

    /**
     * The supervisor refuses the registration.
     *
     * @param body    a map containing the request parameters
     * @param model   the attribute holder for the page
     * @param session the HTTP session
     * @return "redirect:registration" if successful
     */
    @PostMapping("/refuse-registration")
    public String refuseRegistration(@RequestParam Map<String, String> body, Model model, HttpSession session) {

        Long regId = Long.parseLong(body.getOrDefault("regId", "0"));
        String justification = body.get("justification");
        User user = findUser(session);
        Registration registration = registrationService.findById(user, regId);
        if (registration == null) {
            log.warn("Cannot refuse registration [{}] for user [{}]", regId, user.getUserName());
            return "redirect:home";
        }

        registrationService.updateRegistrationStatus(user, regId, RegistrationEvent.REFUSE, justification);
        setSessionEvent(session, RegistrationEvent.REFUSE);
        return "redirect:home";
    }

    /**
     * The Manager sends back a registration to the member.
     *
     * @param regId   the registration id
     * @param model   the attribute holder for the page
     * @param session the HTTP session
     * @return "redirect:home"
     */
    @GetMapping("/sendback-registration")
    public String sendbackRegistration(@RequestParam("id") Long regId, Model model, HttpSession session) {

        User user = findUser(session);
        Registration registration = registrationService.findById(user, regId);
        if (registration == null) {
            log.warn("Cannot send back registration [{}] for user [{}]", regId, user.getUserName());
            return "redirect:home";
        }

        registrationService.updateRegistrationStatus(user, regId, RegistrationEvent.SEND_BACK, null);
        setSessionEvent(session, RegistrationEvent.SEND_BACK);
        return "redirect:home";
    }

    /**
     * The member deletes his own registration.
     *
     * @param regId   the registration id
     * @param model   the attribute holder for the page
     * @param session the HTTP session
     * @return "redirect:home"
     */
    @GetMapping("/delete-registration")
    public String deleteRegistration(@RequestParam("id") Long regId, Model model, HttpSession session) {

        User user = findUser(session);
        Registration registration = registrationService.findById(user, regId);
        if (registration == null) {
            log.warn("Cannot delete registration [{}] for user [{}]", regId, user.getUserName());
            return "redirect:home";
        }

        if (registrationService.deleteRegistration(user, registration)) {
            setSessionEvent(session, RegistrationEvent.DELETE);
        }
        return "redirect:home";
    }

    private List<Training> findAllTrainings(Long regId) {

        return (regId == null) ? trainingService.findAllTrainings(true) : Collections.emptyList();
    }
}
