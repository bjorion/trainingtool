package org.jorion.trainingtool.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum RegistrationStatus {

    NONE("None", null, 0),

    /**
     * Created but not yet submitted
     */
    DRAFT("Draft", Role.MEMBER, 1),

    SUBMITTED_TO_MANAGER("Submitted to Manager", Role.MANAGER, 2),

    REFUSED_BY_MANAGER("Refused by Manager", Role.MANAGER, 0),

    SUBMITTED_TO_HR("Submitted to HR", Role.HR, 3),

    REFUSED_BY_HR("Refused by HR", Role.HR, 0),

    SUBMITTED_TO_TRAINING("Submitted to BeTraining", Role.TRAINING, 4),

    REFUSED_BY_TRAINING("Refused by BeTraining", Role.TRAINING, 0),

    SUBMITTED_TO_PROVIDER("Submitted to Provider", Role.TRAINING, 4),

    REFUSED_BY_PROVIDER("Refused by Provider", Role.TRAINING, 0),

    APPROVED_BY_PROVIDER("Accepted by Provider", null, 5);

    public static final EnumSet<RegistrationStatus> REFUSED_SET =
            EnumSet.of(REFUSED_BY_MANAGER, REFUSED_BY_HR, REFUSED_BY_TRAINING, REFUSED_BY_PROVIDER);

    public static final EnumSet<RegistrationStatus> APPROVED_SET =
            EnumSet.of(APPROVED_BY_PROVIDER);

    public static final EnumSet<RegistrationStatus> PENDING_SET =
            EnumSet.of(DRAFT, SUBMITTED_TO_MANAGER, SUBMITTED_TO_HR, SUBMITTED_TO_TRAINING,
            SUBMITTED_TO_PROVIDER);

    /**
     * Human-readable name.
     */
    final String title;

    /**
     * The role responsible for handling this status.
     */
    final Role responsible;

    /**
     * The step number in the whole workflow.
     */
    final int step;

    public String getName() {
        return name();
    }

    public boolean isRefused() {
        return REFUSED_SET.contains(this);
    }

    public boolean isApproved() {
        return APPROVED_SET.contains(this);
    }

    public boolean isPending() {
        return PENDING_SET.contains(this);
    }

    public boolean isPendingNotDraft() {
        return (this != DRAFT) && PENDING_SET.contains(this);
    }

    public boolean isGTE(int step) {
        return (this.step >= step);
    }

    public boolean isGTE(RegistrationStatus status) {
        return (this.step >= status.step);
    }

    /**
     * @param roles a set of roles
     * @return True if ANY of the given roles is responsible for the current status. Admin role is considered responsible for
     * all statuses.
     */
    public boolean isResponsible(Set<Role> roles) {

        return roles.stream().anyMatch(e -> e == this.responsible || e == Role.ADMIN);
    }

    /**
     * @return The REFUSED status following the current status.
     */
    public RegistrationStatus getNextStatusAfterRefusal() {

        if (!isPending()) {
            return null;
        }

        RegistrationStatus nextStatus;
        switch (this) {
            case SUBMITTED_TO_MANAGER:
                nextStatus = REFUSED_BY_MANAGER;
                break;
            case SUBMITTED_TO_HR:
                nextStatus = REFUSED_BY_HR;
                break;
            case SUBMITTED_TO_TRAINING:
                nextStatus = REFUSED_BY_TRAINING;
                break;
            case SUBMITTED_TO_PROVIDER:
                nextStatus = REFUSED_BY_PROVIDER;
                break;
            default:
                nextStatus = null;
                break;
        }
        return nextStatus;
    }

    /**
     * @param hrWorkflow true if the request must go through HR
     * @return The APPROVED (submitted) status following the current status.
     */
    public RegistrationStatus getNextStatusAfterApproval(boolean hrWorkflow) {

        if (!isPending()) {
            return null;
        }

        RegistrationStatus nextStatus;
        switch (this) {
            case DRAFT:
                nextStatus = SUBMITTED_TO_MANAGER;
                break;
            case SUBMITTED_TO_MANAGER:
                nextStatus = hrWorkflow ? SUBMITTED_TO_HR : SUBMITTED_TO_TRAINING;
                break;
            case SUBMITTED_TO_HR:
                nextStatus = SUBMITTED_TO_TRAINING;
                break;
            case SUBMITTED_TO_TRAINING:
                nextStatus = SUBMITTED_TO_PROVIDER;
                break;
            case SUBMITTED_TO_PROVIDER:
                nextStatus = APPROVED_BY_PROVIDER;
                break;
            default:
                nextStatus = null;
                break;
        }
        return nextStatus;
    }
}
