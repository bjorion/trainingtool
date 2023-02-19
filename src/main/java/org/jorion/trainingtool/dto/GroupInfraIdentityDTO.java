package org.jorion.trainingtool.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jorion.trainingtool.type.Role;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Encapsulate information from LDAP.
 */
@Getter
@Setter
@NoArgsConstructor
public class GroupInfraIdentityDTO {

    // common data
    private boolean subcontractor;

    private String pnr;

    private String employeeType;

    private String division;

    /**
     * username
     */
    private String account;

    private String firstname;

    private String lastname;

    private String email;

    private String phone;

    private String mobile;

    private String function;

    private String managerName;

    private Set<Role> roles = new HashSet<>();

    @Override
    public String toString() {

        return "GroupinfraIdentityDTO [account=" + account +
                ", employeeType=" + employeeType +
                ", pnr=" + pnr +
                ", firstname=" + firstname +
                ", lastname=" + lastname +
                ", email=" + email +
                ", phone=" + phone +
                ", mobile=" + mobile +
                ", function=" + function +
                ", roles=" + getRolesAsStr() +
                ", manager=" + managerName +
                "]";

    }

    /**
     * @return A string of concatenated roles
     */
    public String getRolesAsStr() {
        return roles.stream().map(Role::getName).collect(Collectors.joining(":"));
    }

    /**
     * @return mobile number if not empty, otherwise phone number
     */
    public String getMobileOrPhone() {
        return StringUtils.isBlank(getMobile()) ? getPhone() : getMobile();
    }

    // --- Getters and Setters ---
    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void addRoles(Set<Role> roles) {
        this.roles.addAll(roles);
    }

}
