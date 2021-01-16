package org.jorion.trainingtool.types;

import java.util.Arrays;
import java.util.EnumSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration of the different roles in the application. Each user is assigned only one role.
 */
public enum Role
{
    // --- Enum ---
    /** Simplest role, fewer access rights. */
    MEMBER,

    /** Role for manager (DCS...). */
    MANAGER,

    /** Role for HR. */
    HR,

    /** Role for Training. */
    TRAINING,

    /** Complete access, including Actuator. */
    ADMIN;

    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(Role.class);

    public static final String ROLE = "ROLE_";

    private static final EnumSet<Role> SUPERVISOR_SET = EnumSet.of(MANAGER, HR, TRAINING, ADMIN);

    private static final EnumSet<Role> OFFICE_SET = EnumSet.of(HR, TRAINING, ADMIN);

    private static String[] supervisors;

    // --- Methods ---
    /**
     * Useful in thymeleaf pages.
     */
    public String getName()
    {
        return name();
    }

    /**
     * @return the role name prefixed with the string "ROLE_"
     */
    public String getRoleName()
    {
        return ROLE + name();
    }

    /**
     * @return true if the role is a supervisor role (manager, hr, training)
     */
    public boolean isSupervisor()
    {
        return SUPERVISOR_SET.contains(this);
    }

    /**
     * @return true if the role is Manager
     */
    public boolean isManager()
    {
        return this == Role.MANAGER;
    }

    /**
     * @return true if the role is HR or above
     */
    public boolean isOffice()
    {
        return OFFICE_SET.contains(this);
    }

    /**
     * @return true if the role is HR
     */
    public boolean isHr()
    {
        return this == Role.HR;
    }

    /**
     * @return true if the role is training
     */
    public boolean isTraining()
    {
        return this == Role.TRAINING;
    }

    /**
     * @return true if the role is admin
     */
    public boolean isAdmin()
    {
        return this == Role.ADMIN;
    }

    /**
     * @return an array of strings containing the supervisor names.
     */
    public synchronized static String[] getSupervisors()
    {
        if (supervisors == null) {
            supervisors = Arrays.stream(Role.class.getEnumConstants()).filter(e -> e.isSupervisor()).map(Enum::name).toArray(String[]::new);
        }
        
        // we return a copy because we do not want the caller to be able to modify the original
        return Arrays.copyOf(supervisors, supervisors.length);
    }

    /**
     * @param value the value used
     * @return the role associated to the given value, or MEMBER if not found
     */
    public static Role findByValue(String value)
    {
        Role result = null;
        String newValue = value;
        if (StringUtils.isNotEmpty(newValue)) {
            if (newValue.startsWith(ROLE)) {
                newValue = newValue.substring(ROLE.length());
            }
            for (Role role : values()) {
                if (role.name().equals(newValue)) {
                    result = role;
                    break;
                }
            }
        }
        if (result == null) {
            LOG.warn("value [{}] unknown, role will be set to MEMBER per default", newValue);
            result = MEMBER;
        }
        return result;
    }
}
