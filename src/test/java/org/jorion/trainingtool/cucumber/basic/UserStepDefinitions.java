package org.jorion.trainingtool.cucumber.basic;

import org.jorion.trainingtool.cucumber.common.HttpClient;
import org.jorion.trainingtool.dto.json.UserDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserStepDefinitions {

    // @Autowired
    private HttpClient httpClient;

    private UserDTO user;

    // @Given("user is logged")
    public void user_is_logged() {
        // Write code to implement login
    }

    // @When("user calls users username")
    public void user_calls_user_username() {
        this.user = httpClient.getUser("john.doe");
    }

    // @Then("user info is retrieved")
    public void user_info_is_retrieved() {
        assertNotNull(user);
        assertEquals("john.doe", user.getUserName());
    }
}
