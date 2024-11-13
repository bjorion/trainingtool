package org.jorion.trainingtool.event;

import jakarta.annotation.Nonnull;
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
    @Nonnull
    private Long regId;
    @Nonnull
    private String regTitle;
    private LocalDate regStartDate;
    private String regJustification;
    @Nonnull
    private RegistrationStatus regStatus;
    @Nonnull
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
