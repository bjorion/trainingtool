package org.jorion.trainingtool.controllers;

import static org.jorion.trainingtool.controllers.AbstractController.SESSION_USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

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
import org.springframework.ui.ExtendedModelMap;

import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.services.RegistrationService;
import org.jorion.trainingtool.util.TestingUtils;

/**
 * Unit test for {@link HomeController}.
 */
public class HomeControllerTest
{
    // --- Constants ---
    private static final String USERNAME = "jdoe";

    // --- Variables ---
    @Mock
    RegistrationService mockRegistrationService;

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
    public void testShowDefault()
    {
        final String home = "forward:home";

        HomeController c = new HomeController();
        assertEquals(home, c.showDefault());
    }

    @Test
    public void testHome()
    {
        final String home = "home";

        HttpSession session = new MockHttpSession();
        User user = new User(USERNAME);
        session.setAttribute(SESSION_USER, user);
        ExtendedModelMap model = new ExtendedModelMap();

        HomeController c = new HomeController();
        ReflectionTestUtils.setField(c, "registrationService", mockRegistrationService);
        when(mockRegistrationService.findPendingByUser(user)).thenReturn(new ArrayList<>());

        assertEquals(home, c.showHome(model, session));
        assertEquals(user, model.getAttribute(Constants.MD_USER));
        assertNotNull(model.getAttribute(Constants.MD_REGISTRATIONS));
        assertNotNull(model.getAttribute(Constants.MD_PENDING_REGISTRATIONS));
    }

    @Test
    public void testShowLogin()
    {
        final String home = "redirect:home";
        final String login = "login";

        // check new user
        HomeController c = new HomeController();
        assertEquals(login, c.showLogin());

        // check user already logged in
        TestingUtils.setAuthentication(USERNAME);
        assertEquals(home, c.showLogin());
    }
}
