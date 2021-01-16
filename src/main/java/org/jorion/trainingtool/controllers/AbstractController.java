package org.jorion.trainingtool.controllers;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.services.UserService;

/**
 * Base class for the controllers.
 */
public abstract class AbstractController
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(AbstractController.class);

    protected static final String SESSION_USER = "user";

    protected static final String SESSION_USERNAMES = "usernames";
    
    protected static final String SESSION_REG_EVENT = "registrationEvent";

    // --- Variables ---
    @Autowired
    private UserService userService;

    // --- Methods ---
    /**
     * Retrieve the user using the following sources:
     * <ol>
     * <li>The HTTP session
     * <li>the DB
     * <li>the LDAP
     * </ol>
     * 
     * @param session the HTTP session
     * @return the user, or null if not found
     */
    public User findUser(HttpSession session)
    {
        // Look into the session
        User user = (User) session.getAttribute(SESSION_USER);
        if (user != null) {
            LOG.trace("User retrieved from the session [{}]", user.getUsername());
            return user;
        }

        String username = UserService.getPrincipalName();
        user = userService.findUserByUsernameOrCreate(username);

        if (user == null) {
            LOG.error("User not found in LDAP");
        }

        session.setAttribute(SESSION_USER, user);
        return user;
    }

    /**
     * Remove the USER attribute from the session.
     * 
     * @param session the HTTP session
     */
    public void removeUser(HttpSession session)
    {
        session.removeAttribute(SESSION_USER);
    }

    /**
     * Get an attribute value from the session and remove it.
     * 
     * @param session the HTTP session
     * @param attributeName the attribute name
     * @return the value (can be null)
     */
    public Object getAndRemoveAttribute(HttpSession session, String attributeName)
    {
        Object value = session.getAttribute(attributeName);
        if (value != null) {
            session.removeAttribute(attributeName);
        }
        return value;
    }

}
