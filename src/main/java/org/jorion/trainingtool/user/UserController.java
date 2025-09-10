package org.jorion.trainingtool.user;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.infra.AbstractController;
import org.jorion.trainingtool.infra.ControllerConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Slf4j
@Controller
public class UserController extends AbstractController {

    private static final int MAX_USERS = 10;
    private final UserService userService;

    public UserController(UserService userService) {
        super(userService);
        this.userService = userService;
    }

    /**
     * Initialize the member selection page.
     *
     * @param model   the Spring Model
     * @param session the HTTP session
     * @return "member-select"
     */
    @GetMapping("/select-member")
    public String selectMemberInit(Model model, HttpSession session) {

        User user = findUser(session);
        model.addAttribute(ControllerConstants.MD_USER, user);
        model.addAttribute(ControllerConstants.MD_MEMBER, new User());
        return "member-select";
    }

    /**
     * Find a member (for a training to assign to).
     *
     * @param member  the criteria to retrieve a member
     * @param model   the Spring Model
     * @param session the HTTP session
     * @return "member-select"
     */
    @GetMapping("/select-member-search")
    public String selectMemberSearch(User member, Model model, HttpSession session) {

        User user = findUser(session);
        List<String> errors = userService.checkBusinessErrors(member);
        if (!errors.isEmpty()) {
            model.addAttribute(ControllerConstants.MD_USER, user);
            model.addAttribute(ControllerConstants.MD_MEMBER, new User());
            model.addAttribute(ControllerConstants.MD_ERRORS, errors);
            return "member-select";
        }

        Set<User> members = userService.findUsersByExample(member, MAX_USERS);
        model.addAttribute(ControllerConstants.MD_USER, user);
        model.addAttribute(ControllerConstants.MD_MEMBERS, members);
        model.addAttribute(ControllerConstants.MD_MEMBER, member);
        model.addAttribute(ControllerConstants.MD_MEMBER_SEARCHED, true);
        return "member-select";
    }

    /**
     * A member is selected.
     *
     * @param selectedMember the member selected by the user
     * @param model          the Spring Model
     * @param session        the HTTP session
     * @return "registration-edit"
     */
    @GetMapping("/select-member-selected")
    public String selectMemberSelected(@RequestParam String selectedMember, Model model, HttpSession session) {

        log.info("New registration requested for user [{}]", selectedMember);
        User member = userService.findUserByUserNameOrCreate(selectedMember);
        setMemberToSession(session, member);
        return "redirect:edit-registration";
    }

    /**
     * Initialize the members selection page.
     *
     * @param model   the Spring Model
     * @param session the HTTP session
     * @return "members-select"
     */
    @GetMapping("/select-members")
    public String selectMembersInit(@RequestParam(name = "username", required = false) String username,
                                    Model model, HttpSession session) {

        User user = findUser(session);
        String newUsername = (username == null) ? user.getUserName() : username;
        Set<User> members = userService.findUsersByManager(newUsername);

        model.addAttribute(ControllerConstants.MD_USER, user);
        model.addAttribute(ControllerConstants.MD_MEMBERS, members);
        return "members-select";
    }

    /**
     * Members are selected.
     *
     * @param usernames an array of the selected usernames
     * @param model     the Spring Model
     * @param session   the HTTP session
     * @return "redirect:home" if no username is given, "registrations-edit" otherwise
     */
    @PostMapping("/select-members-selected")
    public String selectMembersSelected(@RequestParam(name = "usernames", required = false) String[] usernames,
                                        Model model, HttpSession session) {

        // nothing selected
        if (usernames == null || usernames.length == 0) {
            log.info("No User Selected");
            return "redirect:home";
        }

        session.setAttribute(SESSION_USERNAMES, usernames);
        return "redirect:edit-registrations";
    }

    /**
     * Add attributes to the model. This method will be invoked <b>before</b> all the other controllers in the class.
     */
    @ModelAttribute
    @SuppressWarnings("unused")
    public void addAttributes(Model model, HttpSession session) {

        // best to leave this empty, specially if you have redirections
    }

}
