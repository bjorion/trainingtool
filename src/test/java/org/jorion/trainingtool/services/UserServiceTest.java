package org.jorion.trainingtool.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.test.util.ReflectionTestUtils;

import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.repositories.IUserRepository;
import org.jorion.trainingtool.util.TestingUtils;

/**
 * Unit test for {@link UserService}.
 */
public class UserServiceTest
{
    // --- Constants ---
    private static final String USERNAME = "jdoe";

    private static final String DN = "basedn=OU=Users,OU=BE,OU=Landlord NL,OU=Corporate,DC=groupinfra,DC=com";

    // --- Variables ---
    @Mock
    IUserRepository mockUserRepository;

    @Mock
    LdapService mockLdapService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    // --- Methods ---
    @Before
    public void setUp()
    {
        TestingUtils.cleanAuthentication();
    }

    @After
    public void cleanUp()
    {
        TestingUtils.cleanAuthentication();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetSectors()
    {
        UserService service = new UserService();
        String[] sectors = { "Governement", "Defense" };
        ReflectionTestUtils.setField(service, "sectors", sectors);
        List<String> list = service.getSectors();
        assertEquals(2, list.size());
        // list should be immutable
        list.clear();
    }

    @Test
    public void testFindUserByUsername()
    {
        UserService service = new UserService();
        ReflectionTestUtils.setField(service, "userRepository", mockUserRepository);
        when(mockUserRepository.findUserByUsername("jdoe")).thenReturn(Optional.empty());
        assertNull(service.findUserByUsername("jdoe"));
    }

    @Test
    public void testFindUserByUsernameOrCreate_NewUser()
    {
        User user = new User(USERNAME);
        user.setFirstname("John");
        user.setLastname("Doe");

        UserService service = new UserService();
        ReflectionTestUtils.setField(service, "userRepository", mockUserRepository);
        ReflectionTestUtils.setField(service, "ldapService", mockLdapService);
        when(mockUserRepository.findUserByUsername("jdoe")).thenReturn(Optional.empty());
        when(mockLdapService.searchByName(USERNAME)).thenReturn(user);
        when(mockUserRepository.save(user)).thenReturn(user);
        User member = service.findUserByUsernameOrCreate(USERNAME);
        assertEquals(user, member);
    }

    @Test
    public void testFindUserByUsernameOrCreate_ExistingUser()
    {
        User user = new User(USERNAME);
        user.setFirstname("John");
        user.setLastname("Doe");
        ReflectionTestUtils.setField(user, "modifiedOn", LocalDateTime.of(1970, 1, 1, 0, 0));

        UserService service = new UserService();
        ReflectionTestUtils.setField(service, "userRepository", mockUserRepository);
        ReflectionTestUtils.setField(service, "ldapService", mockLdapService);
        when(mockUserRepository.findUserByUsername("jdoe")).thenReturn(Optional.of(user));
        when(mockLdapService.searchByName(USERNAME)).thenReturn(user);
        when(mockUserRepository.save(user)).thenReturn(user);
        User member = service.findUserByUsernameOrCreate(USERNAME);
        assertEquals(user, member);
    }

    @Test
    public void testGetPrincipalName()
    {
        TestingUtils.setAuthentication(USERNAME);
        assertEquals(USERNAME, UserService.getPrincipalName());

        LdapUserDetailsImpl.Essence essence = new LdapUserDetailsImpl.Essence();
        essence.setUsername(USERNAME);
        essence.setDn(DN);
        LdapUserDetails ldap = essence.createUserDetails();
        Authentication authentication = new TestingAuthenticationToken(ldap, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertEquals(USERNAME, UserService.getPrincipalName());
    }

    @Test
    public void testCheckBusinessErrors()
    {
        User user = new User();
        assertFalse(UserService.checkBusinessErrors(user).isEmpty());

        user = new User();
        user.setFirstname(" * ");
        user.setLastname(" * ");
        assertFalse(UserService.checkBusinessErrors(user).isEmpty());

        user = new User();
        user.setFirstname("John");
        user.setLastname(" * ");
        assertTrue(UserService.checkBusinessErrors(user).isEmpty());

        user = new User();
        user.setFirstname(" * ");
        user.setLastname(" Doe ");
        assertTrue(UserService.checkBusinessErrors(user).isEmpty());

        user = new User();
        user.setFirstname("John");
        user.setLastname("Doe");
        assertTrue(UserService.checkBusinessErrors(user).isEmpty());
    }

}
