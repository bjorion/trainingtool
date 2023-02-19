package org.jorion.trainingtool.dto;

import org.jorion.trainingtool.type.RegistrationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ReportDTOTest {

    @Test
    void testToString() {

        ReportDTO report = new ReportDTO();
        assertNotNull(report.toString());
    }

    @Test
    void testIsEmpty() {

        ReportDTO report = new ReportDTO();
        report.setUserName("username");
        report.setManagerName("manager");
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
    void testGettersAndSetters() {

        LocalDate now = LocalDate.now();

        ReportDTO report = new ReportDTO();
        report.setLastname("lastname");
        report.setStartDate(now);
        report.setStatus(RegistrationStatus.DRAFT);
        report.setUserName("username");
        report.setManagerName("manager");

        assertEquals("lastname", report.getLastname());
        assertEquals(now, report.getStartDate());
        assertEquals(RegistrationStatus.DRAFT, report.getStatus());
        assertEquals("username", report.getUserName());
        assertEquals("manager", report.getManagerName());
    }

}
