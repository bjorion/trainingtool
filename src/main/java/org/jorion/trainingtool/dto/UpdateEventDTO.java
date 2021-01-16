package org.jorion.trainingtool.dto;

import java.io.Serializable;
import java.time.LocalDate;

import org.springframework.util.Assert;

import org.jorion.trainingtool.types.RegistrationEvent;
import org.jorion.trainingtool.types.RegistrationStatus;

/**
 * Encapsulate information about the registration status update. Immutable.
 */
public class UpdateEventDTO implements Serializable
{
    // --- Variables ---
    // Registration
    private Long regId;
    private String regTitle;
    private LocalDate regStartDate;
    private String regMotivation;
    private RegistrationStatus regStatus;
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

    // --- Methods ---
    private UpdateEventDTO()
    {}

    // --- Getters and Setters ---
    public Long getRegId()
    {
        return regId;
    }

    public String getRegTitle()
    {
        return regTitle;
    }

    public LocalDate getRegStartDate()
    {
        return regStartDate;
    }

    public String getRegMotivation()
    {
        return regMotivation;
    }

    public RegistrationStatus getRegStatus()
    {
        return regStatus;
    }

    public RegistrationEvent getRegEvent()
    {
        return regEvent;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public String getMemberFirstname()
    {
        return memberFirstname;
    }

    public String getMemberLastname()
    {
        return memberLastname;
    }

    public String getMemberFullname()
    {
        return memberFullname;
    }

    public String getMemberEmail()
    {
        return memberEmail;
    }

    public String getActorName()
    {
        return actorName;
    }

    public String getActorEmail()
    {
        return actorEmail;
    }

    public String getManager()
    {
        return manager;
    }

    // --- Builder ---
    /**
     * Implement the Builder pattern for UpdateEventDTO.
     */
    public static class Builder
    {
        private Long regId;
        private String regTitle;
        private LocalDate regStartDate;
        private String motivation;
        private RegistrationStatus regStatus;
        private RegistrationEvent regEvent;

        private Long memberId;
        private String memberFirstname;
        private String memberLastname;
        private String memberFullname;
        private String memberEmail;

        private String actorName;
        private String actorEmail;

        private String manager;

        // --- Methods ---
        /**
         * Default constructor.
         */
        public Builder()
        {}

        public Builder withRegId(Long regId)
        {
            this.regId = regId;
            return this;
        }

        public Builder withRegTitle(String regTitle)
        {
            this.regTitle = regTitle;
            return this;
        }

        public Builder withRegStartDate(LocalDate regStartDate)
        {
            this.regStartDate = regStartDate;
            return this;
        }

        public Builder withRegMotivation(String motivation)
        {
            this.motivation = motivation;
            return this;
        }

        public Builder withRegStatus(RegistrationStatus regStatus)
        {
            this.regStatus = regStatus;
            return this;
        }

        public Builder withRegEvent(RegistrationEvent regEvent)
        {
            this.regEvent = regEvent;
            return this;
        }

        public Builder withMemberId(Long memberId)
        {
            this.memberId = memberId;
            return this;
        }

        public Builder withMemberFirstname(String memberFirstname)
        {
            this.memberFirstname = memberFirstname;
            return this;
        }

        public Builder withMemberLastname(String memberLastname)
        {
            this.memberLastname = memberLastname;
            return this;
        }

        public Builder withMemberFullname(String memberFullname)
        {
            this.memberFullname = memberFullname;
            return this;
        }

        public Builder withMemberEmail(String memberEmail)
        {
            this.memberEmail = memberEmail;
            return this;
        }

        public Builder withActorEmail(String actorEmail)
        {
            this.actorEmail = actorEmail;
            return this;
        }

        public Builder withActorName(String actorName)
        {
            this.actorName = actorName;
            return this;
        }

        public Builder withManager(String manager)
        {
            this.manager = manager;
            return this;
        }

        /**
         * @return a valid instance of {@link UpdateEventDTO}
         * @throws IllegalArgumentException if the data are not valid
         */
        public UpdateEventDTO build()
        {
            Assert.notNull(regId, "Missing info: regId");
            Assert.notNull(regTitle, "Missing info: regTitle");
            Assert.notNull(regStatus, "Missing info: regStatus");
            Assert.notNull(regEvent, "Missing info: regEvent");

            UpdateEventDTO dto = new UpdateEventDTO();
            dto.regId = this.regId;
            dto.regTitle = this.regTitle;
            dto.regStartDate = this.regStartDate;
            dto.regMotivation = this.motivation;
            dto.regStatus = this.regStatus;
            dto.regEvent = this.regEvent;

            dto.memberId = this.memberId;
            dto.memberFirstname = this.memberFirstname;
            dto.memberLastname = this.memberLastname;
            dto.memberFullname = this.memberFullname;
            dto.memberEmail = this.memberEmail;

            dto.actorName = this.actorName;
            dto.actorEmail = this.actorEmail;

            dto.manager = this.manager;
            return dto;
        }
    }
}
