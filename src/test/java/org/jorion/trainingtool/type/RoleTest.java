package org.jorion.trainingtool.type;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RoleTest {

    @Test
    public void testGetName() {
        assertEquals(Role.MEMBER.name(), Role.MEMBER.getName());
    }

    @Test
    public void testGetRoleName() {
        assertEquals(Role.ROLE + Role.MEMBER.name(), Role.MEMBER.getRoleName());
    }

    @Test
    public void testIsManager() {

        assertFalse(Role.MEMBER.isManager());
        assertTrue(Role.MANAGER.isManager());
        assertFalse(Role.HR.isManager());
        assertFalse(Role.TRAINING.isManager());
        assertFalse(Role.ADMIN.isManager());
    }

    @Test
    public void testIsSupervisor() {

        assertFalse(Role.MEMBER.isSupervisor());
        assertTrue(Role.MANAGER.isSupervisor());
        assertTrue(Role.HR.isSupervisor());
        assertTrue(Role.TRAINING.isSupervisor());
        assertTrue(Role.ADMIN.isSupervisor());
    }

    @Test
    public void testGetSupervisors() {

        String[] supervisors = Role.getSupervisors();
        List<String> list = Arrays.asList(supervisors);
        assertFalse(list.contains(Role.MEMBER.name()));
        assertTrue(list.contains(Role.MANAGER.name()));
        assertTrue(list.contains(Role.HR.name()));
        assertTrue(list.contains(Role.TRAINING.name()));
        assertTrue(list.contains(Role.ADMIN.name()));

        assertNotNull(Role.getSupervisors());
    }

    @Test
    public void testIsOffice() {

        assertFalse(Role.MEMBER.isOffice());
        assertFalse(Role.MANAGER.isOffice());
        assertTrue(Role.HR.isOffice());
        assertTrue(Role.TRAINING.isOffice());
        assertTrue(Role.ADMIN.isOffice());
    }

    @Test
    public void testIsHr() {

        assertFalse(Role.MEMBER.isHr());
        assertFalse(Role.MANAGER.isHr());
        assertTrue(Role.HR.isHr());
        assertFalse(Role.TRAINING.isHr());
        assertFalse(Role.ADMIN.isHr());
    }

    @Test
    public void testFindByValue() {

        assertTrue(Role.MEMBER == Role.findByValue(null));
        assertTrue(Role.MEMBER == Role.findByValue("abc"));
        assertTrue(Role.MEMBER == Role.findByValue(Role.MEMBER.name()));
        assertTrue(Role.MANAGER == Role.findByValue(Role.MANAGER.name()));
        assertTrue(Role.MANAGER == Role.findByValue(Role.MANAGER.getRoleName()));
    }

}
