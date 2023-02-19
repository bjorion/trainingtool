package org.jorion.trainingtool.service;

import org.jorion.trainingtool.common.AuthenticationUtils;
import org.jorion.trainingtool.common.EntityUtils;
import org.jorion.trainingtool.entity.User;
import org.jorion.trainingtool.repository.IUserRepository;
import org.jorion.trainingtool.type.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private static final String USERNAME = "jdoe";
    private static final String MANAGERNAME = "manager";
    private static final String DN = "basedn=OU=Users,OU=BE,OU=Landlord NL,OU=Corporate,DC=groupinfra,DC=com";

    @Mock
    private IUserRepository mockUserRepository;

    @Mock
    private LdapService mockLdapService;

    @AfterEach
    void cleanUp() {
        AuthenticationUtils.cleanAuthentication();
    }

    @Test
    void testGetSectors() {

        UserService service = new UserService();
        String[] sectors = {"Governement", "Defense"};
        ReflectionTestUtils.setField(service, "sectors", sectors);
        List<String> list = service.getSectors();
        assertEquals(2, list.size());
        // list should be immutable
        assertThrows(UnsupportedOperationException.class, () -> list.clear());
    }

    @Test
    void testFindUserByUserName() {

        UserService service = new UserService();
        ReflectionTestUtils.setField(service, "userRepository", mockUserRepository);
        when(mockUserRepository.findUserByUserName("jdoe")).thenReturn(Optional.empty());
        assertNull(service.findUserByUserName("jdoe"));
    }

    @Test
    void testFindUserByUserNameOrCreate_NewUser() {

        User user = EntityUtils.createUser(USERNAME);
        user.setFirstName("John");
        user.setLastName("Doe");

        UserService service = new UserService();
        ReflectionTestUtils.setField(service, "userRepository", mockUserRepository);
        ReflectionTestUtils.setField(service, "ldapService", mockLdapService);
        when(mockUserRepository.findUserByUserName("jdoe")).thenReturn(Optional.empty());
        when(mockLdapService.searchByName(USERNAME)).thenReturn(user);
        when(mockUserRepository.save(user)).thenReturn(user);
        User member = service.findUserByUserNameOrCreate(USERNAME);
        assertEquals(user, member);
    }

    @Test
    void testFindUserByUserNameOrCreate_ExistingUser() {

        User user = EntityUtils.createUser(USERNAME);
        user.setFirstName("John");
        user.setLastName("Doe");
        ReflectionTestUtils.setField(user, "modifiedOn", LocalDateTime.of(1970, 1, 1, 0, 0));

        UserService service = new UserService();
        ReflectionTestUtils.setField(service, "userRepository", mockUserRepository);
        ReflectionTestUtils.setField(service, "ldapService", mockLdapService);
        when(mockUserRepository.findUserByUserName("jdoe")).thenReturn(Optional.of(user));
        when(mockLdapService.searchByName(USERNAME)).thenReturn(user);
        when(mockUserRepository.save(user)).thenReturn(user);
        User member = service.findUserByUserNameOrCreate(USERNAME);
        assertEquals(user, member);
    }

    @Test
    void testGetPrincipalName() {

        AuthenticationUtils.setAuthentication(USERNAME);
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
    void testIsAuthorized() {

        assertFalse(UserService.isAuthorized(null, null));

        User hr = EntityUtils.createUserBuilder("hr").build().addRole(Role.HR);
        User manager = EntityUtils.createUserBuilder(MANAGERNAME).build().addRole(Role.MANAGER);
        User member = EntityUtils.createUserBuilder(USERNAME).managerName(MANAGERNAME).build().addRole(Role.MEMBER);
        User member2 = EntityUtils.createUserBuilder(USERNAME + "2").managerName(MANAGERNAME).build().addRole(Role.MEMBER);

        assertTrue(UserService.isAuthorized(hr, member));
        assertTrue(UserService.isAuthorized(manager, manager));
        assertTrue(UserService.isAuthorized(manager, member));

        assertTrue(UserService.isAuthorized(member, member));
        assertFalse(UserService.isAuthorized(member, member2));
        assertFalse(UserService.isAuthorized(member, manager));
        assertFalse(UserService.isAuthorized(member, hr));
    }
}
