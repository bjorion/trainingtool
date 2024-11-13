package org.jorion.trainingtool.registration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jorion.trainingtool.common.entity.AbstractTrackingEntity;
import org.jorion.trainingtool.type.*;
import org.jorion.trainingtool.user.User;
import org.jorion.trainingtool.util.SsinUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Entity corresponding to the REGISTRATION table.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "registration")
public class Registration extends AbstractTrackingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.DRAFT;

    /**
     * One User can have Many Registrations. There cannot be a registration without a User.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User member;

    // Description
    @NotNull
    @Enumerated(EnumType.STRING)
    private Provider provider;

    /**
     * Free field with the provider name in case provider = OTHER.
     */
    private String providerOther;

    /**
     * Social Security Identification Number.
     */
    private String ssin;

    @NotNull
    private String title;

    private String description;

    @NotNull
    private String url;

    private String price;

    // place & date
    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Period period;

    private String totalHour;

    @Enumerated(EnumType.STRING)
    private Location location;

    @Enumerated(EnumType.STRING)
    private YesNo selfStudy;

    // administrative
    @Enumerated(EnumType.STRING)
    private YesNo mandatory;

    @Enumerated(EnumType.STRING)
    private YesNo billable;

    /**
     * Trainings that are within working hours and that contribute to the development of the member.
     */
    @Enumerated(EnumType.STRING)
    private YesNo cbaCompliant = YesNo.NO;

    // Misc.
    private String comment;

    @Column(name = "motivation")
    private String justification;

    private Long trainingId;

    public Registration(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {

        return "Registration [id=" + id + ", title=" + title + ", creationDate=" + getCreatedOn() + "]";
    }

    /**
     * @param userName the member username
     * @return True if the member whose registration is assigned to, has the given username
     */
    public boolean belongsTo(String userName) {
        return getMember().getUserName().equals(userName);
    }

    /**
     * Copy updatable fields from the given registration to the current one. Empty values fill not be copied for
     * mandatory fields.
     * <p>
     * Some fields are not impacted: id, status, creationDate, updateDate because they are handled by the application,
     * not the user.
     *
     * @param srcReg the registration to copy the information from
     */
    public void convertFrom(Registration srcReg) {

        this.setBillable(srcReg.getBillable());
        this.setCbaCompliant(srcReg.getCbaCompliant());
        this.setComment(StringUtils.trimToNull(srcReg.getComment()));
        this.setDescription(srcReg.getDescription());
        this.setEndDate(srcReg.getEndDate());
        this.setLocation(srcReg.getLocation());
        this.setMandatory(srcReg.getMandatory());
        this.setPeriod(srcReg.getPeriod());
        this.setPrice(srcReg.getPrice());
        this.setTotalHour(srcReg.getTotalHour());
        this.setSelfStudy(srcReg.getSelfStudy());
        this.setUrl(srcReg.getUrl());
        this.setProviderOther(srcReg.getProviderOther());

        // following fields are mandatory, so they cannot be set to blank
        if (srcReg.getProvider() != null) {
            this.setProvider(srcReg.getProvider());
        }
        if (!StringUtils.isBlank(srcReg.getSsin())) {
            this.setSsin(srcReg.getSsin());
        }
        if (!StringUtils.isBlank(srcReg.getTitle())) {
            this.setTitle(srcReg.getTitle());
        }
        if (srcReg.getStartDate() != null) {
            this.setStartDate(srcReg.getStartDate());
        }
        if (!StringUtils.isBlank(srcReg.getJustification())) {
            this.setJustification(srcReg.getJustification().trim());
        }
    }

    public String getSsinFormatted() {
        return SsinUtils.formatSsin(ssin);
    }

    public void addJustification(String justification) {

        if (!StringUtils.isBlank(justification)) {
            this.setJustification(justification.trim() + "\n");
        }
    }
}
