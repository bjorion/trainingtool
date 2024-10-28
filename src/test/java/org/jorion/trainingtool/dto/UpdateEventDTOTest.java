package org.jorion.trainingtool.dto;

import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


public class UpdateEventDTOTest {

    @Test
    void testBuildMissingValues() {

        assertThrows(NullPointerException.class, () -> UpdateEventDTO.builder().regId(1L).build());
    }

    @Test
    void testBuildRequiredValues() {

        // test with only required values
        UpdateEventDTO dto = UpdateEventDTO.builder()
                .regId(1L)
                .regTitle("title")
                .regStatus(RegistrationStatus.DRAFT)
                .regEvent(RegistrationEvent.SAVE)
                .build();
        assertEquals(1L, (long) dto.getRegId());
        assertEquals("title", dto.getRegTitle());
        assertSame(dto.getRegStatus(), RegistrationStatus.DRAFT);
        assertSame(dto.getRegEvent(), RegistrationEvent.SAVE);
    }

    @Test
    void testBuildAllValues() {

        LocalDate now = LocalDate.now();

        UpdateEventDTO dto = UpdateEventDTO.builder()
                .regId(1L)
                .regTitle("title")
                .regStatus(RegistrationStatus.DRAFT)
                .regEvent(RegistrationEvent.SAVE)
                .regJustification("justification")
                .regStartDate(now)
                .actorEmail("actor@example.org")
                .actorName("actor")
                .manager("manager")
                .memberId(2L)
                .memberFirstname("john")
                .memberLastname("doe")
                .memberEmail("john.doe@example.org")
                .build();


        assertEquals(1L, (long) dto.getRegId());
        assertEquals("title", dto.getRegTitle());
        assertSame(dto.getRegStatus(), RegistrationStatus.DRAFT);
        assertSame(dto.getRegEvent(), RegistrationEvent.SAVE);
        assertEquals("justification", dto.getRegJustification());
        assertEquals(now, dto.getRegStartDate());

        assertEquals("actor@example.org", dto.getActorEmail());
        assertEquals("actor", dto.getActorName());

        assertEquals("manager", dto.getManager());
        assertEquals(2L, (long) dto.getMemberId());
        assertEquals("john", dto.getMemberFirstname());
        assertEquals("doe", dto.getMemberLastname());
        assertEquals("john.doe@example.org", dto.getMemberEmail());
    }

}
