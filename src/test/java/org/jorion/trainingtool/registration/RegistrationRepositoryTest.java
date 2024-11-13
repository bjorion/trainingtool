package org.jorion.trainingtool.registration;

import org.jorion.trainingtool.type.RegistrationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("local")
public class RegistrationRepositoryTest {

    private static final String USER_NAME = "john.doe";
    private static final String MANAGER_NAME = "manager";

    @Autowired
    private IRegistrationRepository repo;

    @Test
    void testFindRegistrationByIdAndUser() {

        var r = repo.findRegistrationByIdAndUser(1L, USER_NAME).orElse(null);
        assertNotNull(r);
        assertEquals(1L, r.getId());
        assertTrue(r.belongsTo(USER_NAME));
    }

    @Test
    void testFindRegistrationByIdAndManager() {

        var r = repo.findRegistrationByIdAndManager(1L, MANAGER_NAME).orElse(null);
        assertNotNull(r);
        assertEquals(1L, r.getId());
        assertTrue(r.belongsTo(USER_NAME));
    }

    @Test
    void testFindRegistrationsByStatus() {

        final EnumSet<RegistrationStatus> statuses = EnumSet.of(RegistrationStatus.SUBMITTED_TO_MANAGER);

        var regs = repo.findRegistrationsByStatuses(statuses);
        for (Registration r : regs) {
            assertTrue(statuses.contains(r.getStatus()));
        }
    }

    @Test
    void testFindRegistrationsByStatusesAndManager_manager() {

        final EnumSet<RegistrationStatus> statuses = EnumSet.of(RegistrationStatus.SUBMITTED_TO_MANAGER);

        var regs = repo.findRegistrationsByStatusesAndManager(statuses, MANAGER_NAME);
        for (Registration r : regs) {
            assertTrue(statuses.contains(r.getStatus()));
            assertEquals(MANAGER_NAME, r.getMember().getManagerName());
        }
    }

    @Test
    void testFindAllByExample() {

        var registrations = repo.findAllByExample("doe", null, RegistrationStatus.DRAFT, null, null);
        assertFalse(registrations.isEmpty());
    }

    @Test
    void testFindByManagerName() {

        var pageable = Pageable.ofSize(25);
        var registrations = repo.findByManagerName(MANAGER_NAME, pageable);
        assertFalse(registrations.isEmpty());
    }

    @Test
    void testFindByUserName() {

        var pageable = Pageable.ofSize(25);
        var registrations = repo.findByUserName(USER_NAME, pageable);
        assertFalse(registrations.isEmpty());
    }
}
