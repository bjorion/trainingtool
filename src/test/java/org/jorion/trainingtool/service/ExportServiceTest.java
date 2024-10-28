package org.jorion.trainingtool.service;

import org.assertj.core.util.Arrays;
import org.jorion.trainingtool.common.EntityUtils;
import org.jorion.trainingtool.dto.ReportDTO;
import org.jorion.trainingtool.entity.Registration;
import org.jorion.trainingtool.entity.User;
import org.jorion.trainingtool.service.ExportService.RegistrationCsvHeaders;
import org.jorion.trainingtool.type.Provider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExportServiceTest {

    @Mock
    private RegistrationService mockRegistrationService;

    @Test
    void testExportRegistrationsToCSV_Empty()
            throws IOException {

        ExportService service = new ExportService();
        ReflectionTestUtils.setField(service, "registrationService", mockRegistrationService);

        List<Registration> list = new ArrayList<>();
        Page<Registration> page = new PageImpl<>(list);
        ReportDTO dto = new ReportDTO();
        when(mockRegistrationService.findAllByExample(dto)).thenReturn(page);
        String csv = service.exportRegistrationsToCSV(dto, ExportService.CSV_DELIM);
        assertNotNull(csv);

        // check that each enum value is contained in the header

        Arrays.asList(RegistrationCsvHeaders.values())
                .stream()
                .map(e -> (ExportService.RegistrationCsvHeaders) e)
                .map(Enum::name).forEach(e -> assertTrue(csv.contains(e), "Missing header: " + e)
                );
    }

    @Test
    void testExportRegistrationsToCSV_NotEmpty()
            throws IOException {

        ExportService service = new ExportService();
        ReflectionTestUtils.setField(service, "registrationService", mockRegistrationService);

        User member = EntityUtils.createUser("jdoe");
        member.setFirstName("John");
        member.setLastName("Doe");
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
        assertTrue(csv.contains(member.getFullName()));
    }
}
