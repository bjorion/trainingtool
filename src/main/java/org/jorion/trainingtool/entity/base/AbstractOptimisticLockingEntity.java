package org.jorion.trainingtool.entity.base;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.io.Serializable;

/**
 * The field {@code version} is used to implement optimistic locking.
 */
@MappedSuperclass
public abstract class AbstractOptimisticLockingEntity implements Serializable {

    /**
     * Used to ensure optimistic locking.
     */
    @Version
    private Long version;

    public Long getVersion() {
        return version;
    }

}
