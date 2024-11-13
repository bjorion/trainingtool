package org.jorion.trainingtool.registration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.jorion.trainingtool.type.*;

import java.time.LocalDate;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistrationDTO {

    private Long id;

    private RegistrationStatus status = RegistrationStatus.DRAFT;

    // Description
    private String title;

    private String description;

    private Provider provider;

    private String providerOther;

    private String url;

    private String price;

    // place & date
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Period period;

    private String totalHour;

    private Location location;

    private YesNo selfStudy;

    // administrative
    private YesNo mandatory;

    private YesNo billable;

    /**
     * Trainings that are within working hours and that contribute to the development of the member.
     */
    private YesNo cbaCompliant = YesNo.NO;

    // Misc.
    private String comment;

    private String justification;
}
