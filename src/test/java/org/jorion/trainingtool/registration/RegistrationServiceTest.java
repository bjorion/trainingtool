package org.jorion.trainingtool.registration;

import org.jorion.trainingtool.event.EventPublisherService;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.jorion.trainingtool.type.Role;
import org.jorion.trainingtool.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.jorion.trainingtool.registration.RandomRegistration.createRegistration;
import static org.jorion.trainingtool.type.RegistrationStatus.*;
import static org.jorion.trainingtool.user.RandomUser.createUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    private static final User USER_MEMBER = createUser("ALICE");
    private static final User USER_MANAGER = createUser("BOB");
    private static final User USER_HR = createUser("CHARLES");
    private static final User USER_MANAGER_HR = createUser("DAVID");
    private static final User USER_TRAINING = createUser("TOM");

    @Mock
    private IRegistrationRepository registrationRepository;

    @Mock
    private EventPublisherService eventPublisher;

    @InjectMocks
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {

        USER_MEMBER.addRole(Role.MEMBER);
        USER_MANAGER.addRole(Role.MANAGER);
        USER_HR.addRole(Role.HR);
        USER_MANAGER_HR.addRole(Role.MANAGER).addRole(Role.HR);
        USER_TRAINING.addRole(Role.TRAINING);
    }

    @Test
    void testFindById_user() {

        Long regId = 1L;
        User user = USER_MEMBER;
        assertNull(registrationService.findById(user, regId));
        verify(registrationRepository, never()).findById(regId);
        verify(registrationRepository, never()).findRegistrationByIdAndManager(regId, user.getUserName());
        verify(registrationRepository, times(1)).findRegistrationByIdAndUser(regId, user.getUserName());
    }

    @Test
    void testFindById_manager() {

        Long regId = 1L;
        User user = USER_MANAGER;
        assertNull(registrationService.findById(user, regId));
        verify(registrationRepository, never()).findById(regId);
        verify(registrationRepository, times(1)).findRegistrationByIdAndManager(regId, user.getUserName());
        verify(registrationRepository, never()).findRegistrationByIdAndUser(regId, user.getUserName());
    }

    @Test
    void testFindById_supervisor() {

        Long regId = 1L;
        User user = USER_TRAINING;
        assertNull(registrationService.findById(user, regId));
        verify(registrationRepository, times(1)).findById(regId);
        verify(registrationRepository, never()).findRegistrationByIdAndManager(regId, user.getUserName());
        verify(registrationRepository, never()).findRegistrationByIdAndUser(regId, user.getUserName());
    }

    @Test
    void testFindPendingByUser_manager() {

        EnumSet<RegistrationStatus> statuses;

        // check for a manager
        statuses = EnumSet.of(SUBMITTED_TO_MANAGER);
        List<Registration> list = registrationService.findPendingByUser(USER_MANAGER);
        assertTrue(list.isEmpty());
        verify(registrationRepository, times(1)).findRegistrationsByStatusesAndManager(statuses, USER_MANAGER.getUserName());
        verify(registrationRepository, times(0)).findRegistrationsByStatuses(statuses);
    }

    @Test
    void testFindPendingByUser_hr() {

        EnumSet<RegistrationStatus> statuses;

        // check for an HR that's not a manager
        statuses = EnumSet.of(SUBMITTED_TO_HR);
        List<Registration> list = registrationService.findPendingByUser(USER_HR);
        assertTrue(list.isEmpty());
        verify(registrationRepository, times(0)).findRegistrationsByStatusesAndManager(statuses, USER_HR.getUserName());
        verify(registrationRepository, times(1)).findRegistrationsByStatuses(statuses);
    }

    @Test
    void testFindPendingByUser_managerAndHr() {

        Registration r1 = createRegistration(USER_MEMBER, SUBMITTED_TO_MANAGER);
        Registration r2 = createRegistration(USER_MEMBER, SUBMITTED_TO_HR);
        List<Registration> list1 = new ArrayList<>();
        list1.add(r1);
        List<Registration> list2 = new ArrayList<>();
        list1.add(r2);

        // check for an HR that's also a manager
        EnumSet<RegistrationStatus> managerStatuses = EnumSet.of(SUBMITTED_TO_MANAGER);
        EnumSet<RegistrationStatus> hrStatuses = EnumSet.of(SUBMITTED_TO_HR);
        when(registrationRepository.findRegistrationsByStatusesAndManager(managerStatuses, USER_MANAGER_HR.getUserName())).thenReturn(list1);
        when(registrationRepository.findRegistrationsByStatuses(hrStatuses)).thenReturn(list2);

        List<Registration> list = registrationService.findPendingByUser(USER_MANAGER_HR);
        assertEquals(list1.size() + list2.size(), list.size());
        verify(registrationRepository, times(1)).findRegistrationsByStatusesAndManager(managerStatuses, USER_MANAGER_HR.getUserName());
        verify(registrationRepository, times(1)).findRegistrationsByStatuses(hrStatuses);
    }

    @Test
    void testComputeStats() {

        List<Registration> regs = new ArrayList<>();
        regs.add(createRegistration(USER_MEMBER, DRAFT));
        regs.add(createRegistration(USER_MEMBER, SUBMITTED_TO_MANAGER));
        regs.add(createRegistration(USER_MEMBER, SUBMITTED_TO_HR));
        regs.add(createRegistration(USER_MEMBER, SUBMITTED_TO_MANAGER));
        when(registrationRepository.findAll()).thenReturn(regs);

        Map<RegistrationStatus, Integer> map = registrationService.computeStats();
        assertEquals(1, map.get(DRAFT));
        assertEquals(2, map.get(SUBMITTED_TO_MANAGER));
        assertEquals(1, map.get(SUBMITTED_TO_HR));
    }

    @Test
    void testUpdateRegistrationStatus() {

        Registration r = new Registration(1L);
        r.setStatus(DRAFT);
        r.setMember(USER_MEMBER);
        r.setCreatedBy("ALICE");
        r.setJustification("Approved by Manager");
        Optional<Registration> optReg = Optional.of(r);

        when(registrationRepository.findRegistrationByIdAndUser(1L, "ALICE")).thenReturn(optReg);
        when(registrationRepository.save(r)).thenReturn(r);

        Registration result = registrationService.updateRegistrationStatus(USER_MEMBER, 1L, RegistrationEvent.SUBMIT, "Rejected by HR");
        assertEquals(SUBMITTED_TO_MANAGER, result.getStatus());
        assertTrue(result.getJustification().startsWith("Rejected by HR"));
        verify(eventPublisher, times(1)).publishUpdateEvent(RegistrationEvent.SUBMIT, r, USER_MEMBER);
    }

    @Test
    void testIsDeletable() {

        Registration r = createRegistration(USER_MEMBER, DRAFT);
        assertTrue(RegistrationService.isDeletable(USER_MEMBER, r));

        r = createRegistration(USER_MEMBER, REFUSED_BY_MANAGER);
        assertTrue(RegistrationService.isDeletable(USER_MEMBER, r));

        // wrong status
        r = createRegistration(USER_MEMBER, SUBMITTED_TO_MANAGER);
        assertFalse(RegistrationService.isDeletable(USER_MEMBER, r));

        // wrong user
        r = createRegistration(USER_MEMBER, DRAFT);
        assertFalse(RegistrationService.isDeletable(USER_MANAGER, r));

        r = createRegistration(USER_MEMBER, REFUSED_BY_MANAGER);
        assertFalse(RegistrationService.isDeletable(USER_MANAGER, r));
    }

    @Test
    void testIsEditable() {

        // status = draft; principal = registration user
        Registration r = createRegistration(USER_MEMBER, DRAFT);
        assertTrue(RegistrationService.isEditable(USER_MEMBER, r));

        r = createRegistration(USER_MEMBER, REFUSED_BY_MANAGER);
        assertFalse(RegistrationService.isEditable(USER_MEMBER, r));

        r = createRegistration(USER_MEMBER, APPROVED_BY_PROVIDER);
        assertFalse(RegistrationService.isEditable(USER_MEMBER, r));

        r = createRegistration(USER_MEMBER, APPROVED_BY_PROVIDER);
        assertTrue(RegistrationService.isEditable(USER_TRAINING, r));
    }

    @Test
    void testIsApprovable() {

        // draft and owner
        Registration r = createRegistration(USER_MEMBER, DRAFT);
        r.setCreatedBy("ALICE");
        assertTrue(RegistrationService.isApprovable(USER_MEMBER, r));

        // draft and not owner
        r = createRegistration(USER_MEMBER, DRAFT);
        r.setCreatedBy("ALICE");
        assertFalse(RegistrationService.isApprovable(USER_MANAGER, r));

        // pending, principal ok
        r = createRegistration(USER_MEMBER, SUBMITTED_TO_MANAGER);
        r.setCreatedBy("ALICE");
        assertTrue(RegistrationService.isApprovable(USER_MANAGER, r));

        // not pending, principal ok
        r = createRegistration(USER_MEMBER, REFUSED_BY_MANAGER);
        r.setCreatedBy("ALICE");
        assertFalse(RegistrationService.isApprovable(USER_MANAGER, r));

        // pending, principal ok but belongs to him
        r = createRegistration(USER_MANAGER, SUBMITTED_TO_MANAGER);
        r.setCreatedBy("BOB");
        assertFalse(RegistrationService.isApprovable(USER_MANAGER, r));
    }

    @Test
    void testIsSubmittable() {

        // draft and owner
        Registration r = createRegistration(USER_MEMBER, DRAFT);
        assertTrue(RegistrationService.isSubmittable(USER_MEMBER, r));

        // draft and not owner
        r = createRegistration(USER_MEMBER, DRAFT);
        assertFalse(RegistrationService.isSubmittable(USER_MANAGER, r));

        // pending, principal ok
        r = createRegistration(USER_MEMBER, SUBMITTED_TO_MANAGER);
        assertFalse(RegistrationService.isSubmittable(USER_MEMBER, r));

        // not pending, principal ok
        r = createRegistration(USER_MEMBER, REFUSED_BY_MANAGER);
        assertFalse(RegistrationService.isSubmittable(USER_MEMBER, r));
    }

    @Test
    void testIsAcceptable() {

        Registration r = new Registration();
        r.setCreatedBy("ALICE");
        assertTrue(RegistrationService.isAcceptable(USER_MANAGER, r));

        r = new Registration();
        r.setCreatedBy("BOB");
        assertFalse(RegistrationService.isAcceptable(USER_MANAGER, r));
    }

    @Test
    void testIsRefusable() {

        Registration r = createRegistration(USER_MEMBER, SUBMITTED_TO_MANAGER);
        assertTrue(RegistrationService.isRefusable(USER_MANAGER, r));

        // wrong status
        r = createRegistration(USER_MEMBER, SUBMITTED_TO_HR);
        assertFalse(RegistrationService.isRefusable(USER_MANAGER, r));

        // wrong user
        r = createRegistration(USER_MEMBER, SUBMITTED_TO_MANAGER);
        assertFalse(RegistrationService.isRefusable(USER_MEMBER, r));

        // both
        r = createRegistration(USER_MEMBER, SUBMITTED_TO_HR);
        assertFalse(RegistrationService.isRefusable(USER_MEMBER, r));
    }

    @Test
    void testIsRejectable() {

        Registration r = createRegistration(USER_MEMBER, SUBMITTED_TO_MANAGER);
        assertTrue(RegistrationService.isRejectable(USER_MANAGER, r));

        // wrong status
        r = createRegistration(USER_MEMBER, SUBMITTED_TO_HR);
        assertFalse(RegistrationService.isRejectable(USER_MANAGER, r));

        // wrong user
        r = createRegistration(USER_MEMBER, SUBMITTED_TO_MANAGER);
        assertFalse(RegistrationService.isRejectable(USER_MEMBER, r));
    }

}
