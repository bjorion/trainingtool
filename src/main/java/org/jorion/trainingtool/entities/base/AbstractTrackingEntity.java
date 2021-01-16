package org.jorion.trainingtool.entities.base;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

/**
 * Declares the fields version, createdOn, createdBy, modifiedOn and modifiedBy.
 */
@MappedSuperclass
public abstract class AbstractTrackingEntity extends AbstractTimeTrackingEntity
{
    // --- Variables ---
    @NotNull
    private String createdBy;

    private String modifiedBy;

    // --- Methods ---
    public String getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }

    public String getModifiedBy()
    {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy)
    {
        this.modifiedBy = modifiedBy;
    }

}
