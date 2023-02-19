package org.jorion.trainingtool.config.ldap;

import org.jorion.trainingtool.common.AssertUtils;
import org.jorion.trainingtool.common.LdapConstants;
import org.jorion.trainingtool.dto.GroupInfraIdentityDTO;
import org.jorion.trainingtool.type.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IdentityAttributeMapperTest {

    private static final String[] ROLES_ADMIN = new String[]{"admin"};
    private static final String[] ROLES_TRAINING = new String[]{"training"};
    private static final String[] ROLES_HR = new String[]{"hr"};
    private static final String[] ROLES_MANAGER = new String[]{"manager"};

    @Mock
    Attributes attrs;

    @Mock
    Attribute attr, attrId, attrManager;

    @Mock
    @SuppressWarnings("rawtypes")
    NamingEnumeration mockEnumeration;

    private static IdentityAttributeMapper initMapper() {

        IdentityAttributeMapper mapper = new IdentityAttributeMapper();
        ReflectionTestUtils.setField(mapper, "roleAdmin", ROLES_ADMIN);
        ReflectionTestUtils.setField(mapper, "roleTraining", ROLES_TRAINING);
        ReflectionTestUtils.setField(mapper, "roleHr", ROLES_HR);
        ReflectionTestUtils.setField(mapper, "roleManager", ROLES_MANAGER);
        return mapper;
    }

    @Test
    void testMapFromAttributes_simple() {

        final String id = "128191";
        final String manager = "bigBoss";
        final String cnManager = "CN=" + manager + ",OU=users";

        IdentityAttributeMapper mapper = initMapper();
        lenient().when(attrId.toString()).thenReturn(IdentityAttributeMapper.EMPLOYEE_ID + "=" + id);
        lenient().when(attrs.get(IdentityAttributeMapper.EMPLOYEE_ID)).thenReturn(attrId);

        lenient().when(attrManager.toString()).thenReturn(LdapConstants.MANAGER + "=" + cnManager);
        lenient().when(attrs.get(LdapConstants.MANAGER)).thenReturn(attrManager);

        GroupInfraIdentityDTO dto = mapper.mapFromAttributes(attrs);
        assertEquals(id, dto.getPnr());
        assertEquals(manager, dto.getManagerName());
    }

    @Test
    void testRemoveParenthesis() {

        assertEquals("", IdentityAttributeMapper.removeParenthesis(""));
        assertEquals("abc", IdentityAttributeMapper.removeParenthesis("abc"));
        assertEquals("abc", IdentityAttributeMapper.removeParenthesis("abc (def)"));
    }

    @Test
    void testReadAttributes() {

        Attributes attribs = new BasicAttributes();
        attribs.put("cn", "seldonh");
        assertEquals("seldonh", IdentityAttributeMapper.readAttribute(attribs, "cn"));
        assertEquals("", IdentityAttributeMapper.readAttribute(attribs, "none"));
    }

    @Test
    void testMapRoles_properties() {

        IdentityAttributeMapper mapper = initMapper();
        assertTrue(AssertUtils.containsExactly(mapper.mapRoles("admin", attrs), Role.MEMBER, Role.ADMIN));
        assertTrue(AssertUtils.containsExactly(mapper.mapRoles("training", attrs), Role.MEMBER, Role.TRAINING));
        assertTrue(AssertUtils.containsExactly(mapper.mapRoles("hr", attrs), Role.MEMBER, Role.HR));
        assertTrue(AssertUtils.containsExactly(mapper.mapRoles("manager", attrs), Role.MEMBER, Role.MANAGER));
        assertTrue(AssertUtils.containsExactly(mapper.mapRoles("member", attrs), Role.MEMBER));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testMapRoles_ldapAttributes()
            throws NamingException {

        final String cn = "CN=john.doe,OU=Users,OU=BE,OU=Landlord NL,OU=Corporate,DC=groupinfra,DC=com";
        IdentityAttributeMapper mapper = initMapper();

        when(mockEnumeration.hasMore()).thenReturn(true);
        when(mockEnumeration.next()).thenReturn(cn);
        when(attr.size()).thenReturn(1);
        when(attr.getAll()).thenReturn(mockEnumeration);
        when(attrs.get(LdapConstants.DIRECT_REPORTS)).thenReturn(attr);
        Set<Role> roles = mapper.mapRoles("myManager", attrs);

        assertTrue(AssertUtils.containsExactly(roles, Role.MEMBER, Role.MANAGER));
    }

}
