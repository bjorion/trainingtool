package org.jorion.trainingtool.controllers;

import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.services.RegistrationService;
import org.jorion.trainingtool.types.RegistrationEvent;

/**
 * Controller for the home page.
 * <p>
 * Important: the string values returned by methods should not start with /, otherwise this will confuse the Thymeleaf
 * template resolver.
 */
@Controller
public class HomeController extends AbstractController
{
    // --- Constants ---
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

    // --- Variables ---
    @Autowired
    private RegistrationService registrationService;

    // --- Methods ---
    /**
     * Alternate mapping for the home page.
     * 
     * @return "forward:home"
     */
    @GetMapping("/")
    public String showDefault()
    {
        return "forward:home";
    }

    /**
     * Mapping for the home page.
     * 
     * @param model the attribute holder for the page
     * @param session the HTTP session
     * @return "home";
     */
    @GetMapping("/home")
    public String showHome(Model model, HttpSession session)
    {
        User user = findUser(session);

        // retrieve registrations, sort them by ID desc
        Set<Registration> registrations = user.getRegistrations();
        Set<Registration> sortedRegistrations = new TreeSet<>((o1, o2) -> o2.getId().compareTo(o1.getId()));
        sortedRegistrations.addAll(registrations);

        model.addAttribute(Constants.MD_USER, user);
        model.addAttribute(Constants.MD_PENDING_REGISTRATIONS, registrationService.findPendingByUser(user));
        model.addAttribute(Constants.MD_REGISTRATIONS, sortedRegistrations);

        RegistrationEvent event = (RegistrationEvent) getAndRemoveAttribute(session, SESSION_REG_EVENT);
        if (event != null) {
            // the value must correspond to a key in the file message.properties
            model.addAttribute(Constants.MD_REG_EVENT_LABEL, "msg.reg." + event.name().toLowerCase());
        }

        return "home";
    }

    /**
     * Mapping for the login action. We want to skip the login page if the user is already logged in.
     * 
     * @return "login" if not yet logged in, "home" otherwise.
     */
    @GetMapping("/login")
    public String showLogin()
    {
        String result;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            result = "login";
        }
        else {
            result = "redirect:home";
        }
        return result;
    }
}
