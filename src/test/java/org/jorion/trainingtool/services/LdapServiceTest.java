package org.jorion.trainingtool.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.test.util.ReflectionTestUtils;

import org.jorion.trainingtool.common.Constants;
import org.jorion.trainingtool.config.ldap.IdentityAttributeMapper;
import org.jorion.trainingtool.dto.GroupinfraIdentityDTO;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.types.Role;

/**
 * Unit tests for {@link LdapService}.
 */
public class LdapServiceTest
{
    // --- Constants ---
    private static final String FIRSTNAME = "John";

    private static final String LASTNAME = "DOE";

    private static final String USERNAME = "jdoe";

    private static final String UNKNOWN = "unknown";

    // --- Variables ---
    @Mock
    LdapTemplate mockLdapTemplate;

    @Mock
    IdentityAttributeMapper mockMapper;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    // --- Methods ---
    @Test
    public void testSearchByName()
    {
        LdapService service = new LdapService();
        List<GroupinfraIdentityDTO> dtos = null;
        ReflectionTestUtils.setField(service, "ldap", true);
        ReflectionTestUtils.setField(service, "ldapTemplate", mockLdapTemplate);
        ReflectionTestUtils.setField(service, "identityAttributeMapper", mockMapper);

        // not found
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter(Constants.COMMON_NAME, UNKNOWN));

        dtos = new ArrayList<>();
        when(mockLdapTemplate.search("", filter.encode(), mockMapper)).thenReturn(dtos);
        assertNull(service.searchByName(UNKNOWN));
        verify(mockLdapTemplate, times(1)).search("", filter.encode(), mockMapper);

        // found 1
        filter = new AndFilter();
        filter.and(new EqualsFilter(Constants.COMMON_NAME, USERNAME));
        dtos = new ArrayList<>();
        GroupinfraIdentityDTO dto = new GroupinfraIdentityDTO();
        dto.setAccount(USERNAME);
        dto.setFirstname(FIRSTNAME);
        dto.setLastname(LASTNAME);
        dto.addRole(Role.MEMBER);
        dtos.add(dto);

        when(mockLdapTemplate.search("", filter.encode(), mockMapper)).thenReturn(dtos);
        User user = service.searchByName(USERNAME);
        assertNotNull(user);
        assertEquals(USERNAME, user.getUsername());
        verify(mockLdapTemplate, times(1)).search("", filter.encode(), mockMapper);
    }

    @Test
    public void testSearchByExample()
    {
        LdapService service = new LdapService();
        List<GroupinfraIdentityDTO> dtos = null;
        ReflectionTestUtils.setField(service, "ldap", true);
        ReflectionTestUtils.setField(service, "ldapTemplate", mockLdapTemplate);
        ReflectionTestUtils.setField(service, "identityAttributeMapper", mockMapper);

        User srcUser = new User();
        srcUser.setFirstname(FIRSTNAME);
        srcUser.setLastname(LASTNAME);

        AndFilter filter = new AndFilter();
        filter.and(new LikeFilter(Constants.SURNAME, LASTNAME));
        filter.and(new LikeFilter(Constants.GIVEN_NAME, FIRSTNAME));
        
        dtos = new ArrayList<>();
        GroupinfraIdentityDTO dto = new GroupinfraIdentityDTO();
        dto.setAccount(USERNAME);
        dto.setFirstname(FIRSTNAME);
        dto.setLastname(LASTNAME);
        dto.addRole(Role.MEMBER);
        dtos.add(dto);
        
        when(mockLdapTemplate.search("", filter.encode(), mockMapper)).thenReturn(dtos);
        List<User> users = service.searchByExample(srcUser);
        assertNotNull(users);
        assertTrue(users.size() == 1);
        User user = users.get(0);
        assertEquals(USERNAME, user.getUsername());
        verify(mockLdapTemplate, times(1)).search("", filter.encode(), mockMapper);

    }
}
