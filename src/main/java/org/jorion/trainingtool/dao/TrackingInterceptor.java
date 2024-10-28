package org.jorion.trainingtool.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.jorion.trainingtool.entity.base.AbstractTrackingEntity;
import org.jorion.trainingtool.service.UserService;
import org.jorion.trainingtool.util.StrUtils;

/**
 * Hibernate custom interceptor used for record tracking.
 * <p>
 * Improvement: replace this by Spring annotations
 */
@Slf4j
public class TrackingInterceptor implements Interceptor {

    private static final String CREATED_BY = "createdBy";
    private static final String MODIFIED_BY = "modifiedBy";

    @Override
    public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {

        boolean change = false;
        if (entity instanceof AbstractTrackingEntity) {
            int index = StrUtils.findIndex(propertyNames, CREATED_BY);
            if (index >= 0) {
                String principal = UserService.getPrincipalName();
                AbstractTrackingEntity ate = (AbstractTrackingEntity) entity;
                ate.setCreatedBy(principal);
                state[index] = principal;
                change = true;
                log.debug("DAO: [{}] created by [{}]", ate.getClass().getSimpleName(), principal);
            }
        }
        return change;
    }

    @Override
    public boolean onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState,
                                String[] propertyNames, Type[] types) {

        boolean change = false;
        if (entity instanceof AbstractTrackingEntity) {
            int index = StrUtils.findIndex(propertyNames, MODIFIED_BY);
            if (index >= 0) {
                String principal = UserService.getPrincipalName();
                AbstractTrackingEntity ate = (AbstractTrackingEntity) entity;
                ate.setModifiedBy(principal);
                String modifiedBy = (String) previousState[index];
                if (!StringUtils.equals(principal, modifiedBy)) {
                    currentState[index] = principal;
                    change = true;
                    log.debug("DAO: [{}] modified by [{}]", ate.getClass().getSimpleName(), principal);
                }
            }
        }
        return change;
    }
}
