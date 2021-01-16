package org.jorion.trainingtool.types;

import static org.jorion.trainingtool.types.RegistrationStatus.APPROVED_BY_PROVIDER;
import static org.jorion.trainingtool.types.RegistrationStatus.DRAFT;
import static org.jorion.trainingtool.types.RegistrationStatus.REFUSED_BY_HR;
import static org.jorion.trainingtool.types.RegistrationStatus.REFUSED_BY_MANAGER;
import static org.jorion.trainingtool.types.RegistrationStatus.REFUSED_BY_PROVIDER;
import static org.jorion.trainingtool.types.RegistrationStatus.REFUSED_BY_TRAINING;
import static org.jorion.trainingtool.types.RegistrationStatus.SUBMITTED_TO_HR;
import static org.jorion.trainingtool.types.RegistrationStatus.SUBMITTED_TO_MANAGER;
import static org.jorion.trainingtool.types.RegistrationStatus.SUBMITTED_TO_PROVIDER;
import static org.jorion.trainingtool.types.RegistrationStatus.SUBMITTED_TO_TRAINING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Unit test for {@link RegistrationStatus}
 */
public class RegistrationStatusTest
{
    // --- Methods ---
    @Test
    public void testDraft()
    {
        assertEquals(DRAFT.name(), DRAFT.getName());
        assertEquals(DRAFT.title, DRAFT.getTitle());
        assertFalse(DRAFT.isApproved());
        assertTrue(DRAFT.isPending());
        assertFalse(DRAFT.isPendingNotDraft());
        assertFalse(DRAFT.isRefused());
        assertTrue(DRAFT.isGTE(1));
        assertFalse(DRAFT.isGTE(2));
        assertTrue(DRAFT.isGTE(DRAFT));
        assertFalse(DRAFT.isGTE(SUBMITTED_TO_MANAGER));
        assertNull(DRAFT.getNextStatusAfterRefusal());
        assertEquals(SUBMITTED_TO_MANAGER, DRAFT.getNextStatusAfterApproval(false));
    }

    @Test
    public void testIsResponsible()
    {
        Set<Role> roles;

        roles = new HashSet<>();
        roles.add(Role.MEMBER);
        assertTrue(DRAFT.isResponsible(roles));

        roles = new HashSet<>();
        roles.add(Role.MANAGER);
        assertFalse(DRAFT.isResponsible(roles));

        roles = new HashSet<>();
        roles.add(Role.ADMIN);
        assertTrue(DRAFT.isResponsible(roles));
    }

    @Test
    public void testGetNextStatusAfterRefusal()
    {
        assertNull(APPROVED_BY_PROVIDER.getNextStatusAfterRefusal());

        assertEquals(REFUSED_BY_MANAGER, SUBMITTED_TO_MANAGER.getNextStatusAfterRefusal());
        assertEquals(REFUSED_BY_HR, SUBMITTED_TO_HR.getNextStatusAfterRefusal());
        assertEquals(REFUSED_BY_TRAINING, SUBMITTED_TO_TRAINING.getNextStatusAfterRefusal());
        assertEquals(REFUSED_BY_PROVIDER, SUBMITTED_TO_PROVIDER.getNextStatusAfterRefusal());
    }

    @Test
    public void testGetNextStatusAfterApproval()
    {
        assertNull(APPROVED_BY_PROVIDER.getNextStatusAfterApproval(false));

        assertEquals(SUBMITTED_TO_TRAINING, SUBMITTED_TO_MANAGER.getNextStatusAfterApproval(false));
        assertEquals(SUBMITTED_TO_HR, SUBMITTED_TO_MANAGER.getNextStatusAfterApproval(true));
        assertEquals(SUBMITTED_TO_TRAINING, SUBMITTED_TO_HR.getNextStatusAfterApproval(false));
        assertEquals(SUBMITTED_TO_PROVIDER, SUBMITTED_TO_TRAINING.getNextStatusAfterApproval(false));
        assertEquals(APPROVED_BY_PROVIDER, SUBMITTED_TO_PROVIDER.getNextStatusAfterApproval(false));
    }
}
