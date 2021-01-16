package org.jorion.trainingtool.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

import org.jorion.trainingtool.dto.UpdateEventDTO.Builder;
import org.jorion.trainingtool.types.RegistrationEvent;
import org.jorion.trainingtool.types.RegistrationStatus;

/**
 * Unit test for {@link UpdateEventDTO}.
 */
public class UpdateEventDTOTest
{
    // --- Methods ---
    @Test(expected = IllegalArgumentException.class)
    public void testBuildMissingValues()
    {
        UpdateEventDTO.Builder builder = new UpdateEventDTO.Builder();
        builder.withRegId(1L).build();
    }

    @Test
    public void testBuildRequiredValues()
    {
        // test with only required values
        Builder builder = new UpdateEventDTO.Builder();
        UpdateEventDTO dto = builder.withRegId(1L).withRegTitle("title").withRegStatus(RegistrationStatus.DRAFT).withRegEvent(RegistrationEvent.SAVE).build();
        assertTrue(dto.getRegId() == 1L);
        assertEquals("title", dto.getRegTitle());
        assertTrue(dto.getRegStatus() == RegistrationStatus.DRAFT);
        assertTrue(dto.getRegEvent() == RegistrationEvent.SAVE);
    }
    
    @Test
    public void testBuildAllValues()
    {
        LocalDate now = LocalDate.now();
        Builder builder = new UpdateEventDTO.Builder();
        // @formatter:off
        UpdateEventDTO dto = builder
                .withRegId(1L)
                .withRegTitle("title")
                .withRegStatus(RegistrationStatus.DRAFT)
                .withRegEvent(RegistrationEvent.SAVE)
                .withRegMotivation("motivation")
                .withRegStartDate(now)
                .withActorEmail("actor@jorion.org")
                .withActorName("actor")
                .withManager("manager")
                .withMemberId(2L)
                .withMemberFirstname("john")
                .withMemberLastname("doe")
                .withMemberEmail("john.doe@jorion.org")
                .build();
        // @formatter:on
        
        assertTrue(dto.getRegId() == 1L);
        assertEquals("title", dto.getRegTitle());
        assertTrue(dto.getRegStatus() == RegistrationStatus.DRAFT);
        assertTrue(dto.getRegEvent() == RegistrationEvent.SAVE);
        assertEquals("motivation", dto.getRegMotivation());
        assertEquals(now, dto.getRegStartDate());
        
        assertEquals("actor@jorion.org", dto.getActorEmail());
        assertEquals("actor", dto.getActorName());
        
        assertEquals("manager", dto.getManager());
        assertTrue(dto.getMemberId() == 2L);
        assertEquals("john", dto.getMemberFirstname());
        assertEquals("doe", dto.getMemberLastname());
        assertEquals("john.doe@jorion.org", dto.getMemberEmail());
    }

}
