package org.jorion.trainingtool.controller;

import jakarta.servlet.http.HttpSession;
import org.jorion.trainingtool.common.AuthenticationUtils;
import org.jorion.trainingtool.common.EntityUtils;
import org.jorion.trainingtool.common.controller.Constants;
import org.jorion.trainingtool.user.User;
import org.jorion.trainingtool.registration.RegistrationService;
import org.jorion.trainingtool.training.TrainingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HomeControllerTest {

    private static final String USERNAME = "jdoe";

    @Mock
    private RegistrationService registrationService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private HomeController homeController;

    @BeforeEach
    public void setUp() {
        AuthenticationUtils.cleanAuthentication();
    }

    @AfterEach
    public void cleanUp() {
        AuthenticationUtils.cleanAuthentication();
    }

    @Test
    void testShowRoot() {

        final String home = "forward:home";
        assertEquals(home, homeController.showRoot());
    }

    @Test
    void testHome() {

        final String home = "home";

        HttpSession session = new MockHttpSession();
        User user = EntityUtils.createUser(USERNAME);
        session.setAttribute("user", user);
        ExtendedModelMap model = new ExtendedModelMap();

        when(registrationService.findPendingByUser(user)).thenReturn(Collections.emptyList());
        when(trainingService.findAllTrainings(true)).thenReturn(Collections.emptyList());

        assertEquals(home, homeController.showHome(model, session));
        assertEquals(user, model.getAttribute(Constants.MD_USER));
        assertNotNull(model.getAttribute(Constants.MD_REGISTRATIONS));
        assertNotNull(model.getAttribute(Constants.MD_PENDING_REGISTRATIONS));
    }

    @Test
    void testShowLogin() {

        final String home = "redirect:home";
        final String login = "login";

        // check new user
        assertEquals(login, homeController.showLogin());

        // check user already logged in
        AuthenticationUtils.setAuthentication(USERNAME);
        assertEquals(home, homeController.showLogin());
    }
}
