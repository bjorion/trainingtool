package org.jorion.trainingtool.dao;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.services.UserService;
import org.jorion.trainingtool.util.MiscUtils;

/**
 * Hibernate custom interceptor used for record tracking.
 */
public class TrackingInterceptor extends EmptyInterceptor
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(TrackingInterceptor.class);

    private static final String CREATED_BY = "createdBy";

    private static final String MODIFIED_BY = "modifiedBy";

    // --- Methods ---
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
    {
        boolean change = false;
        if (entity instanceof Registration) {
            int index = MiscUtils.findIndex(propertyNames, CREATED_BY);
            if (index >= 0) {
                String principal = UserService.getPrincipalName();
                Registration r = (Registration) entity;
                r.setCreatedBy(principal);
                state[index] = principal;
                change = true;
                LOG.debug("DAO: Registration created by [{}]", principal);
            }
        }
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types)
    {
        boolean change = false;
        if (entity instanceof Registration) {
            int index = MiscUtils.findIndex(propertyNames, MODIFIED_BY);
            if (index >= 0) {
                String principal = UserService.getPrincipalName();
                Registration r = (Registration) entity;
                r.setModifiedBy(principal);
                String modifiedBy = (String) previousState[index];
                if (!StringUtils.equals(principal, modifiedBy)) {
                    currentState[index] = principal;
                    change = true;
                    LOG.debug("DAO: Registration modified by [{}]", principal);
                }
            }
        }
        return change;
    }
}
