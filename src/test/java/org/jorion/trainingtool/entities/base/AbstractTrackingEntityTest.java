package org.jorion.trainingtool.entities.base;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit test for {@link AbstractTrackingEntity}.
 */
public class AbstractTrackingEntityTest
{
    // --- Constants ---
    private static final String USERNAME = "username";
    
    private static final LocalDateTime TIME = LocalDateTime.now();

    // --- Methods ---
    @Test
    public void testCreatedBy()
    {
        AbstractTrackingEntity entity = buildEntity();
        entity.setCreatedBy(USERNAME);
        assertEquals(USERNAME, entity.getCreatedBy());
    }

    @Test
    public void testModifiedBy()
    {
        AbstractTrackingEntity entity = buildEntity();
        entity.setModifiedBy(USERNAME);
        assertEquals(USERNAME, entity.getModifiedBy());
    }

    @Test
    public void testGetCreatedOn()
    {
        AbstractTrackingEntity entity = buildEntity();
        ReflectionTestUtils.setField(entity, "createdOn", TIME);
        assertEquals(TIME, entity.getCreatedOn());
    }

    @Test
    public void testGetModifiedOn()
    {
        AbstractTrackingEntity entity = buildEntity();
        ReflectionTestUtils.setField(entity, "modifiedOn", TIME);
        assertEquals(TIME, entity.getModifiedOn());
    }

    @Test
    public void testGetVersion()
    {
        AbstractTrackingEntity entity = buildEntity();
        ReflectionTestUtils.setField(entity, "version", 1L);
        assertEquals((Long) 1L, entity.getVersion());
    }

    private static AbstractTrackingEntity buildEntity()
    {
        return new AbstractTrackingEntity() {};
    }
}
