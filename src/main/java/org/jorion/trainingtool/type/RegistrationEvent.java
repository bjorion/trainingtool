package org.jorion.trainingtool.type;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

public enum RegistrationEvent {

    /**
     * Assign a new training to a member. Special case of SAVE, where the creator is NOT the training recipient.
     */
    ASSIGN,

    /**
     * Request is deleted.
     */
    DELETE,

    /**
     * Create or update a training.
     */
    SAVE,

    /**
     * Submit/approve a training.
     */
    SUBMIT,

    /**
     * Refuse a training.
     */
    REFUSE,

    /**
     * Request a modification to the training.
     */
    SEND_BACK;

    /**
     * Events for which an email is to be sent.
     */
    public static final EnumSet<RegistrationEvent> EMAIL_SET = EnumSet.of(ASSIGN, SUBMIT, REFUSE, SEND_BACK);

    /**
     * @param value the value used
     * @return the event associated to the given value, or null if not found
     */
    public static RegistrationEvent findByValue(String value) {

        RegistrationEvent result = null;
        if (StringUtils.isNotEmpty(value)) {
            for (RegistrationEvent event : values()) {
                if (event.name().equals(value)) {
                    result = event;
                    break;
                }
            }
        }
        return result;
    }
}
