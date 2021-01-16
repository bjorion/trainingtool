package org.jorion.trainingtool.bdd.steps;

import static org.junit.Assert.assertEquals;

import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.types.RegistrationStatus;

import net.thucydides.core.annotations.Step;

/**
 * Step library.
 */
public class RegistrationSteps
{
    String actor;

    User user;

    @Step("#actor created with username {0}")
    public void createUser(String username)
    {
        user = new User(username);
    }

    @Step("#actor creates a new registration")
    public void addNewRegistration(Registration reg)
    {
        user.addRegistration(reg);
    }

    @Step("#actor deletes his registration")
    public void deleteRegistration(Registration reg)
    {
        user.removeRegistration(reg);
    }

    @Step("#actor shoud have {0} registration(s)")
    public void shouldHaveRegistrations(int i)
    {
        assertEquals(i, user.getRegistrations().size());
    }

    @Step("#actor's registration with id={0} should be in state {1}")
    public void shouldRegistrationStatusBe(Long regId, RegistrationStatus status)
    {
        Registration reg = user.findRegistrationById(regId);
        assertEquals(status, reg.getStatus());
    }
}
