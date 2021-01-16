package org.jorion.trainingtool.services;

import static org.jorion.trainingtool.types.RegistrationStatus.DRAFT;
import static org.jorion.trainingtool.types.RegistrationStatus.NONE;
import static org.jorion.trainingtool.types.RegistrationStatus.REFUSED_BY_MANAGER;
import static org.jorion.trainingtool.types.RegistrationStatus.SUBMITTED_TO_HR;
import static org.jorion.trainingtool.types.RegistrationStatus.SUBMITTED_TO_MANAGER;
import static org.jorion.trainingtool.types.RegistrationStatus.SUBMITTED_TO_PROVIDER;
import static org.jorion.trainingtool.types.RegistrationStatus.SUBMITTED_TO_TRAINING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.repositories.IRegistrationRepository;
import org.jorion.trainingtool.types.Period;
import org.jorion.trainingtool.types.Provider;
import org.jorion.trainingtool.types.RegistrationEvent;
import org.jorion.trainingtool.types.RegistrationStatus;
import org.jorion.trainingtool.types.Role;
import org.jorion.trainingtool.types.YesNo;

/**
 * Unit tests for {@link RegistrationService}.
 */
public class RegistrationServiceTest
{
    // --- Constants ---
    private static final User USER_MEMBER = new User("ALICE");

    private static final User USER_MANAGER = new User("BOB");

    // --- Variables ---
    @Mock
    IRegistrationRepository mockRegistrationRepository;

    @Mock
    EventPublisherService mockEventPublisher;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    // --- Methods ---
    @Before
    public void setUp()
    {
        USER_MEMBER.addRole(Role.MEMBER);
        USER_MANAGER.addRole(Role.MANAGER);
    }

    @Test
    public void testFindByIdNotFound()
    {
        RegistrationService service = new RegistrationService();
        ReflectionTestUtils.setField(service, "registrationRepository", mockRegistrationRepository);

        Long regId = 1L;
        assertNull(service.findById(USER_MEMBER, regId));
        verify(mockRegistrationRepository, never()).findById(regId);
        verify(mockRegistrationRepository, times(1)).findRegistrationByIdAndUsername(regId, "ALICE");
    }

    @Test
    public void testFindPendingByUserManager()
    {
        RegistrationService service = new RegistrationService();
        ReflectionTestUtils.setField(service, "registrationRepository", mockRegistrationRepository);
        EnumSet<RegistrationStatus> set;
        List<Registration> list;

        // check for a simple member
        set = EnumSet.of(NONE);
        list = service.findPendingByUser(USER_MEMBER);
        assertTrue(list.isEmpty());
        verify(mockRegistrationRepository, never()).findRegistrationsByStatusesAndManager(set, null);

        // check for a manager
        set = EnumSet.of(NONE, SUBMITTED_TO_MANAGER);
        list = service.findPendingByUser(USER_MANAGER);
        assertTrue(list.isEmpty());
        verify(mockRegistrationRepository, times(1)).findRegistrationsByStatusesAndManager(set, "BOB");
    }

    @Test
    public void testFindPendingByUserHR()
    {
        final User user = new User("CHARLES");
        user.addRole(Role.MANAGER).addRole(Role.HR);

        RegistrationService service = new RegistrationService();
        ReflectionTestUtils.setField(service, "registrationRepository", mockRegistrationRepository);
        EnumSet<RegistrationStatus> set;
        List<Registration> list;

        // check for a HR that's also a manager
        set = EnumSet.of(NONE, SUBMITTED_TO_MANAGER, SUBMITTED_TO_HR);
        list = service.findPendingByUser(user);
        assertTrue(list.isEmpty());
        verify(mockRegistrationRepository, times(1)).findRegistrationsByStatusesAndManager(set, "CHARLES");
    }

    @Test
    public void testUpdateRegistrationStatus()
    {
        Registration r = new Registration(1L);
        r.setStatus(DRAFT);
        r.setMember(USER_MEMBER);
        r.setCreatedBy("ALICE");
        Optional<Registration> optReg = Optional.of(r);

        RegistrationService service = new RegistrationService();
        ReflectionTestUtils.setField(service, "registrationRepository", mockRegistrationRepository);
        ReflectionTestUtils.setField(service, "eventPublisher", mockEventPublisher);

        when(mockRegistrationRepository.findRegistrationByIdAndUsername(1L, "ALICE")).thenReturn(optReg);
        when(mockRegistrationRepository.save(r)).thenReturn(r);

        Registration result = service.updateRegistrationStatus(USER_MEMBER, 1L, RegistrationEvent.SUBMIT);
        assertEquals(SUBMITTED_TO_MANAGER, result.getStatus());
        verify(mockEventPublisher, times(1)).publishUpdateEvent(RegistrationEvent.SUBMIT, r, USER_MEMBER);
    }

    @Test
    public void testIsDeletable()
    {
        Registration r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(DRAFT);
        assertTrue(RegistrationService.isDeletable(USER_MEMBER, r));

        // wrong status
        r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(SUBMITTED_TO_MANAGER);
        assertFalse(RegistrationService.isDeletable(USER_MEMBER, r));

        // wrong user
        r = new Registration();
        r.setMember(USER_MANAGER);
        r.setStatus(DRAFT);
        assertFalse(RegistrationService.isDeletable(USER_MEMBER, r));
    }

    @Test
    public void testIsEditable()
    {
        // status = draft; principal = registration user
        Registration r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(DRAFT);
        assertTrue(RegistrationService.isEditable(USER_MEMBER, r));
    }

    @Test
    public void testIsApprovable()
    {
        // draft and owner
        Registration r = new Registration();
        r.setCreatedBy("ALICE");
        r.setMember(USER_MEMBER);
        r.setStatus(DRAFT);
        assertTrue(RegistrationService.isApprovable(USER_MEMBER, r));

        // draft and not owner
        r = new Registration();
        r.setCreatedBy("ALICE");
        r.setMember(USER_MEMBER);
        r.setStatus(DRAFT);
        assertFalse(RegistrationService.isApprovable(USER_MANAGER, r));

        // pending, principal ok
        r = new Registration();
        r.setCreatedBy("ALICE");
        r.setMember(USER_MEMBER);
        r.setStatus(SUBMITTED_TO_MANAGER);
        assertTrue(RegistrationService.isApprovable(USER_MANAGER, r));

        // not pending, principal ok
        r = new Registration();
        r.setCreatedBy("ALICE");
        r.setMember(USER_MEMBER);
        r.setStatus(REFUSED_BY_MANAGER);
        assertFalse(RegistrationService.isApprovable(USER_MANAGER, r));

        // pending, principal ok but belongs to him
        r = new Registration();
        r.setCreatedBy("BOB");
        r.setMember(USER_MANAGER);
        r.setStatus(SUBMITTED_TO_MANAGER);
        assertFalse(RegistrationService.isApprovable(USER_MANAGER, r));
    }

    @Test
    public void testIsSubmittable()
    {
        // draft and owner
        Registration r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(DRAFT);
        assertTrue(RegistrationService.isSubmittable(USER_MEMBER, r));

        // draft and not owner
        r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(DRAFT);
        assertFalse(RegistrationService.isSubmittable(USER_MANAGER, r));

        // pending, principal ok
        r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(SUBMITTED_TO_MANAGER);
        assertFalse(RegistrationService.isSubmittable(USER_MEMBER, r));

        // not pending, principal ok
        r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(REFUSED_BY_MANAGER);
        assertFalse(RegistrationService.isSubmittable(USER_MEMBER, r));
    }

    @Test
    public void testIsAcceptable()
    {
        Registration r = new Registration();
        r.setCreatedBy("ALICE");
        assertTrue(RegistrationService.isAcceptable(USER_MANAGER, r));

        r = new Registration();
        r.setCreatedBy("BOB");
        assertFalse(RegistrationService.isAcceptable(USER_MANAGER, r));
    }

    @Test
    public void testIsRefusable()
    {
        Registration r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(SUBMITTED_TO_MANAGER);
        assertTrue(RegistrationService.isRefusable(USER_MANAGER, r));

        // wrong status
        r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(SUBMITTED_TO_HR);
        assertFalse(RegistrationService.isRefusable(USER_MANAGER, r));

        // wrong user
        r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(SUBMITTED_TO_MANAGER);
        assertFalse(RegistrationService.isRefusable(USER_MEMBER, r));

        // both
        r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(SUBMITTED_TO_HR);
        assertFalse(RegistrationService.isRefusable(USER_MEMBER, r));
    }

    @Test
    public void testIsRejectable()
    {
        Registration r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(SUBMITTED_TO_MANAGER);
        assertTrue(RegistrationService.isRejectable(USER_MANAGER, r));

        // wrong status
        r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(SUBMITTED_TO_HR);
        assertFalse(RegistrationService.isRejectable(USER_MANAGER, r));

        // wrong user
        r = new Registration();
        r.setMember(USER_MEMBER);
        r.setStatus(SUBMITTED_TO_MANAGER);
        assertFalse(RegistrationService.isRejectable(USER_MEMBER, r));
    }

    @Test
    public void testIsMotivationValid()
    {
        Registration r = new Registration();
        assertTrue(RegistrationService.isMotivationValid(USER_MEMBER, r));

        r.setMotivation("motivation");
        assertTrue(RegistrationService.isMotivationValid(USER_MEMBER, r));

        r = new Registration();
        assertFalse(RegistrationService.isMotivationValid(USER_MANAGER, r));

        r.setMotivation("motivation");
        assertTrue(RegistrationService.isMotivationValid(USER_MANAGER, r));
    }

    @Test
    public void testIsDateValid()
    {
        // draft
        Registration r = new Registration();
        assertTrue(RegistrationService.isDateValid(USER_MEMBER, r));

        r.setStartDate(LocalDate.now());
        assertTrue(RegistrationService.isDateValid(USER_MEMBER, r));

        r.setEndDate(LocalDate.now());
        assertTrue(RegistrationService.isDateValid(USER_MEMBER, r));

        // submitted to training
        r = new Registration();
        r.setStatus(SUBMITTED_TO_TRAINING);
        assertFalse(RegistrationService.isDateValid(USER_MEMBER, r));

        r.setStartDate(LocalDate.now());
        assertFalse(RegistrationService.isDateValid(USER_MEMBER, r));

        r.setEndDate(LocalDate.now());
        assertTrue(RegistrationService.isDateValid(USER_MEMBER, r));

        // submitted to provider
        r = new Registration();
        r.setStatus(SUBMITTED_TO_PROVIDER);
        assertFalse(RegistrationService.isDateValid(USER_MEMBER, r));

        r.setStartDate(LocalDate.now());
        assertFalse(RegistrationService.isDateValid(USER_MEMBER, r));

        r.setEndDate(LocalDate.now());
        assertTrue(RegistrationService.isDateValid(USER_MEMBER, r));
    }

    @Test
    public void testIsSsinValid()
    {
        Registration r = new Registration();
        r.setProvider(Provider.CEVORA);
        assertFalse(RegistrationService.isSsinValid(USER_MEMBER, r));
        r.setSsin("00000000000");
        assertTrue(RegistrationService.isSsinValid(USER_MEMBER, r));

        r = new Registration();
        r.setProvider(Provider.CEVORA);
        assertTrue(RegistrationService.isSsinValid(USER_MANAGER, r));
        r.setSsin("00000000000");
        assertTrue(RegistrationService.isSsinValid(USER_MANAGER, r));
    }

    @Test
    public void testIsBillabilityValid()
    {
        Registration r = new Registration();
        assertTrue(RegistrationService.isBillabilityValid(USER_MEMBER, r));
        assertFalse(RegistrationService.isBillabilityValid(USER_MANAGER, r));

        r.setBillable(YesNo.YES);
        assertTrue(RegistrationService.isBillabilityValid(USER_MEMBER, r));
        assertTrue(RegistrationService.isBillabilityValid(USER_MANAGER, r));
    }

    @Test
    public void testIsSsinMandatory()
    {
        // CEVORA and Member
        Registration r = new Registration();
        r.setProvider(Provider.CEVORA);
        assertTrue(RegistrationService.isSsinMandatory(USER_MEMBER, r));

        // CEVORA and Manager
        r = new Registration();
        r.setProvider(Provider.CEVORA);
        assertFalse(RegistrationService.isSsinMandatory(USER_MANAGER, r));

        // Internal and Member
        r = new Registration();
        r.setProvider(Provider.INTERNAL);
        assertFalse(RegistrationService.isSsinMandatory(USER_MEMBER, r));

        // Internal and Manager
        r = new Registration();
        r.setProvider(Provider.INTERNAL);
        assertFalse(RegistrationService.isSsinMandatory(USER_MANAGER, r));
    }

    @Test
    public void testCheckBusinessErrorsOK()
    {
        User user;
        User member = null;
        Registration r;
        List<String> errors;

        // User = MANAGER (everything ok)
        user = new User("ALICE");
        user.addRole(Role.MANAGER);
        member = new User("BOB");
        member.setSector("sector");
        r = new Registration();
        r.setTitle("title");
        r.setPeriod(Period.DAY);
        r.setProvider(Provider.INTERNAL);
        r.setUrl("http://www.jorion.org");
        r.setPrice("123456");
        r.setBillable(YesNo.YES);
        r.setMotivation("motivation");
        errors = RegistrationService.checkBusinessErrors(user, member, r);
        assertEquals(0, errors.size());
        
        // User = Member, dates in the past
        user = new User("ALICE");
        user.addRole(Role.MEMBER);
        user.setSector("mysector");
        
        r = new Registration();
        r.setTitle("title");
        r.setProvider(Provider.CEVORA);
        r.setSsin("85073003328");
        r.setUrl("http://www.microsoft.com");
        r.setPrice("123456");
        r.setPeriod(Period.DAY);
        r.setTotalHour("0");
        
        LocalDate startDate = LocalDate.now().minusDays(1);
        r.setStartDate(startDate);
        r.setEndDate(startDate);
        
        errors = RegistrationService.checkBusinessErrors(user, user, r);
        assertEquals(0, errors.size());
    }

    @Test
    public void testCheckBusinessErrorsNOK()
    {
        User user;
        User member = null;
        Registration r;
        List<String> errors;

        // Provider = NONE
        user = new User("ALICE");
        r = new Registration();
        r.setPeriod(Period.DAY);
        errors = RegistrationService.checkBusinessErrors(user, null, r);
        assertTrue(errors.contains("provider"));
        assertTrue(errors.contains("title"));
        assertTrue(errors.contains("url"));
        assertEquals(3, errors.size());

        // Provider = CEVORA
        user = new User("ALICE");
        r = new Registration();
        r.setProvider(Provider.CEVORA);
        r.setUrl("http://www.jorion.org");
        errors = RegistrationService.checkBusinessErrors(user, null, r);
        assertTrue(errors.contains("title"));
        assertTrue(errors.contains("ssin"));
        assertTrue(errors.contains("period"));
        assertTrue(errors.contains("totalHour"));
        assertTrue(errors.contains("startDate"));
        assertTrue(errors.contains("endDate"));
        assertEquals(6, errors.size());

        // Provider = OTHER
        user = new User("ALICE");
        r = new Registration();
        r.setProvider(Provider.OTHER);
        r.setUrl("https://www.jorion.org");
        r.setPrice("123");
        errors = RegistrationService.checkBusinessErrors(user, null, r);
        assertTrue(errors.contains("provider.other"));
        assertTrue(errors.contains("title"));
        assertTrue(errors.contains("period"));
        assertEquals(3, errors.size());

        // User = MANAGER
        user = new User("ALICE");
        user.addRole(Role.MANAGER);
        member = new User("BOB");
        r = new Registration();
        r.setProvider(Provider.INTERNAL);
        r.setUrl("ldap://www.microsoft.com");
        r.setPrice("abc");
        errors = RegistrationService.checkBusinessErrors(user, member, r);
        assertTrue(errors.contains("sector"));
        assertTrue(errors.contains("title"));
        assertTrue(errors.contains("url"));
        assertTrue(errors.contains("period"));
        assertTrue(errors.contains("price"));
        assertTrue(errors.contains("billable"));
        assertTrue(errors.contains("motivation"));
        assertEquals(7, errors.size());

        // User = ADMIN
        user = new User("ALICE");
        user.addRole(Role.TRAINING);
        r = new Registration();
        r.setProvider(Provider.OTHER);
        errors = RegistrationService.checkBusinessErrors(user, null, r);
        assertTrue(errors.contains("provider.other"));
        assertTrue(errors.contains("title"));
        assertTrue(errors.contains("url"));
        assertTrue(errors.contains("period"));
        assertTrue(errors.contains("startDate"));
        assertTrue(errors.contains("endDate"));
        assertTrue(errors.contains("totalHour"));
        assertTrue(errors.contains("billable"));
        assertTrue(errors.contains("motivation"));
        assertEquals(9, errors.size());
    }
}
