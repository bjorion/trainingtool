package org.jorion.trainingtool.ldap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GroupInfraIdentityDTOTest {

    @Test
    void testToString() {

        var gi = new GroupInfraIdentityDTO();
        assertNotNull(gi.toString());
    }

    @Test
    void testPhone() {

        var gi = new GroupInfraIdentityDTO();
        gi.setPhone("123");
        gi.setMobile("456");
        assertEquals("456", gi.getMobileOrPhone());

        gi = new GroupInfraIdentityDTO();
        gi.setPhone("123");
        gi.setMobile(null);
        assertEquals("123", gi.getMobileOrPhone());
    }

    @Test
    void testMisc() {

        var gi = new GroupInfraIdentityDTO();
        gi.setEmail("noreply@example.org");
        gi.setManagerName("mgt");
        gi.setFunction("none");
        gi.setPnr("LS0001");
        gi.setDivision("taxud");

        assertEquals("noreply@example.org", gi.getEmail());
        assertEquals("mgt", gi.getManagerName());
        assertEquals("none", gi.getFunction());
        assertEquals("LS0001", gi.getPnr());
        assertEquals("taxud", gi.getDivision());
    }

}
