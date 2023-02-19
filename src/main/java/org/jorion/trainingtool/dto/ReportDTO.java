package org.jorion.trainingtool.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Container for the registration request search criteria (page report).
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ReportDTO implements Serializable {

    private String lastname;

    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;

    private String userName;

    private String managerName;

    /**
     * @return true if all the selection criteria are empty
     */
    public boolean isEmpty() {
        return StringUtils.isBlank(lastname) && (startDate == null) && (status == null);
    }

}
