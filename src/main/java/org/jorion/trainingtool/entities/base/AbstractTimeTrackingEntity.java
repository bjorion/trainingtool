package org.jorion.trainingtool.entities.base;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Declares the fields version, createdOn and modifiedOn.
 */
@MappedSuperclass
public abstract class AbstractTimeTrackingEntity extends AbstractOptimisticLockingEntity
{
    // --- Variables ---
    /** Creation date. Not updatable. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdOn;

    /** Modification date. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @UpdateTimestamp
    private LocalDateTime modifiedOn;

    // --- Getters and Setters ----
    public LocalDateTime getCreatedOn()
    {
        return createdOn;
    }

    public LocalDateTime getModifiedOn()
    {
        return modifiedOn;
    }

}
