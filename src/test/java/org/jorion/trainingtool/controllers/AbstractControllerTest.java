package org.jorion.trainingtool.controllers;

import static org.jorion.trainingtool.controllers.AbstractController.SESSION_USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.services.UserService;
import org.jorion.trainingtool.util.TestingUtils;

/**
 * Unit test for {@link AbstractController}.
 */
public class AbstractControllerTest
{
    // --- Constants ---
    private static final String USERNAME = "jdoe";

    // --- Variables ---
    @Mock
    UserService mockUserService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    // --- Methods ---
    @Before
    public void setUp()
    {
        TestingUtils.cleanAuthentication();
    }

    @After
    public void cleanUp()
    {
        TestingUtils.cleanAuthentication();
    }

    @Test
    public void testFindUserFindInSession()
    {
        AbstractController c = new AbstractController() {};
        HttpSession session = new MockHttpSession();
        User user = new User(USERNAME);
        session.setAttribute(SESSION_USER, user);

        User foundUser = c.findUser(session);
        assertNotNull(foundUser);
        assertEquals(USERNAME, foundUser.getUsername());
        assertEquals(USERNAME, ((User) session.getAttribute(SESSION_USER)).getUsername());
    }

    @Test
    public void testFindUserNotInSession()
    {
        AbstractController c = new AbstractController() {};
        HttpSession session = new MockHttpSession();
        ReflectionTestUtils.setField(c, "userService", mockUserService);

        TestingUtils.setAuthentication(USERNAME);
        User localUser = new User(USERNAME);
        when(mockUserService.findUserByUsernameOrCreate(USERNAME)).thenReturn(localUser);

        User foundUser = c.findUser(session);
        assertNotNull(foundUser);
        assertEquals(USERNAME, foundUser.getUsername());
        assertEquals(USERNAME, ((User) session.getAttribute(SESSION_USER)).getUsername());
        verify(mockUserService, times(1)).findUserByUsernameOrCreate(USERNAME);
    }

    @Test
    public void testRemoveUser()
    {
        AbstractController c = new AbstractController() {};
        HttpSession session = new MockHttpSession();
        User user = new User(USERNAME);
        session.setAttribute(AbstractController.SESSION_USER, user);
        assertNotNull(session.getAttribute(SESSION_USER));
        c.removeUser(session);
        assertNull(session.getAttribute(SESSION_USER));
    }

    @Test
    public void testGetAndRemoveAttribute()
    {
        AbstractController c = new AbstractController() {};
        HttpSession session = new MockHttpSession();
        session.setAttribute(AbstractController.SESSION_REG_EVENT, "submit");
        String value = (String) c.getAndRemoveAttribute(session, AbstractController.SESSION_REG_EVENT);
        assertEquals("submit", value);
        assertNull(session.getAttribute(AbstractController.SESSION_REG_EVENT));
    }
}
