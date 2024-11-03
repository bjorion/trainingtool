package org.jorion.trainingtool.entity;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jorion.trainingtool.entity.base.AbstractTimeTrackingEntity;
import org.jorion.trainingtool.type.Role;
import org.springframework.util.Assert;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entity corresponding to the USER table.
 * <p>
 * The field {@code username} is the business key and should be unique.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "myuser")
public class User extends AbstractTimeTrackingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subco")
    private boolean subContractor;

    @NotNull
    private String pnr;

    @NotNull
    @Column(name = "username")
    private String userName;

    @Transient
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * For internal use only.
     */
    @Column(name = "role")
    private String rolesAsStr;

    @NotNull
    @Column(name = "lastname")
    private String lastName;

    @NotNull
    @Column(name = "firstname")
    private String firstName;

    @NotNull
    private String email;

    private String sector;

    private String phoneNumber;

    private String function;

    @Column(name = "managername")
    private String managerName;

    @Builder.Default
    @OneToMany(mappedBy = "member", fetch = FetchType.EAGER)
    private Set<Registration> registrations = new HashSet<>();

    @PostLoad
    protected void postLoad() {

        String[] arr = rolesAsStr.split(":");
        for (String value : arr) {
            Role role = Role.findByValue(value);
            roles.add(role);
        }
    }

    // Object
    @Override
    public String toString() {

        return "User [id=" + id + ", user=" + userName + ", roles=" + getRolesAsStr() + ", manager=" + managerName + "]";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(userName).toHashCode();
    }

    /**
     * @return true if both users share the same username
     */
    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }
        if (!(other instanceof User that)) {
            return false;
        }
        return new EqualsBuilder().append(userName, that.userName).isEquals();
    }

    // Misc.

    /**
     * @return Concatenation of the firstname and the lastname (in uppercase)
     */
    public String getFullName() {
        return this.firstName + " " + StringUtils.upperCase(this.lastName);
    }

    /**
     * Copy updatable fields from the given user to the current one. Empty values fill not be copied for mandatory
     * fields.
     *
     * @param srcUser the user to copy the information from
     */
    public void convertFrom(User srcUser) {

        if (!StringUtils.isBlank(srcUser.getSector())) {
            this.setSector(srcUser.getSector());
        }
    }

    // roles

    /**
     * @return True if the user is a manager, HR or TRAINING.
     */
    public boolean isSupervisor() {
        return roles.stream().anyMatch(Role::isSupervisor);
    }

    /**
     * @return True if the user is a manager
     */
    public boolean isManager() {
        return roles.stream().anyMatch(Role::isManager);
    }

    /**
     * @return True if the user is a HR or TRAINING
     */
    public boolean isOffice() {
        return roles.stream().anyMatch(Role::isOffice);
    }

    /**
     * @return True if the user is a HR
     */
    public boolean isHr() {
        return roles.stream().anyMatch(Role::isHr);
    }

    /**
     * @return True if the user is TRAINING
     */
    public boolean isTraining() {
        return roles.stream().anyMatch(Role::isTraining);
    }

    /**
     * @return True if the user is ADMIN
     */
    public boolean isAdmin() {
        return roles.stream().anyMatch(Role::isAdmin);
    }

    /**
     * @return A string of concatenated roles
     */
    protected String getRolesAsStr() {
        return roles.stream().map(Role::getName).collect(Collectors.joining(":"));
    }

    /**
     * @return an unmodifiable set of roles
     */
    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    /**
     * Set the given roles to the user.
     */
    public void setRoles(Set<Role> roles) {

        this.roles.clear();
        this.roles.addAll(roles);
        this.rolesAsStr = getRolesAsStr();
    }

    /**
     * Add the given role to the user.
     */
    public User addRole(Role role) {

        this.roles.add(role);
        this.rolesAsStr = getRolesAsStr();
        return this;
    }

    // registrations

    /**
     * Find the registration assigned to this user given its id.
     *
     * @param regId the registration id
     * @return the registration, null if not found or if regId is null
     */
    public Registration findRegistrationById(Long regId) {

        if (regId == null) {
            return null;
        }
        return registrations.stream().filter(e -> regId.equals(e.getId())).findFirst().orElse(null);
    }

    /**
     * Add the given registration. If the registration already exists (identified by its id), throws an
     * {@link IllegalArgumentException}.
     *
     * @param registration the registration, cannot be null
     */
    public void addRegistration(Registration registration) {

        Assert.notNull(registration, "Registration cannot be null");
        Assert.notNull(registration.getId(), "Registration id cannot be null");
        if (findRegistrationById(registration.getId()) != null) {
            throw new IllegalArgumentException("Registration id already exists: " + registration.getId());
        }
        registrations.add(registration);
    }

    /**
     * Replace the given registration but only if it already exists.
     *
     * @param registration the registration
     * @return true if a replace did happen, false otherwise
     */
    public boolean replaceRegistration(Registration registration) {

        Assert.notNull(registration, "Registration cannot be null");
        Assert.notNull(registration.getId(), "Registration id cannot be null");

        boolean result = false;
        Registration current = findRegistrationById(registration.getId());
        if (current != null) {
            registrations.remove(current);
            registrations.add(registration);
            result = true;
        }
        return result;
    }

    /**
     * Remove the given registration if it's linked to the user.
     *
     * @param registration the registration
     * @return true if a remove did happen, false otherwise
     */
    public boolean removeRegistration(Registration registration) {

        Assert.notNull(registration, "Registration cannot be null");
        Assert.notNull(registration.getId(), "Registration id cannot be null");

        boolean result = false;
        Registration current = findRegistrationById(registration.getId());
        if (current != null) {
            registrations.remove(current);
            result = true;
        }
        return result;
    }

    public Set<Registration> getRegistrations() {
        return Collections.unmodifiableSet(registrations);
    }

    // we need to declare UserBuilder ourselves 
    // otherwise JavaDoc will not see it and throw an error
    public static class UserBuilder {
    }
}
