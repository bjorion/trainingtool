package org.jorion.trainingtool.ldap;

import org.jorion.trainingtool.common.LdapConstants;
import org.jorion.trainingtool.user.User;
import org.jorion.trainingtool.type.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LdapServiceTest {

    private static final String FIRSTNAME = "John";
    private static final String LASTNAME = "DOE";
    private static final String USERNAME = "jdoe";
    private static final String UNKNOWN = "unknown";

    @Mock
    private LdapTemplate mockLdapTemplate;

    @Mock
    private IdentityAttributeMapper mockMapper;

    @Test
    void testSearchByName() {

        LdapService service = new LdapService();
        List<GroupInfraIdentityDTO> dtos;
        ReflectionTestUtils.setField(service, "ldap", true);
        ReflectionTestUtils.setField(service, "ldapTemplate", mockLdapTemplate);
        ReflectionTestUtils.setField(service, "identityAttributeMapper", mockMapper);

        // not found
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter(LdapConstants.COMMON_NAME, UNKNOWN));

        dtos = new ArrayList<>();
        when(mockLdapTemplate.search("", filter.encode(), mockMapper)).thenReturn(dtos);
        assertNull(service.searchByName(UNKNOWN));
        verify(mockLdapTemplate, times(1)).search("", filter.encode(), mockMapper);

        // found 1
        filter = new AndFilter();
        filter.and(new EqualsFilter(LdapConstants.COMMON_NAME, USERNAME));
        dtos = new ArrayList<>();
        GroupInfraIdentityDTO dto = new GroupInfraIdentityDTO();
        dto.setAccount(USERNAME);
        dto.setFirstname(FIRSTNAME);
        dto.setLastname(LASTNAME);
        dto.addRole(Role.MEMBER);
        dtos.add(dto);

        when(mockLdapTemplate.search("", filter.encode(), mockMapper)).thenReturn(dtos);
        User user = service.searchByName(USERNAME);
        assertNotNull(user);
        assertEquals(USERNAME, user.getUserName());
        verify(mockLdapTemplate, times(1)).search("", filter.encode(), mockMapper);
    }

    @Test
    void testSearchByExample() {

        LdapService service = new LdapService();
        List<GroupInfraIdentityDTO> dtos;
        ReflectionTestUtils.setField(service, "ldap", true);
        ReflectionTestUtils.setField(service, "ldapTemplate", mockLdapTemplate);
        ReflectionTestUtils.setField(service, "identityAttributeMapper", mockMapper);

        User srcUser = new User();
        srcUser.setFirstName(FIRSTNAME);
        srcUser.setLastName(LASTNAME);

        AndFilter filter = new AndFilter();
        filter.and(new LikeFilter(LdapConstants.SURNAME, LASTNAME));
        filter.and(new LikeFilter(LdapConstants.GIVEN_NAME, FIRSTNAME));

        dtos = new ArrayList<>();
        GroupInfraIdentityDTO dto = new GroupInfraIdentityDTO();
        dto.setAccount(USERNAME);
        dto.setFirstname(FIRSTNAME);
        dto.setLastname(LASTNAME);
        dto.addRole(Role.MEMBER);
        dtos.add(dto);

        when(mockLdapTemplate.search("", filter.encode(), mockMapper)).thenReturn(dtos);
        List<User> users = service.searchByExample(srcUser);
        assertNotNull(users);
        assertEquals(1, users.size());
        User user = users.getFirst();
        assertEquals(USERNAME, user.getUserName());
        verify(mockLdapTemplate, times(1)).search("", filter.encode(), mockMapper);

    }
}
