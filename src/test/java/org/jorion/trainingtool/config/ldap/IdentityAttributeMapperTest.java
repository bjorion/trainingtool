package org.jorion.trainingtool.config.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import org.jorion.trainingtool.common.Constants;
import org.jorion.trainingtool.types.Role;

/**
 * Unit tests for {@link IdentityAttributeMapper}.
 */
public class IdentityAttributeMapperTest
{
    // --- Variables ---
    @Mock
    Attributes mockAttrs;

    @Mock
    Attribute mockAttr;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    // --- Methods ---
    @Test
    public void testRemoveParenthesis()
    {
        assertEquals("", IdentityAttributeMapper.removeParenthesis(""));
        assertEquals("abc", IdentityAttributeMapper.removeParenthesis("abc"));
        assertEquals("abc", IdentityAttributeMapper.removeParenthesis("abc (def)"));
    }

    @Test
    public void testReadAttributes()
    {
        Attributes attribs = new BasicAttributes();
        attribs.put("cn", "seldonh");
        assertEquals("seldonh", IdentityAttributeMapper.readAttribute(attribs, "cn"));
        assertEquals("", IdentityAttributeMapper.readAttribute(attribs, "none"));
    }

    @Test
    public void testMapRole()
    {
        IdentityAttributeMapper mapper = new IdentityAttributeMapper();
        ReflectionTestUtils.setField(mapper, "roleAdmin", new String[] { "admin" });
        ReflectionTestUtils.setField(mapper, "roleTraining", new String[] { "training" });
        ReflectionTestUtils.setField(mapper, "roleHr", new String[] { "hr" });
        ReflectionTestUtils.setField(mapper, "roleManager", new String[] { "manager" });

        assertTrue(mapper.mapRoles("admin", mockAttrs).contains(Role.ADMIN));
        assertTrue(mapper.mapRoles("training", mockAttrs).contains(Role.TRAINING));
        assertTrue(mapper.mapRoles("hr", mockAttrs).contains(Role.HR));
        assertTrue(mapper.mapRoles("manager", mockAttrs).contains(Role.MANAGER));

        when(mockAttrs.get(Constants.DIRECT_REPORTS)).thenReturn(null);
        assertTrue(mapper.mapRoles("someguy", mockAttrs).contains(Role.MEMBER));

        when(mockAttr.size()).thenReturn(1);
        when(mockAttrs.get(Constants.DIRECT_REPORTS)).thenReturn(mockAttr);
        assertTrue(mapper.mapRoles("someguy", mockAttrs).contains(Role.MANAGER));
    }
}
