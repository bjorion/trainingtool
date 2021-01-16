package org.jorion.trainingtool.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Unit test for {@link GroupinfraIdentityDTO}.
 */
public class GroupinfraIdentityDTOTest
{
    // --- Methods ---
    @Test
    public void testToString()
    {
        GroupinfraIdentityDTO gi = new GroupinfraIdentityDTO();
        assertNotNull(gi.toString());
    }

    @Test
    public void testPhone()
    {
        GroupinfraIdentityDTO gi = new GroupinfraIdentityDTO();
        gi.setPhone("123");
        gi.setMobile("456");
        assertEquals("456", gi.getMobileOrPhone());
        
        gi = new GroupinfraIdentityDTO();
        gi.setPhone("123");
        gi.setMobile(null);
        assertEquals("123", gi.getMobileOrPhone());
    }
    
    @Test
    public void testMisc()
    {
        GroupinfraIdentityDTO gi = new GroupinfraIdentityDTO();
        gi.setEmail("noreply@jorion.org");
        gi.setManagername("mgt");
        gi.setFunction("none");
        gi.setPnr("LS0001");
        gi.setDivision("taxud");
        
        assertEquals("noreply@jorion.org", gi.getEmail());
        assertEquals("mgt", gi.getManagername());
        assertEquals("none", gi.getFunction());
        assertEquals("LS0001", gi.getPnr());
        assertEquals("taxud", gi.getDivision());
    }

}
