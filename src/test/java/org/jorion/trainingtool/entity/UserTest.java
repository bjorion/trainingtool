package org.jorion.trainingtool.entity;

import org.jorion.trainingtool.common.EntityUtils;
import org.jorion.trainingtool.type.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private static final String USERNAME = "hari.seldon";

    @Test
    void testUser() {

        User user = new User();
        user.setUserName(USERNAME);
        assertEquals(USERNAME, user.getUserName());
        assertNotNull(user.toString());
    }

    @Test
    void testHashCode() {

        User user1 = EntityUtils.createUser(USERNAME);
        User user2 = EntityUtils.createUser("dummy");
        assertNotEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testEquals() {

        User user1 = EntityUtils.createUser(USERNAME);
        User user2 = EntityUtils.createUser("dummy");
        User user3 = EntityUtils.createUser(USERNAME);

        assertNotEquals(null, user1);
        assertNotEquals(user1, new Object());
        assertNotEquals(user1, user2);
        assertNotSame(user1, user3);
        assertEquals(user1, user3);
    }

    @Test
    void testMember() {
        User user = EntityUtils.createUser(USERNAME);
        user.addRole(Role.MEMBER);
        assertFalse(user.isSupervisor());
        assertFalse(user.isHr());
        assertFalse(user.isTraining());
        assertFalse(user.isAdmin());
    }

    @Test
    void testManager() {
        User user = EntityUtils.createUser(USERNAME);
        user.addRole(Role.MANAGER);
        assertTrue(user.isSupervisor());
        assertFalse(user.isHr());
        assertFalse(user.isTraining());
        assertFalse(user.isAdmin());
    }

    @Test
    void testHR() {
        User user = EntityUtils.createUser(USERNAME);
        user.addRole(Role.HR);
        assertTrue(user.isSupervisor());
        assertTrue(user.isHr());
        assertFalse(user.isTraining());
        assertFalse(user.isAdmin());
    }

    @Test
    void testTraining() {
        User user = EntityUtils.createUser(USERNAME);
        user.addRole(Role.TRAINING);
        assertTrue(user.isSupervisor());
        assertFalse(user.isHr());
        assertTrue(user.isTraining());
        assertFalse(user.isAdmin());
    }

    @Test
    void testAdmin() {
        User user = EntityUtils.createUser(USERNAME);
        user.addRole(Role.ADMIN);
        assertTrue(user.isSupervisor());
        assertFalse(user.isHr());
        assertFalse(user.isTraining());
        assertTrue(user.isAdmin());
    }

    @Test
    void testGetRegistrations() {
        User user = EntityUtils.createUser(USERNAME);
        assertNotNull(user.getRegistrations());
        assertTrue(user.getRegistrations().isEmpty());
    }

    @Test
    void testGetFullname() {
        User user = EntityUtils.createUser(USERNAME);
        user.setLastName("doe");
        user.setFirstName("john");
        assertEquals("john DOE", user.getFullName());
    }

    @Test
    void testConvertFrom() {
        User userFrom = EntityUtils.createUser(USERNAME);
        userFrom.setSector("mysector");

        // check that not-empty fields are copied
        User userTo = EntityUtils.createUser(USERNAME);
        userTo.convertFrom(userFrom);
        assertEquals("mysector", userTo.getSector());

        // check that empty fields are not copied
        userFrom = EntityUtils.createUser(USERNAME);
        userTo = EntityUtils.createUser(USERNAME);
        userTo.setSector("mysector");
        userTo.convertFrom(userFrom);
        assertEquals("mysector", userTo.getSector());
    }

    @Test
    void testAddRegistrationOk() {
        User user = EntityUtils.createUser(USERNAME);
        user.addRegistration(new Registration(1L));
        assertNotNull(user.findRegistrationById(1L));
    }

    void testAddRegistrationNok() {
        User user = EntityUtils.createUser(USERNAME);
        user.addRegistration(new Registration(1L));
        assertThrows(IllegalArgumentException.class, () -> user.addRegistration(new Registration(1L)));
    }

    @Test
    void testFindRegistrationById() {
        User user = EntityUtils.createUser(USERNAME);
        user.addRegistration(new Registration(1L));
        user.addRegistration(new Registration(2L));

        assertNull(user.findRegistrationById(null));
        assertNull(user.findRegistrationById(10L));
        assertEquals((Long) 1L, user.findRegistrationById(1L).getId());
        assertEquals((Long) 2L, user.findRegistrationById(2L).getId());
    }

    @Test
    void testRemoveRegistration() {
        User user = EntityUtils.createUser(USERNAME);
        Registration r = new Registration(1L);
        user.addRegistration(r);
        assertTrue(user.removeRegistration(r));
        assertFalse(user.removeRegistration(r));
    }

    @Test
    void testReplaceRegistration() {
        User user = EntityUtils.createUser(USERNAME);
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
