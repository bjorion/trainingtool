package org.jorion.trainingtool.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import org.jorion.trainingtool.entities.base.AbstractTrackingEntity;
import org.jorion.trainingtool.types.Location;
import org.jorion.trainingtool.types.Period;
import org.jorion.trainingtool.types.Provider;
import org.jorion.trainingtool.types.RegistrationStatus;
import org.jorion.trainingtool.types.YesNo;
import org.jorion.trainingtool.util.SsinUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Entity corresponding to the REGISTRATION table.
 */
@Entity
@Table(name = "registration", schema = "trainingtool")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "createdOn", "createdBy", "modifiedOn", "modifiedBy", "status", "provider", "providerOther", "title", "description" })
public class Registration extends AbstractTrackingEntity
{
    // --- Variables ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.DRAFT;

    /** One User can have Many Registrations. There cannot be a registration without a User. */
    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User member;

    // Description
    @NotNull
    @Enumerated(EnumType.STRING)
    private Provider provider;

    /** Free field with the provider name in case provider = OTHER. */
    private String providerOther;

    /** Social Security Identification Number. */
    private String ssin;

    @NotNull
    private String title;

    private String description;

    @NotNull
    private String url;

    private String price;

    // place & date
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
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

    /** Trainings that are within working hours and that contribute to the development of the member. */
    @Enumerated(EnumType.STRING)
    private YesNo cbaCompliant = YesNo.NO;

    // Misc.
    private String comment;

    private String motivation;

    // --- Methods ---
    /**
     * Default constructor.
     */
    public Registration()
    {}

    public Registration(Long id)
    {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Registration [id=" + id + ", title=" + title + ", creationDate=" + getCreatedOn() + "]";
    }

    /**
     * @param username the member username
     * @return True if the member whose registration is assigned to, has the given username
     */
    public boolean belongsTo(String username)
    {
        return getMember().getUsername().equals(username);
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
    public void convertFrom(Registration srcReg)
    {
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
        if (!StringUtils.isBlank(srcReg.getMotivation())) {
            this.setMotivation(srcReg.getMotivation().trim());
        }
    }

    // --- Getters and Setters ---
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }
    
    public User getMember()
    {
        return member;
    }

    public void setMember(User member)
    {
        this.member = member;
    }

    public String getSsin()
    {
        return ssin;
    }

    @JsonIgnore
    public String getSsinFormatted()
    {
        return SsinUtils.formatSsin(ssin);
    }

    public void setSsin(String ssin)
    {
        this.ssin = ssin;
    }

    public String getTotalHour()
    {
        return totalHour;
    }

    public void setTotalHour(String totalHour)
    {
        this.totalHour = totalHour;
    }

    public String getPrice()
    {
        return price;
    }

    public void setPrice(String price)
    {
        this.price = price;
    }

    public YesNo getMandatory()
    {
        return mandatory;
    }

    public void setMandatory(YesNo mandatory)
    {
        this.mandatory = mandatory;
    }

    public YesNo getBillable()
    {
        return billable;
    }

    public void setBillable(YesNo billable)
    {
        this.billable = billable;
    }

    public YesNo getCbaCompliant()
    {
        return cbaCompliant;
    }

    public void setCbaCompliant(YesNo cbaCompliant)
    {
        this.cbaCompliant = cbaCompliant;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Provider getProvider()
    {
        return provider;
    }

    public void setProvider(Provider provider)
    {
        this.provider = provider;
    }

    public String getProviderOther()
    {
        return providerOther;
    }

    public void setProviderOther(String providerOther)
    {
        this.providerOther = providerOther;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getMotivation()
    {
        return motivation;
    }

    public void setMotivation(String motivation)
    {
        this.motivation = motivation;
    }

    public LocalDate getStartDate()
    {
        return startDate;
    }

    public void setStartDate(LocalDate startDate)
    {
        this.startDate = startDate;
    }

    public LocalDate getEndDate()
    {
        return endDate;
    }

    public Location getLocation()
    {
        return location;
    }

    public void setLocation(Location location)
    {
        this.location = location;
    }

    public void setEndDate(LocalDate endDate)
    {
        this.endDate = endDate;
    }

    public RegistrationStatus getStatus()
    {
        return status;
    }

    public void setStatus(RegistrationStatus status)
    {
        this.status = status;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Period getPeriod()
    {
        return period;
    }

    public void setPeriod(Period period)
    {
        this.period = period;
    }

    public YesNo getSelfStudy()
    {
        return selfStudy;
    }

    public void setSelfStudy(YesNo selfStudy)
    {
        this.selfStudy = selfStudy;
    }
}
