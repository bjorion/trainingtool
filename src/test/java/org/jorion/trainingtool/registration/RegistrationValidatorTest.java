package org.jorion.trainingtool.registration;

import org.jorion.trainingtool.user.User;
import org.jorion.trainingtool.type.Period;
import org.jorion.trainingtool.type.Provider;
import org.jorion.trainingtool.type.Role;
import org.jorion.trainingtool.type.YesNo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.jorion.trainingtool.type.RegistrationStatus.SUBMITTED_TO_PROVIDER;
import static org.jorion.trainingtool.type.RegistrationStatus.SUBMITTED_TO_TRAINING;
import static org.jorion.trainingtool.user.RandomUser.createUser;
import static org.junit.jupiter.api.Assertions.*;

class RegistrationValidatorTest {
    
    private static final User USER_MEMBER = createUser("ALICE");
    private static final User USER_MANAGER = createUser("BOB");
    private static final User USER_TRAINING = createUser("TOM");

    private RegistrationValidator rv;

    @BeforeEach
    void setUp() {
        
        rv = new RegistrationValidator();
        rv.setWeeksToSubtract(3);
        USER_MEMBER.addRole(Role.MEMBER);
        USER_MANAGER.addRole(Role.MANAGER);
        USER_TRAINING.addRole(Role.TRAINING);
    }

    @Test
    void testCheckBusinessErrorsOK() {
        
        User user;
        User member;
        Registration r;
        List<String> errors;

        // User = MANAGER (everything ok)
        user = createUser("ALICE");
        user.addRole(Role.MANAGER);
        member = createUser("BOB");
        member.setSector("sector");
        r = new Registration();
        r.setTitle("title");
        r.setPeriod(Period.DAY);
        r.setProvider(Provider.INTERNAL);
        r.setUrl("http://www.example.org");
        r.setPrice("123456");
        r.setBillable(YesNo.YES);
        r.setJustification("justification");
        errors = rv.validate(user, member, r);
        assertEquals(0, errors.size());

        // User = Member, dates in the past
        user = createUser("ALICE");
        user.addRole(Role.MEMBER);
        user.setSector("my-sector");

        r = new Registration();
        r.setTitle("title");
        r.setProvider(Provider.CEVORA);
        r.setSsin("85073003328");
        r.setUrl("http://www.example.org");
        r.setPrice("123456");
        r.setPeriod(Period.DAY);
        r.setTotalHour("0");

        LocalDate startDate = LocalDate.now().minusDays(1);
        r.setStartDate(startDate);
        r.setEndDate(startDate);

        errors = rv.validate(user, member, r);
        assertEquals(0, errors.size());
    }

    @Test
    void testCheckBusinessErrorsNOK() {
        
        User user;
        User member;
        Registration r;
        List<String> errors;

        // Provider = NONE
        user = createUser("ALICE");
        r = new Registration();
        r.setPeriod(Period.DAY);
        errors = rv.validate(user, null, r);
        assertTrue(errors.contains("provider"));
        assertTrue(errors.contains("title"));
        assertTrue(errors.contains("url"));
        assertEquals(3, errors.size());

        // Provider = CEVORA
        user = createUser("ALICE");
        r = new Registration();
        r.setProvider(Provider.CEVORA);
        r.setUrl("http://www.example.org");
        errors = rv.validate(user, null, r);
        assertTrue(errors.contains("title"));
        assertTrue(errors.contains("ssin"));
        assertTrue(errors.contains("period"));
        assertTrue(errors.contains("totalHour"));
        assertTrue(errors.contains("startDate"));
        assertTrue(errors.contains("endDate"));
        assertEquals(6, errors.size());

        // Provider = OTHER
        user = createUser("ALICE");
        r = new Registration();
        r.setProvider(Provider.OTHER);
        r.setUrl("https://www.example.org");
        r.setPrice("123");
        errors = rv.validate(user, null, r);
        assertTrue(errors.contains("provider.other"));
        assertTrue(errors.contains("title"));
        assertTrue(errors.contains("period"));
        assertEquals(3, errors.size());

        // User = MANAGER
        user = createUser("ALICE");
        user.addRole(Role.MANAGER);
        member = createUser("BOB");
        r = new Registration();
        r.setProvider(Provider.INTERNAL);
        r.setUrl("ldap://www.example.org");
        r.setPrice("abc");
        errors = rv.validate(user, member, r);
        assertTrue(errors.contains("sector"));
        assertTrue(errors.contains("title"));
        assertTrue(errors.contains("url"));
        assertTrue(errors.contains("period"));
        assertTrue(errors.contains("price"));
        assertTrue(errors.contains("billable"));
        assertTrue(errors.contains("justification"));
        assertEquals(7, errors.size());

        // User = ADMIN
        user = createUser("ALICE");
        user.addRole(Role.TRAINING);
        r = new Registration();
        r.setProvider(Provider.OTHER);
        errors = rv.validate(user, null, r);
        assertTrue(errors.contains("provider.other"));
        assertTrue(errors.contains("title"));
        assertTrue(errors.contains("url"));
        assertTrue(errors.contains("period"));
        assertTrue(errors.contains("startDate"));
        assertTrue(errors.contains("endDate"));
        assertTrue(errors.contains("totalHour"));
        assertTrue(errors.contains("billable"));
        assertTrue(errors.contains("justification"));
        assertEquals(9, errors.size());
    }

    @Test
    void testIsJustificationValid() {
        
        Registration r = new Registration();
        assertTrue(RegistrationValidator.isJustificationValid(USER_MEMBER, r));

        r.setJustification("justification");
        assertTrue(RegistrationValidator.isJustificationValid(USER_MEMBER, r));

        r = new Registration();
        assertFalse(RegistrationValidator.isJustificationValid(USER_MANAGER, r));

        r.setJustification("justification");
        assertTrue(RegistrationValidator.isJustificationValid(USER_MANAGER, r));
    }

    @Test
    void testIsDateValid() {
        
        // draft
        Registration r = new Registration();
        assertTrue(RegistrationValidator.isDateValid(USER_MEMBER, r));

        r.setStartDate(LocalDate.now());
        assertTrue(RegistrationValidator.isDateValid(USER_MEMBER, r));

        r.setEndDate(LocalDate.now());
        assertTrue(RegistrationValidator.isDateValid(USER_MEMBER, r));

        // submitted to training
        r = new Registration();
        r.setStatus(SUBMITTED_TO_TRAINING);
        assertFalse(RegistrationValidator.isDateValid(USER_MEMBER, r));

        r.setStartDate(LocalDate.now());
        assertFalse(RegistrationValidator.isDateValid(USER_MEMBER, r));

        r.setEndDate(LocalDate.now());
        assertTrue(RegistrationValidator.isDateValid(USER_MEMBER, r));

        // submitted to provider
        r = new Registration();
        r.setStatus(SUBMITTED_TO_PROVIDER);
        assertFalse(RegistrationValidator.isDateValid(USER_MEMBER, r));

        r.setStartDate(LocalDate.now());
        assertFalse(RegistrationValidator.isDateValid(USER_MEMBER, r));

        r.setEndDate(LocalDate.now());
        assertTrue(RegistrationValidator.isDateValid(USER_MEMBER, r));
    }

    @Test
    void testIsSsinValid() {
        
        Registration r = new Registration();
        r.setProvider(Provider.CEVORA);
        assertFalse(RegistrationValidator.isSsinValid(USER_MEMBER, r));
        r.setSsin("00000000000");
        assertTrue(RegistrationValidator.isSsinValid(USER_MEMBER, r));

        r = new Registration();
        r.setProvider(Provider.CEVORA);
        assertTrue(RegistrationValidator.isSsinValid(USER_MANAGER, r));
        r.setSsin("00000000000");
        assertTrue(RegistrationValidator.isSsinValid(USER_MANAGER, r));
    }

    @Test
    void testIsBillabilityValid() {
        
        Registration r = new Registration();
        assertTrue(RegistrationValidator.isBillabilityValid(USER_MEMBER, r));
        assertFalse(RegistrationValidator.isBillabilityValid(USER_MANAGER, r));

        r.setBillable(YesNo.YES);
        assertTrue(RegistrationValidator.isBillabilityValid(USER_MEMBER, r));
        assertTrue(RegistrationValidator.isBillabilityValid(USER_MANAGER, r));
    }

    @Test
    void testIsSsinMandatory() {
        
        // CEVORA and Member
        Registration r = new Registration();
        r.setProvider(Provider.CEVORA);
        assertTrue(RegistrationValidator.isSsinMandatory(USER_MEMBER, r));

        // CEVORA and Manager
        r = new Registration();
        r.setProvider(Provider.CEVORA);
        assertFalse(RegistrationValidator.isSsinMandatory(USER_MANAGER, r));

        // Internal and Member
        r = new Registration();
        r.setProvider(Provider.INTERNAL);
        assertFalse(RegistrationValidator.isSsinMandatory(USER_MEMBER, r));

        // Internal and Manager
        r = new Registration();
        r.setProvider(Provider.INTERNAL);
        assertFalse(RegistrationValidator.isSsinMandatory(USER_MANAGER, r));
    }
}
