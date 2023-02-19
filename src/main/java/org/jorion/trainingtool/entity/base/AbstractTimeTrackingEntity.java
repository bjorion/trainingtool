package org.jorion.trainingtool.entity.base;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * Declares the fields version, createdOn and modifiedOn.
 */
@MappedSuperclass
public abstract class AbstractTimeTrackingEntity extends AbstractOptimisticLockingEntity {

    /**
     * Creation date. Not updatable.
     */
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdOn;

    /**
     * Modification date.
     */
    @UpdateTimestamp
    private LocalDateTime modifiedOn;

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public LocalDateTime getModifiedOn() {
        return modifiedOn;
    }

}
