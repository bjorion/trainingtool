package org.jorion.trainingtool.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.jorion.trainingtool.types.Role;

/**
 * Unit tests for {@link User}.
 */
public class UserTest
{
    // --- Constants ---
    private static final String USERNAME = "hari.seldon";

    // --- Methods ---
    @Test
    public void testUser()
    {
        User user = new User();
        user.setUsername(USERNAME);
        assertEquals(USERNAME, user.getUsername());
        assertNotNull(user.toString());
    }

    @Test
    public void testHashCode()
    {
        User user1 = new User(USERNAME);
        User user2 = new User("dummy");
        assertNotEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void testEquals()
    {
        User user1 = new User(USERNAME);
        User user2 = new User("dummy");
        User user3 = new User(USERNAME);

        assertFalse(user1.equals(null));
        assertFalse(user1.equals(new Object()));
        assertTrue(user1.equals(user1));
        assertFalse(user1.equals(user2));
        assertTrue(user1 != user3);
        assertTrue(user1.equals(user3));
    }

    @Test
    public void testMember()
    {
        User user = new User(USERNAME);
        user.addRole(Role.MEMBER);
        assertFalse(user.isSupervisor());
        assertFalse(user.isHr());
        assertFalse(user.isTraining());
        assertFalse(user.isAdmin());
    }

    @Test
    public void testManager()
    {
        User user = new User(USERNAME);
        user.addRole(Role.MANAGER);
        assertTrue(user.isSupervisor());
        assertFalse(user.isHr());
        assertFalse(user.isTraining());
        assertFalse(user.isAdmin());
    }

    @Test
    public void testHR()
    {
        User user = new User(USERNAME);
        user.addRole(Role.HR);
        assertTrue(user.isSupervisor());
        assertTrue(user.isHr());
        assertFalse(user.isTraining());
        assertFalse(user.isAdmin());
    }

    @Test
    public void testTraining()
    {
        User user = new User(USERNAME);
        user.addRole(Role.TRAINING);
        assertTrue(user.isSupervisor());
        assertFalse(user.isHr());
        assertTrue(user.isTraining());
        assertFalse(user.isAdmin());
    }

    @Test
    public void testAdmin()
    {
        User user = new User(USERNAME);
        user.addRole(Role.ADMIN);
        assertTrue(user.isSupervisor());
        assertFalse(user.isHr());
        assertFalse(user.isTraining());
        assertTrue(user.isAdmin());
    }

    @Test
    public void testGetRegistrations()
    {
        User user = new User(USERNAME);
        assertNotNull(user.getRegistrations());
        assertTrue(user.getRegistrations().isEmpty());
    }

    @Test
    public void testGetFullname()
    {
        User user = new User(USERNAME);
        user.setLastname("doe");
        user.setFirstname("john");
        assertEquals("john DOE", user.getFullname());
    }

    @Test
    public void testConvertFrom()
    {
        User userFrom = new User(USERNAME);
        userFrom.setSector("mysector");

        // check that not-empty fields are copied
        User userTo = new User(USERNAME);
        userTo.convertFrom(userFrom);
        assertEquals("mysector", userTo.getSector());

        // check that empty fields are not copied
        userFrom = new User(USERNAME);
        userTo = new User(USERNAME);
        userTo.setSector("mysector");
        userTo.convertFrom(userFrom);
        assertEquals("mysector", userTo.getSector());
    }

    @Test
    public void testAddRegistrationOk()
    {
        User user = new User(USERNAME);
        user.addRegistration(new Registration(1L));
        assertNotNull(user.findRegistrationById(1L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddRegistrationNok()
    {
        User user = new User(USERNAME);
        user.addRegistration(new Registration(1L));
        user.addRegistration(new Registration(1L));
    }

    @Test
    public void testFindRegistrationById()
    {
        User user = new User(USERNAME);
        user.addRegistration(new Registration(1L));
        user.addRegistration(new Registration(2L));

        assertNull(user.findRegistrationById(null));
        assertNull(user.findRegistrationById(10L));
        assertEquals((Long) 1L, user.findRegistrationById(1L).getId());
        assertEquals((Long) 2L, user.findRegistrationById(2L).getId());
    }

    @Test
    public void testRemoveRegistration()
    {
        User user = new User(USERNAME);
        Registration r = new Registration(1L);
        user.addRegistration(r);
        assertTrue(user.removeRegistration(r));
        assertFalse(user.removeRegistration(r));
    }

    @Test
    public void testReplaceRegistration()
    {
        User user = new User(USERNAME);
        Registration r = new Registration(1L);
        r.setTitle("TITLE_1");
        user.addRegistration(r);

        r = new Registration(1L);
        r.setTitle("TITLE_2");
        assertTrue(user.replaceRegistration(r));
        assertEquals("TITLE_2", user.findRegistrationById(1L).getTitle());

        r = new Registration(2L);
        assertFalse(user.replaceRegistration(r));
    }

}
