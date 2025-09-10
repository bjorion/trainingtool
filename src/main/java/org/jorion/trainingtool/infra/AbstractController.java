package org.jorion.trainingtool.infra;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.type.SessionEvent;
import org.jorion.trainingtool.user.User;
import org.jorion.trainingtool.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

/**
 * Base class for the controllers.
 * <p>
 * Session is used to store the following information:
 * <ul>
 * <li>user
 * <li>member
 * <li>usernames
 * <li>registration event
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractController {

    protected static final String SESSION_USER = "user";
    protected static final String SESSION_MEMBER = "member";
    protected static final String SESSION_USERNAMES = "usernames";
    private static final String SESSION_EVENT = "event";

    private final UserService userService;

    /**
     * Handle the AccessDeniedException exception: redirect to an "access-denied" page.
     *
     * @param e         an instance of AccessDeniedException
     * @param principal the current Principal
     * @return a ModelAndView
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleException(AccessDeniedException e, Principal principal) {

        log.error("AccessDeniedException [{}] for Principal {}", e.getMessage(), principal);
        var mav = new ModelAndView();
        mav.setStatus(HttpStatus.FORBIDDEN);
        mav.setViewName("error/403");
        mav.addObject("message", e.getMessage());
        return mav;
    }

    /**
     * Retrieve the user using the following sources, in this order:
     * <ol>
     * <li>The HTTP session
     * <li>the DB (if not found in the session)
     * <li>the LDAP (if not found in the DB)
     * </ol>
     * If the user is not found in the session, it will be added in it.
     *
     * @param session the HTTP session
     * @return the user, or null if not found
     */
    protected User findUser(HttpSession session) {

        // Look into the session
        var user = (User) session.getAttribute(SESSION_USER);
        if (user != null) {
            log.trace("User retrieved from the session [{}]", user.getUserName());
            return user;
        }

        var username = UserService.getPrincipalName();
        user = userService.findUserByUserNameOrCreate(username);

        if (user == null) {
            log.error("User not found in DB or LDAP");
        }

        session.setAttribute(SESSION_USER, user);
        return user;
    }

    /**
     * Remove the USER attribute from the session.
     *
     * @param session the HTTP session
     */
    protected void removeUserFromSession(HttpSession session) {
        session.removeAttribute(SESSION_USER);
    }

    protected User getMemberFromSession(HttpSession session, User defaultMember) {

        var member = (User) session.getAttribute(SESSION_MEMBER);
        return (member != null) ? member : defaultMember;
    }

    /**
     * Add the given member to the HTTP session.
     */
    protected void setMemberToSession(HttpSession session, User member) {

        session.setAttribute(SESSION_MEMBER, member);
    }

    protected void setSessionEvent(HttpSession session, RegistrationEvent event) {

        session.setAttribute(SESSION_EVENT, event.name());
    }

    protected void setSessionEvent(HttpSession session) {

        session.setAttribute(SESSION_EVENT, SessionEvent.UNAUTHORIZED.name());
    }

    protected String getSessionEvent(HttpSession session) {

        return (String) getAndRemoveAttribute(session, SESSION_EVENT);
    }

    /**
     * Get an attribute value from the session and remove it.
     *
     * @param session       the HTTP session
     * @param attributeName the attribute name
     * @return the value (can be null)
     */
    protected Object getAndRemoveAttribute(HttpSession session, String attributeName) {

        var value = session.getAttribute(attributeName);
        if (value != null) {
            session.removeAttribute(attributeName);
        }
        return value;
    }

}
