package org.jorion.trainingtool.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import org.jorion.trainingtool.dto.ReportDTO;
import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.services.ExportService.RegistrationCsvHeaders;
import org.jorion.trainingtool.types.Provider;

/**
 * Unit test for {@link ExportService}.
 */
public class ExportServiceTest
{
    // --- Variables ---
    @Mock
    RegistrationService mockRegistrationService;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    // --- Methods ---
    @Test
    public void testExportRegistrationsToCSV_Empty()
            throws IOException
    {
        ExportService service = new ExportService();
        ReflectionTestUtils.setField(service, "registrationService", mockRegistrationService);

        List<Registration> list = new ArrayList<>();
        Page<Registration> page = new PageImpl<>(list);
        ReportDTO dto = new ReportDTO();
        when(mockRegistrationService.findAllByExample(dto)).thenReturn(page);
        String csv = service.exportRegistrationsToCSV(dto, ExportService.CSV_DELIM);
        assertNotNull(csv);

        // check that each enum value is contained in the header
        Arrays.asList(RegistrationCsvHeaders.values()).stream().map(e -> (ExportService.RegistrationCsvHeaders) e).map(e -> e.name()).forEach(e -> {
            assertTrue("Missing header: " + e, csv.contains(e));
        });
    }
    
    @Test
    public void testExportRegistrationsToCSV_NotEmpty()
            throws IOException
    {
        ExportService service = new ExportService();
        ReflectionTestUtils.setField(service, "registrationService", mockRegistrationService);
        
        User member = new User("jdoe");
        member.setFirstname("John");
        member.setLastname("Doe");
        Registration r = new Registration(1L);
        r.setProvider(Provider.OTHER);
        r.setProviderOther("MOOC");
        r.setMember(member);
        
        List<Registration> registrations = new ArrayList<>();
        registrations.add(r);
        
        Page<Registration> page = new PageImpl<>(registrations);
        ReportDTO dto = new ReportDTO();
        when(mockRegistrationService.findAllByExample(dto)).thenReturn(page);
        String csv = service.exportRegistrationsToCSV(dto, ExportService.CSV_DELIM);
        assertNotNull(csv);
        assertTrue(csv.contains("MOOC"));
        assertTrue(csv.contains(member.getFullname()));
    }
}
