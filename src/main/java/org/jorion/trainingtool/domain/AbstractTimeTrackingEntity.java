package org.jorion.trainingtool.domain;

import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

/**
 * Declares the fields version, createdOn and modifiedOn.
 */
@Getter
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

}
