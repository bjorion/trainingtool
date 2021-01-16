package org.jorion.trainingtool.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.services.UserService;

/**
 * Controller for the user-related operations.
 */
@Controller
public class UserController extends AbstractController
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private static final int MAX_USERS = 10;

    // --- Variables ---
    @Autowired
    private UserService userService;

    // --- Methods ---
    /**
     * Initialize the member selection page.
     * 
     * @param model the Spring Model
     * @param session the HTTP session
     * @return "member-select"
     */
    @GetMapping("/selectMember")
    public String selectMemberInit(Model model, HttpSession session)
    {
        User user = findUser(session);
        model.addAttribute(Constants.MD_USER, user);
        model.addAttribute(Constants.MD_MEMBER, new User());
        return "member-select";
    }

    /**
     * Find a member (for a training to assign to).
     * 
     * @param member the criteria to retrieve a member
     * @param model the Spring Model
     * @param session the HTTP session
     * @return "member-select"
     */
    @GetMapping("/selectMemberSearch")
    public String selectMemberSearch(User member, Model model, HttpSession session)
    {
        User user = findUser(session);
        List<String> errors = UserService.checkBusinessErrors(member);
        if (!errors.isEmpty()) {
            model.addAttribute(Constants.MD_USER, user);
            model.addAttribute(Constants.MD_MEMBER, new User());
            model.addAttribute(Constants.MD_ERRORS, errors);
            return "member-select";
        }

        Set<User> members = userService.findUsersByExample(member, MAX_USERS);
        model.addAttribute(Constants.MD_USER, user);
        model.addAttribute(Constants.MD_MEMBERS, members);
        model.addAttribute(Constants.MD_MEMBER, member);
        return "member-select";
    }

    /**
     * A member is selected.
     * 
     * @param selectedMember the member selected by the user
     * @param model the Spring Model
     * @param session the HTTP session
     * @return "registration-edit"
     */
    @GetMapping("/selectMemberSelected")
    public String selectMemberSelected(@RequestParam String selectedMember, Model model, HttpSession session)
    {
        LOG.info("New registration requested for user [{}]", selectedMember);

        User user = findUser(session);
        User member = userService.findUserByUsernameOrCreate(selectedMember);
        Registration registration = new Registration();
        registration.setMember(member);

        model.addAttribute(Constants.MD_USER, user);
        model.addAttribute(Constants.MD_MEMBER, member);
        model.addAttribute(Constants.MD_REGISTRATION, registration);
        model.addAttribute(Constants.MD_ERRORS, new ArrayList<>());
        return "registration-edit";
    }

    /**
     * Initialize the members selection page.
     * 
     * @param model the Spring Model
     * @param session the HTTP session
     * @return "members-select"
     */
    @GetMapping("/selectMembers")
    public String selectMembersInit(@RequestParam(name = "username", required = false) String username, Model model, HttpSession session)
    {
        User user = findUser(session);
        String newUsername = (username == null) ? user.getUsername() : username;
        Set<User> members = userService.findUsersByManager(newUsername);

        model.addAttribute(Constants.MD_USER, user);
        model.addAttribute(Constants.MD_MEMBERS, members);
        return "members-select";
    }

    /**
     * Members are selected.
     * 
     * @param usernames an array of the selected usernames
     * @param model the Spring Model
     * @param session the HTTP session
     * @return "redirect:home" if no username is given, "registrations-edit" otherwise
     */
    @PostMapping("/selectMembersSelected")
    public String selectMembersSelected(@RequestParam(name = "usernames", required = false) String[] usernames, Model model, HttpSession session)
    {
        // nothing selected
        if (usernames == null || usernames.length == 0) {
            return "redirect:home";
        }

        User user = findUser(session);
        session.setAttribute(SESSION_USERNAMES, usernames);
        LOG.info("New registration requested from [{}] for #users [{}]", user.getUsername(), usernames.length);

        Registration registration = new Registration();
        model.addAttribute(Constants.MD_USER, user);
        model.addAttribute(Constants.MD_USERNAMES, usernames);
        model.addAttribute(Constants.MD_REGISTRATION, registration);
        model.addAttribute(Constants.MD_ERRORS, new ArrayList<>());

        return "registrations-edit";
    }

    /**
     * Add attribues to the model. This method will be invoked <b>before</b> all the other controllers in the class.
     */
    @ModelAttribute
    public void addAttributes(Model model, HttpSession session)
    {
        model.addAttribute(Constants.MD_SECTORS, userService.getSectors());
    }

}
