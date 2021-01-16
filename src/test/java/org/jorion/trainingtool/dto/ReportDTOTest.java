package org.jorion.trainingtool.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

import org.jorion.trainingtool.types.RegistrationStatus;

/**
 * Unit test for {@link ReportDTO}.
 */
public class ReportDTOTest
{
    // --- Methods ---
    @Test
    public void testToString()
    {
        ReportDTO report = new ReportDTO();
        assertNotNull(report.toString());
    }
    
    @Test
    public void testIsEmpty()
    {
        ReportDTO report = new ReportDTO();
        report.setUsername("username");
        report.setManager("manager");
        assertTrue(report.isEmpty());
        
        report = new ReportDTO();
        report.setLastname("lastname");
        assertFalse(report.isEmpty());
        
        report = new ReportDTO();
        report.setStartDate(LocalDate.now());
        assertFalse(report.isEmpty());
        
        report = new ReportDTO();
        report.setStatus(RegistrationStatus.DRAFT);
        assertFalse(report.isEmpty());
    }

    @Test
    public void testGettersAndSetters()
    {
        LocalDate now = LocalDate.now();

        ReportDTO report = new ReportDTO();
        report.setLastname("lastname");
        report.setStartDate(now);
        report.setStatus(RegistrationStatus.DRAFT);
        report.setUsername("username");
        report.setManager("manager");

        assertEquals("lastname", report.getLastname());
        assertEquals(now, report.getStartDate());
        assertEquals(RegistrationStatus.DRAFT, report.getStatus());
        assertEquals("username", report.getUsername());
        assertEquals("manager", report.getManager());
    }

}
