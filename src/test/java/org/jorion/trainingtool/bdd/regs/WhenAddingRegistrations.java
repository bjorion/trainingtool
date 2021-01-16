package org.jorion.trainingtool.bdd.regs;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.jorion.trainingtool.bdd.steps.RegistrationSteps;
import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.types.RegistrationStatus;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.thucydides.core.annotations.Narrative;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.annotations.Title;

@RunWith(SerenityRunner.class)
@Narrative(text = { "Adding a registration for a user" })
public class WhenAddingRegistrations
{
    @Steps
    RegistrationSteps john;

    @Test
    @Title("Create a new registration and then delete it")
    public void addNewRegistrationByMember()
    {
        System.out.println("---------------BDD");

        // Given
        john.createUser("john");
        john.shouldHaveRegistrations(0);

        // When
        Registration reg = new Registration(1L);
        reg.setTitle("MyNewRegistration");
        john.addNewRegistration(reg);

        // Then
        john.shouldHaveRegistrations(1);
        john.shouldRegistrationStatusBe(reg.getId(), RegistrationStatus.DRAFT);

        // When
        john.deleteRegistration(reg);

        // Then
        john.shouldHaveRegistrations(0);
    }
}
