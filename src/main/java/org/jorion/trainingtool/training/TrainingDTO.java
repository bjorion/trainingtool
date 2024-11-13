package org.jorion.trainingtool.training;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.jorion.trainingtool.type.Location;
import org.jorion.trainingtool.type.Period;
import org.jorion.trainingtool.type.Provider;
import org.jorion.trainingtool.type.YesNo;

import java.time.LocalDate;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainingDTO {

    private Long id;

    // Description
    private String title;

    private String description;

    private boolean enabled;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate enabledFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate enabledUntil;

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

}
