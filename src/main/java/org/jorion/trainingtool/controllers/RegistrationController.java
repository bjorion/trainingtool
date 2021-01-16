package org.jorion.trainingtool.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.services.RegistrationService;
import org.jorion.trainingtool.services.UserService;
import org.jorion.trainingtool.types.RegistrationEvent;
import org.jorion.trainingtool.util.MiscUtils;

/**
 * Controller for the registration page.
 */
@Controller
public class RegistrationController extends AbstractController
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(RegistrationController.class);

    // --- Variables ---
    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserService userService;

    // --- Methods ---
    /**
     * Show an existing registration.
     * 
     * @param regId the registration id
     * @param model the attribute holder for the page
     * @param session the HTTP session
     * @return "registration" if the user has access to the registration, "home" otherwise
     */
    @GetMapping("/registration")
    public String showRegistration(@RequestParam("id") Long regId, Model model, HttpSession session)
    {
        User user = findUser(session);
        Registration registration = registrationService.findById(user, regId);
        if (registration == null) {
            LOG.warn("Cannot show registration [{}] for user [{}]", regId, user.getUsername());
            session.setAttribute(SESSION_REG_EVENT, RegistrationEvent.UNAUTHORIZED);
            return "redirect:home";
        }
        User member = registration.getMember();

        model.addAttribute(Constants.MD_USER, user);
        model.addAttribute(Constants.MD_MEMBER, member);
        model.addAttribute(Constants.MD_REGISTRATION, registration);

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
        boolean mtvn = RegistrationService.isMotivationValid(user, registration);
        boolean ssin = RegistrationService.isSsinValid(user, registration);
        boolean date = RegistrationService.isDateValid(user, registration);
        boolean bill = RegistrationService.isBillabilityValid(user, registration);
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
     * Create a new registration for edit or modify an existing one.
     * 
     * @param regId the registration id (optional)
     * @param model the attribute holder for the page
     * @param session the HTTP session
     * @return "registration-edit" if successful
     */
    @GetMapping("/editRegistration")
    public String editRegistration(@RequestParam(name = "id", required = false) Long regId, Model model, HttpSession session)
    {
        User user = findUser(session);
        Registration registration = null;
        if (regId != null) {
            registration = registrationService.findById(user, regId);
            if (registration == null) {
                LOG.error("Registration [{}] not found for user [{}]", regId, user.getUsername());
                return "redirect:home";
            }
            if (!RegistrationService.isEditable(user, registration)) {
                LOG.error("Registration [{}] not editable for user [{}]", regId, user.getUsername());
                return "redirect:home";
            }
        }
        if (registration == null) {
            registration = new Registration();
        }

        User member = user;
        if (registration.getMember() != null) {
            member = registration.getMember();
        }
        else {
            registration.setMember(member);
        }

        model.addAttribute(Constants.MD_USER, user);
        model.addAttribute(Constants.MD_MEMBER, member);
        model.addAttribute(Constants.MD_REGISTRATION, registration);
        model.addAttribute(Constants.MD_SECTORS, userService.getSectors());
        model.addAttribute(Constants.MD_ERRORS, new ArrayList<>());
        return "registration-edit";
    }

    /**
     * Save a new or an existing registration.
     * <p>
     * This way of working has some weaknesses. The member's username and the registration id come from hidden fields in
     * the page. That means somebody could manually modify those values and change another registration.
     * 
     * @param formMember a user object containing member's data
     * @param formRegistration a registration object containing the registration data
     * @param model the attribute holder for the page
     * @param session the HTTP session
     * @return "redirect:registration" if successful
     */
    @PostMapping("/saveRegistration")
    public String saveRegistration(User formMember, Registration formRegistration, Model model, HttpSession session)
    {
        Long regId = formRegistration.getId();
        User user = findUser(session);

        Registration registration = null;
        String username = null;
        if (regId == null) {
            // new registration: find username from request data
            username = formMember.getUsername();
        }
        else {
            // existing registration
            registration = registrationService.findById(user, regId);
            username = registration.getMember().getUsername();
            formRegistration.setStatus(registration.getStatus());
            if (!RegistrationService.isEditable(user, registration)) {
                LOG.error("Registration [{}] save not allowed for user [{}]", regId, user.getUsername());
                return "redirect:home";
            }
        }

        // Checking business errors
        List<String> errors = RegistrationService.checkBusinessErrors(user, formMember, formRegistration);
        if (!errors.isEmpty()) {
            User member = (regId == null) ? userService.findUserByUsername(username) : registration.getMember();

            member.convertFrom(formMember);
            model.addAttribute(Constants.MD_USER, user);
            model.addAttribute(Constants.MD_MEMBER, member);
            model.addAttribute(Constants.MD_REGISTRATION, formRegistration);
            model.addAttribute(Constants.MD_SECTORS, userService.getSectors());
            model.addAttribute(Constants.MD_ERRORS, errors);
            return "registration-edit";
        }

        formRegistration.setPrice(MiscUtils.parsePositiveFloat(formRegistration.getPrice()));
        formMember.setUsername(username);

        User member = userService.updateUser(formMember);
        formRegistration.setMember(member);
        if (user.equals(member)) {
            // this will force a refresh of the current user from the session
            removeUser(session);
        }

        Registration savedRegistration = registrationService.saveRegistration(user, formRegistration);
        return "redirect:registration?id=" + savedRegistration.getId();
    }

    /**
     * A supervisor assigns a new registration to several members at the same time.
     * <p>
     * The list of usernames is in the session.
     * 
     * @param model the attribute holder for the page
     * @param session the HTTP session
     * @return "redirect:home" if successful
     */
    @PostMapping("/saveRegistrations")
    public String saveRegistrations(Registration formRegistration, Model model, HttpSession session)
    {
        User user = findUser(session);
        String[] usernames = (String[]) session.getAttribute(SESSION_USERNAMES);

        if (usernames == null || usernames.length == 0) {
            LOG.warn("No usernames found in the session");
            return "redirect:home";
        }

        List<String> errors = RegistrationService.checkBusinessErrors(user, null, formRegistration);
        if (!errors.isEmpty()) {
            model.addAttribute(Constants.MD_USER, user);
            model.addAttribute(Constants.MD_USERNAMES, usernames);
            model.addAttribute(Constants.MD_REGISTRATION, formRegistration);
            model.addAttribute(Constants.MD_ERRORS, errors);
            return "registrations-edit";
        }

        formRegistration.setPrice(MiscUtils.parsePositiveFloat(formRegistration.getPrice()));
        registrationService.saveRegistrations(user, formRegistration, usernames);
        session.setAttribute(SESSION_REG_EVENT, RegistrationEvent.ASSIGN);
        return "redirect:home";
    }

    /**
     * The supervisor approves (or the member submits) the registration.
     * 
     * @param regId the registration id
     * @param model the attribute holder for the page
     * @param session the HTTP session
     * @return "redirect:home"
     */
    @GetMapping("/submitRegistration")
    public String submitRegistration(@RequestParam("id") Long regId, Model model, HttpSession session)
    {
        LOG.debug("Submit/approve registration [{}]", regId);
        User user = findUser(session);
        Registration registration = registrationService.findById(user, regId);
        if (registration == null) {
            LOG.warn("Cannot submit/approve registration [{}] for user [{}]", regId, user.getUsername());
            return "redirect:home";
        }

        registrationService.updateRegistrationStatus(user, regId, RegistrationEvent.SUBMIT);
        session.setAttribute(SESSION_REG_EVENT, RegistrationEvent.SUBMIT);
        return "redirect:home";
    }

    /**
     * The supervisor refuses the registration.
     * 
     * @param regId the registration id
     * @param model the attribute holder for the page
     * @param session the HTTP session
     * @return "redirect:registration" if successful
     */
    @GetMapping("/refuseRegistration")
    public String refuseRegistration(@RequestParam("id") Long regId, Model model, HttpSession session)
    {
        User user = findUser(session);
        Registration registration = registrationService.findById(user, regId);
        if (registration == null) {
            LOG.warn("Cannot refuse registration [{}] for user [{}]", regId, user.getUsername());
            return "redirect:home";
        }

        registrationService.updateRegistrationStatus(user, regId, RegistrationEvent.REFUSE);
        session.setAttribute(SESSION_REG_EVENT, RegistrationEvent.REFUSE);
        return "redirect:home";
    }

    /**
     * The Manager sends back a registration to the member.
     * 
     * @param regId the registration id
     * @param model the attribute holder for the page
     * @param session the HTTP session
     * @return "redirect:home"
     */
    @GetMapping("/sendbackRegistration")
    public String sendbackRegistration(@RequestParam("id") Long regId, Model model, HttpSession session)
    {
        User user = findUser(session);
        Registration registration = registrationService.findById(user, regId);
        if (registration == null) {
            LOG.warn("Cannot send back registration [{}] for user [{}]", regId, user.getUsername());
            return "redirect:home";
        }

        registrationService.updateRegistrationStatus(user, regId, RegistrationEvent.SEND_BACK);
        session.setAttribute(SESSION_REG_EVENT, RegistrationEvent.SEND_BACK);
        return "redirect:home";
    }

    /**
     * The member deletes his own registration.
     * 
     * @param regId the registration id
     * @param model the attribute holder for the page
     * @param session the HTTP session
     * @return "redirect:home"
     */
    @GetMapping("/deleteRegistration")
    public String deleteRegistration(@RequestParam("id") Long regId, Model model, HttpSession session)
    {
        User user = findUser(session);
        Registration registration = registrationService.findById(user, regId);
        if (registration == null) {
            LOG.warn("Cannot delete registration [{}] for user [{}]", regId, user.getUsername());
            return "redirect:home";
        }

        registrationService.deleteRegistration(user, registration);
        session.setAttribute(SESSION_REG_EVENT, RegistrationEvent.DELETE);
        return "redirect:home";
    }

    /**
     * Add attribues to the model. This method will be invoked <b>before</b> all the other controllers in the class.
     * 
     * @param model the attribute holder for the page
     */
    @ModelAttribute
    public void addAttributes(Model model, HttpSession session)
    {
        // empty
    }

    /**
     * Handle the AccessDeniedException exception: redirect to a "access-denied" page.
     *
     * @param e an instance of AccessDeniedException
     * @param principal the current Principal
     * @return a ModelAndView
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleException(AccessDeniedException e, Principal principal)
    {
        LOG.error("AccessDeniedException [{}] for Principal {}", new Object[] { e.getMessage(), principal });
        ModelAndView mav = new ModelAndView();
        mav.setViewName("access-denied");
        mav.addObject("message", e.getMessage());
        return mav;
    }

}
