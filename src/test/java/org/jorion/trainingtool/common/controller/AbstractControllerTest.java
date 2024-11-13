package org.jorion.trainingtool.common.controller;

import jakarta.servlet.http.HttpSession;
import org.jorion.trainingtool.common.AuthenticationUtils;
import org.jorion.trainingtool.common.EntityUtils;
import org.jorion.trainingtool.user.User;
import org.jorion.trainingtool.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import static org.jorion.trainingtool.common.controller.AbstractController.SESSION_USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbstractControllerTest {

    private static final String USERNAME = "jdoe";

    @Mock
    private UserService userService;

    @InjectMocks
    private AbstractController abstractController = new AbstractController() {
    };

    @AfterEach
    public void cleanUp() {
        AuthenticationUtils.cleanAuthentication();
    }

    @Test
    public void testFindUserFindInSession() {

        HttpSession session = new MockHttpSession();
        User user = EntityUtils.createUser(USERNAME);
        session.setAttribute(SESSION_USER, user);

        User foundUser = abstractController.findUser(session);
        assertNotNull(foundUser);
        assertEquals(USERNAME, foundUser.getUserName());
        assertEquals(USERNAME, ((User) session.getAttribute(SESSION_USER)).getUserName());
    }

    @Test
    public void testFindUserNotInSession() {

        HttpSession session = new MockHttpSession();
        AuthenticationUtils.setAuthentication(USERNAME);
        User localUser = EntityUtils.createUser(USERNAME);
        when(userService.findUserByUserNameOrCreate(USERNAME)).thenReturn(localUser);

        User foundUser = abstractController.findUser(session);
        assertNotNull(foundUser);
        assertEquals(USERNAME, foundUser.getUserName());
        assertEquals(USERNAME, ((User) session.getAttribute(SESSION_USER)).getUserName());
        verify(userService, times(1)).findUserByUserNameOrCreate(USERNAME);
    }

    @Test
    public void testRemoveUserFromSession() {

        HttpSession session = new MockHttpSession();
        User user = EntityUtils.createUser(USERNAME);
        session.setAttribute(AbstractController.SESSION_USER, user);
        assertNotNull(session.getAttribute(SESSION_USER));

        abstractController.removeUserFromSession(session);
        assertNull(session.getAttribute(SESSION_USER));
    }

    @Test
    public void testGetAndRemoveAttribute() {

        HttpSession session = new MockHttpSession();
        session.setAttribute("attribute-name", "submit");

        String value = (String) abstractController.getAndRemoveAttribute(session, "attribute-name");
        assertEquals("submit", value);
        assertNull(session.getAttribute("attribute-name"));
    }
}
