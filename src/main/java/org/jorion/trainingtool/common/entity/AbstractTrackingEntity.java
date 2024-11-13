package org.jorion.trainingtool.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Declares the fields version, createdOn, createdBy, modifiedOn and modifiedBy.
 */
@Setter
@Getter
@MappedSuperclass
public abstract class AbstractTrackingEntity extends AbstractTimeTrackingEntity {

    @Column(nullable = false, updatable = false)
    private String createdBy;

    private String modifiedBy;

}
