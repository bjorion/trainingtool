package org.jorion.trainingtool.home;

// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.jorion.trainingtool.common.EntityUtils;
import org.jorion.trainingtool.ldap.CustomLdapAuthoritiesPopulator;
import org.jorion.trainingtool.registration.IRegistrationRepository;
import org.jorion.trainingtool.registration.RegistrationService;
import org.jorion.trainingtool.training.ITrainingRepository;
import org.jorion.trainingtool.training.TrainingService;
import org.jorion.trainingtool.user.IUserRepository;
import org.jorion.trainingtool.user.User;
import org.jorion.trainingtool.user.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test for {@link HomeController}.
 * <p>
 * See
 * <ul>
 * <li><a href="https://newbedev.com/spring-test-security-how-to-mock-authentication">...</a>
 * <li>https://www.baeldung.com/spring-security-integration-tests
 * </ul>
 */
// @WebMvcTest(controllers = HomeController.class)
public class HomeControllerMvcTest {

    private static final String USERNAME = "john";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private TrainingService trainingService;

    @MockBean
    private CustomLdapAuthoritiesPopulator customLdapAuthoritiesPopulator;

    @MockBean
    private IUserRepository userRepository;

    @MockBean
    private ITrainingRepository trainingRepository;

    @MockBean
    private IRegistrationRepository registrationRepository;

    /**
     * User is not authenticated: go to the login page.
     */
    @Test
    @Disabled
    public void testShowLogin()
            throws Exception {

        final String extract = "action=\"/login\"";

        mockMvc.perform(get("/login"))
                // .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(extract)));
    }

    @Test
    @Disabled
    @WithMockUser(username = USERNAME, roles = "MEMBER")
    public void testShowHomeAuthenticated()
            throws Exception {

        final String extract = "<title>Home</title>";
        final User user = EntityUtils.createUser(USERNAME);
        Mockito.when(userService.findUserByUserNameOrCreate(USERNAME)).thenReturn(user);

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(extract)));
    }

    @Test
    @Disabled
    public void testShowHomeUnauthenticated()
            throws Exception {

        mockMvc.perform(get("/home")).andExpect(status().isUnauthorized());
    }

}
