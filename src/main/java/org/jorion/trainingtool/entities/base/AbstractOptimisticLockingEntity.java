package org.jorion.trainingtool.entities.base;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The field {@code version} is used to implement optimistic locking.
 */
@MappedSuperclass
public abstract class AbstractOptimisticLockingEntity implements Serializable
{
    // --- Variables ---
    /**
     * Used to ensure optimistic locking.
     */
    @JsonIgnore
    @Version
    private Long version;

    // --- Getters and Setters ---
    public Long getVersion()
    {
        return version;
    }

}
