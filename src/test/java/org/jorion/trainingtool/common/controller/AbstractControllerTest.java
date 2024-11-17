package org.jorion.trainingtool.common.controller;

import org.jorion.trainingtool.support.AuthenticationUtils;
import org.jorion.trainingtool.user.RandomUser;
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
class AbstractControllerTest {

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
    void testFindUserFindInSession() {

        var session = new MockHttpSession();
        var user = RandomUser.createUser(USERNAME);
        session.setAttribute(SESSION_USER, user);

        var foundUser = abstractController.findUser(session);
        assertNotNull(foundUser);
        assertEquals(USERNAME, foundUser.getUserName());
        var sessionUser = (User) session.getAttribute(SESSION_USER);
        assertNotNull(sessionUser);
        assertEquals(USERNAME, sessionUser.getUserName());
    }

    @Test
    void testFindUserNotInSession() {

        var session = new MockHttpSession();
        AuthenticationUtils.setAuthentication(USERNAME);
        var localUser = RandomUser.createUser(USERNAME);
        when(userService.findUserByUserNameOrCreate(USERNAME)).thenReturn(localUser);

        var foundUser = abstractController.findUser(session);
        assertNotNull(foundUser);
        assertEquals(USERNAME, foundUser.getUserName());
        var sessionUser = (User) session.getAttribute(SESSION_USER);
        assertNotNull(sessionUser);
        assertEquals(USERNAME, sessionUser.getUserName());
        verify(userService, times(1)).findUserByUserNameOrCreate(USERNAME);
    }

    @Test
    void testRemoveUserFromSession() {

        var session = new MockHttpSession();
        var user = RandomUser.createUser(USERNAME);
        session.setAttribute(AbstractController.SESSION_USER, user);
        assertNotNull(session.getAttribute(SESSION_USER));

        abstractController.removeUserFromSession(session);
        assertNull(session.getAttribute(SESSION_USER));
    }

    @Test
    void testGetAndRemoveAttribute() {

        var session = new MockHttpSession();
        session.setAttribute("attribute-name", "submit");

        var value = (String) abstractController.getAndRemoveAttribute(session, "attribute-name");
        assertEquals("submit", value);
        assertNull(session.getAttribute("attribute-name"));
    }
}
