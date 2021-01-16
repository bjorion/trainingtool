package org.jorion.trainingtool.types;

import java.util.EnumSet;

import org.apache.commons.lang3.StringUtils;

/**
 * Enumeration of events (eg. actions from the user).
 */
public enum RegistrationEvent
{
    // --- Enum ---
    /** Assign a new training to a member. Special case of SAVE, where the creator is NOT the training recipient. */
    ASSIGN,
    
    /** Request is deleted. */
    DELETE,

    /** Create or update a training. */
    SAVE,

    /** Submit/approve a training. */
    SUBMIT,

    /** Refuse a training. */
    REFUSE,

    /** Request a modification to the training. */
    SEND_BACK,
    
    /** Unauthorized operation. */
    UNAUTHORIZED;

    // --- Constants ---
    /** Events for which an email is to be sent. */
    public static final EnumSet<RegistrationEvent> EMAIL_SET = EnumSet.of(ASSIGN, SUBMIT, REFUSE, SEND_BACK);
    
    // --- Methods ---
    /**
     * @param value the value used
     * @return the event associated to the given value, or null if not found
     */
    public static RegistrationEvent findByValue(String value)
    {
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
