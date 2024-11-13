package org.jorion.trainingtool.common.entity;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractTrackingEntityTest {

    private static final String USERNAME = "username";
    private static final LocalDateTime TIME = LocalDateTime.now();

    private static AbstractTrackingEntity buildEntity() {
        return new AbstractTrackingEntity() {
        };
    }

    @Test
    public void testCreatedBy() {

        AbstractTrackingEntity entity = buildEntity();
        entity.setCreatedBy(USERNAME);
        assertEquals(USERNAME, entity.getCreatedBy());
    }

    @Test
    public void testModifiedBy() {

        AbstractTrackingEntity entity = buildEntity();
        entity.setModifiedBy(USERNAME);
        assertEquals(USERNAME, entity.getModifiedBy());
    }

    @Test
    public void testGetCreatedOn() {

        AbstractTrackingEntity entity = buildEntity();
        ReflectionTestUtils.setField(entity, "createdOn", TIME);
        assertEquals(TIME, entity.getCreatedOn());
    }

    @Test
    public void testGetModifiedOn() {

        AbstractTrackingEntity entity = buildEntity();
        ReflectionTestUtils.setField(entity, "modifiedOn", TIME);
        assertEquals(TIME, entity.getModifiedOn());
    }

    @Test
    public void testGetVersion() {

        AbstractTrackingEntity entity = buildEntity();
        ReflectionTestUtils.setField(entity, "version", 1L);
        assertEquals((Long) 1L, entity.getVersion());
    }
}
