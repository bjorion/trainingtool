package org.jorion.trainingtool.home;

import jakarta.servlet.http.HttpSession;
import org.jorion.trainingtool.infra.AbstractController;
import org.jorion.trainingtool.registration.Registration;
import org.jorion.trainingtool.registration.RegistrationService;
import org.jorion.trainingtool.training.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.TreeSet;

import static org.jorion.trainingtool.infra.ControllerConstants.*;

@Controller
public class HomeController extends AbstractController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private TrainingService trainingService;

    /**
     * Alternate mapping for the home page.
     *
     * @return "forward:home"
     */
    @GetMapping("/")
    public String showRoot() {
        return "forward:home";
    }

    /**
     * Mapping for the home page.
     *
     * @param model   the attribute holder for the page
     * @param session the HTTP session
     * @return "home";
     */
    @GetMapping("/home")
    public String showHome(Model model, HttpSession session) {

        var user = findUser(session);
        setMemberToSession(session, user);

        // retrieve registrations, sort them by ID desc
        var registrations = user.getRegistrations();
        var sortedRegistrations = new TreeSet<Registration>((o1, o2) -> o2.getId().compareTo(o1.getId()));
        sortedRegistrations.addAll(registrations);

        model.addAttribute(MD_USER, user);
        model.addAttribute(MD_PENDING_REGISTRATIONS, registrationService.findPendingByUser(user));
        model.addAttribute(MD_REGISTRATIONS, sortedRegistrations);
        model.addAttribute(MD_TRAININGS, trainingService.findAllTrainings(true));

        String event = getSessionEvent(session);
        if (event != null) {
            // the value must correspond to a key in the file message.properties
            model.addAttribute(MD_EVENT_LABEL, "msg.event." + event.toLowerCase());
        }

        return "home";
    }

    /**
     * Mapping for the login action. We skip the login page if the user is already logged in.
     *
     * @return "login" if not yet logged in, "home" otherwise.
     */
    @GetMapping("/login")
    public String showLogin() {

        String result;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            result = "login";
        } else {
            result = "redirect:home";
        }
        return result;
    }

    /**
     * Simulate an Internal Server Error. Used to test the error page.
     */
    @GetMapping("/test-error")
    public String testError() {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            throw new IllegalArgumentException("IllegalArgumentException for: " + auth.getName());
        }
        return "login";
    }

    /**
     * Simulate an AccessDeniedException. Used to test the access-denied page.
     */
    @GetMapping("/test-denied")
    public String testDenied() {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            throw new AccessDeniedException("AccessDeniedException for: " + auth.getName());
        }
        return "login";
    }

}
