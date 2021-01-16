package org.jorion.trainingtool.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.hibernate.type.Type;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.entities.User;
import org.jorion.trainingtool.util.TestingUtils;

/**
 * Unit test for {@link TrackingInterceptorTest}.
 */
public class TrackingInterceptorTest
{
    // --- Constants ---
    private static final String USERNAME = "jdoe";

    // --- Methods ---
    @Before
    public void setUp()
    {
        TestingUtils.cleanAuthentication();
    }

    @After
    public void cleanUp()
    {
        TestingUtils.cleanAuthentication();
    }

    @Test
    public void testOnSave()
    {
        boolean change;
        TestingUtils.setAuthentication(USERNAME);
        LocalDateTime now = LocalDateTime.now();
        String[] names = { "createdOn", "createdBy", "modifiedOn", "modifiedBy" };

        // change
        Registration r = new Registration(1L);
        Object[] regStates = { now, null, now, null };
        TrackingInterceptor ti = new TrackingInterceptor();
        change = ti.onSave(r, 1L, regStates, names, null);
        assertTrue(change);
        assertEquals(USERNAME, regStates[1]);

        // no change
        User user = new User(USERNAME);
        user.setId(1L);
        Object[] userStates = { now, null, now, null };
        ti = new TrackingInterceptor();
        change = ti.onSave(user, 1L, userStates, names, null);
        assertFalse(change);
        assertNull(userStates[1]);
    }

    @Test
    public void testOnFlushDirty()
    {
        boolean change;
        TestingUtils.setAuthentication(USERNAME);
        LocalDateTime now = LocalDateTime.now();
        String[] names = { "createdOn", "createdBy", "modifiedOn", "modifiedBy" };
        Type[] types = { null, null, null, null };

        // change
        Registration r = new Registration(1L);
        Object[] prevStates = { now, null, now, null };
        Object[] currStates = { now, null, now, null };
        TrackingInterceptor ti = new TrackingInterceptor();
        change = ti.onFlushDirty(r, 1L, currStates, prevStates, names, types);
        assertTrue(change);
        assertEquals(USERNAME, currStates[3]);

        // no change
        r = new Registration(1L);
        Object[] prevStates2 = { now, null, now, USERNAME };
        Object[] currStates2 = { now, null, now, USERNAME };
        ti = new TrackingInterceptor();
        change = ti.onFlushDirty(r, 1L, currStates2, prevStates2, names, types);
        assertFalse(change);
    }

}
