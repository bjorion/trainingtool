package org.jorion.trainingtool.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Declares the fields version, createdOn, createdBy, modifiedOn and modifiedBy.
 */
@MappedSuperclass
public abstract class AbstractTrackingEntity extends AbstractTimeTrackingEntity {

    @Column(nullable = false, updatable = false)
    private String createdBy;

    private String modifiedBy;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

}
