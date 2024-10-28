package org.jorion.trainingtool.entity;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jorion.trainingtool.entity.base.AbstractTrackingEntity;
import org.jorion.trainingtool.type.Location;
import org.jorion.trainingtool.type.Period;
import org.jorion.trainingtool.type.Provider;
import org.jorion.trainingtool.type.YesNo;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entity corresponding to the TRAINING table.
 * <p>
 * Registration can be created by copying data from a training.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "training", schema = "trainingtool")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Training extends AbstractTrackingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Activation
    private boolean enabled;

    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate enabledFrom;

    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate enabledUntil;

    // Description
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    /**
     * Free field with the provider name in case provider = OTHER.
     */
    private String providerOther;

    @Column(nullable = false)
    private String url;

    private String price;

    // place & date
    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate endDate;

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private YesNo cbaCompliant = YesNo.NO;

    // Misc.
    @Column(name = "motivation")
    private String justification;

    private static String toStr(LocalDate date) {
        return (date == null) ? "?" : date.toString();
    }

    @Override
    public String toString() {

        return "Training [id=" + id + ", title=" + title + ", available=" + isAvailable() + ", startDate=" + startDate + ", endDate=" + endDate + "]";
    }

    public String getContent() {
        return String.format("%s ( %s to %s )", title, toStr(startDate), toStr(endDate));
    }

    public boolean isAvailable() {

        LocalDate now = LocalDate.now();
        return enabled && (enabledFrom == null || !enabledFrom.isAfter(now)) && (enabledUntil == null || !enabledUntil.isBefore(now));
    }

}
