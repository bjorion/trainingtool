package org.jorion.trainingtool.dao;

import org.hibernate.type.Type;
import org.jorion.trainingtool.common.AuthenticationUtils;
import org.jorion.trainingtool.common.EntityUtils;
import org.jorion.trainingtool.entity.Registration;
import org.jorion.trainingtool.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TrackingInterceptorTest {

    private static final String USERNAME = "jdoe";

    @Test
    void testOnSave() {

        boolean change;
        AuthenticationUtils.setAuthentication(USERNAME);
        LocalDateTime now = LocalDateTime.now();
        String[] names = {"createdOn", "createdBy", "modifiedOn", "modifiedBy"};

        // change
        Registration r = new Registration(1L);
        Object[] regStates = {now, null, now, null};
        TrackingInterceptor ti = new TrackingInterceptor();
        change = ti.onSave(r, 1L, regStates, names, null);
        assertTrue(change);
        assertEquals(USERNAME, regStates[1]);

        // no change
        User user = EntityUtils.createUser(USERNAME);
        user.setId(1L);
        Object[] userStates = {now, null, now, null};
        ti = new TrackingInterceptor();
        change = ti.onSave(user, 1L, userStates, names, null);
        assertFalse(change);
        assertNull(userStates[1]);
    }

    @Test
    void testOnFlushDirty() {

        boolean change;
        AuthenticationUtils.setAuthentication(USERNAME);
        LocalDateTime now = LocalDateTime.now();
        String[] names = {"createdOn", "createdBy", "modifiedOn", "modifiedBy"};
        Type[] types = {null, null, null, null};

        // change
        Registration r = new Registration(1L);
        Object[] prevStates = {now, null, now, null};
        Object[] currStates = {now, null, now, null};
        TrackingInterceptor ti = new TrackingInterceptor();
        change = ti.onFlushDirty(r, 1L, currStates, prevStates, names, types);
        assertTrue(change);
        assertEquals(USERNAME, currStates[3]);

        // no change
        r = new Registration(1L);
        Object[] prevStates2 = {now, null, now, USERNAME};
        Object[] currStates2 = {now, null, now, USERNAME};
        ti = new TrackingInterceptor();
        change = ti.onFlushDirty(r, 1L, currStates2, prevStates2, names, types);
        assertFalse(change);
    }

}
