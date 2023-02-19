package org.jorion.trainingtool.dto;

import lombok.*;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.type.RegistrationStatus;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Encapsulate information about the registration status update. Immutable.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateEventDTO implements Serializable {

    // Registration
    @NonNull
    private Long regId;
    @NonNull
    private String regTitle;
    private LocalDate regStartDate;
    private String regJustification;
    @NonNull
    private RegistrationStatus regStatus;
    @NonNull
    private RegistrationEvent regEvent;

    // Member (the user requesting the training)
    private Long memberId;
    private String memberFirstname;
    private String memberLastname;
    private String memberFullname;
    private String memberEmail;

    // Actor (the user making the action)
    private String actorName;
    private String actorEmail;

    // Manager (the member's manager)
    private String manager;

}
