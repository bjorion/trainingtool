package org.jorion.trainingtool.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import org.jorion.trainingtool.entities.base.AbstractTimeTrackingEntity;
import org.jorion.trainingtool.types.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Entity corresponding to the USER table.
 * <p>
 * The field {@code username} is the business key and should be unique.
 */
@Entity
@Table(name = "user", schema = "trainingtool")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "createdOn", "modifiedOn", "username", "pnr", "roles", "sector", "function", "lastname", "firstname", "phoneNumber",
        "email", "managername", "registrations" })
public class User extends AbstractTimeTrackingEntity
{
    // --- Constants ---
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(User.class);

    // --- Variables ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subco")
    private boolean subContractor;

    @NotNull
    private String pnr;

    @NotNull
    private String username;

    @Transient
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @Column(name = "role")
    private String rolesAsStr;

    @NotNull
    private String lastname;

    @NotNull
    private String firstname;

    @NotNull
    private String email;

    private String sector;

    private String phoneNumber;

    private String function;

    private String managername;

    @OneToMany(mappedBy = "member", fetch = FetchType.EAGER)
    private Set<Registration> registrations = new HashSet<>();

    // --- Methods ---
    /**
     * Default constructor.
     */
    public User()
    {}

    /**
     * Constructor used for testing purposes.
     * 
     * @param username the user's username
     */
    public User(String username)
    {
        this.username = username;
    }

    @PostLoad
    protected void postLoad()
    {
        // LOG.debug("Postload: rolesAsStr for [{}] = [{}]", username, rolesAsStr);
        String[] arr = rolesAsStr.split(":");
        for (String value : arr) {
            Role role = Role.findByValue(value);
            roles.add(role);
        }
    }

    // Object
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "User [id=" + id + ", username=" + username + ", roles=" + getRolesAsStr() + ", manager=" + managername + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(username).toHashCode();
    }

    /**
     * {@inheritDoc}
     * 
     * @return true if both users share the same username
     */
    @Override
    public boolean equals(Object other)
    {
        if (other == this) {
            return true;
        }
        if (other instanceof User == false) {
            return false;
        }
        User that = (User) other;
        return new EqualsBuilder().append(username, that.username).isEquals();
    }

    // Misc.
    /**
     * @return Concatenation of the lastname (in uppercase) and the firstname
     */
    @JsonIgnore
    public String getFullname()
    {
        return this.firstname + " " + StringUtils.upperCase(this.lastname);
    }

    /**
     * Copy updatable fields from the given user to the current one. Empty values fill not be copied for mandatory
     * fields.
     *
     * @param srcUser the user to copy the information from
     */
    public void convertFrom(User srcUser)
    {
        if (!StringUtils.isBlank(srcUser.getSector())) {
            this.setSector(srcUser.getSector());
        }
    }

    // roles
    /**
     * @return True if the user is a manager, HR or TRAINING.
     */
    public boolean isSupervisor()
    {
        return roles.stream().anyMatch(Role::isSupervisor);
    }

    /**
     * @return True if the user is a manager
     */
    public boolean isManager()
    {
        return roles.stream().anyMatch(Role::isManager);
    }

    /**
     * @return True if the user is a HR or TRAINING
     */
    public boolean isOffice()
    {
        return roles.stream().anyMatch(Role::isOffice);
    }

    /**
     * @return True if the user is a HR
     */
    public boolean isHr()
    {
        return roles.stream().anyMatch(Role::isHr);
    }

    /**
     * @return True if the user is TRAINING
     */
    public boolean isTraining()
    {
        return roles.stream().anyMatch(Role::isTraining);
    }

    /**
     * @return True if the user is ADMIN
     */
    public boolean isAdmin()
    {
        return roles.stream().anyMatch(Role::isAdmin);
    }

    /**
     * @return A string of concatenated roles
     */
    public String getRolesAsStr()
    {
        return roles.stream().map(Role::getName).collect(Collectors.joining(":"));
    }

    /**
     * @return an unmodifiable set of roles
     */
    public Set<Role> getRoles()
    {
        return Collections.unmodifiableSet(roles);
    }

    /**
     * Add the given role to the user.
     */
    public User addRole(Role role)
    {
        this.roles.add(role);
        this.rolesAsStr = getRolesAsStr();
        return this;
    }

    /**
     * Set the given roles to the user.
     */
    public void setRoles(Set<Role> roles)
    {
        this.roles.clear();
        this.roles.addAll(roles);
        this.rolesAsStr = getRolesAsStr();
    }

    // registrations
    /**
     * Find the registration assigned to this user given its id.
     * 
     * @param regId the registration id
     * @return the registration, null if not found or if regId is null
     */
    public Registration findRegistrationById(Long regId)
    {
        if (regId == null) {
            return null;
        }
        Registration r = registrations.stream().filter(e -> regId.equals(e.getId())).findFirst().orElseGet(() -> null);
        return r;
    }

    /**
     * Add the given registration. If the registration already exists (identified by its id), throws an
     * {@link IllegalArgumentException}.
     * 
     * @param registration the registration, cannot be null
     */
    public void addRegistration(Registration registration)
    {
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
    public boolean replaceRegistration(Registration registration)
    {
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
    public boolean removeRegistration(Registration registration)
    {
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

    // --- Getters and Setters ---
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public boolean isSubContractor()
    {
        return subContractor;
    }

    public void setSubContractor(boolean subContractor)
    {
        this.subContractor = subContractor;
    }

    public String getPnr()
    {
        return pnr;
    }

    public void setPnr(String pnr)
    {
        this.pnr = pnr;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    public String getLastname()
    {
        return lastname;
    }

    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getSector()
    {
        return sector;
    }

    public void setSector(String sector)
    {
        this.sector = sector;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getFunction()
    {
        return function;
    }

    public void setFunction(String function)
    {
        this.function = function;
    }

    public String getManagername()
    {
        return managername;
    }

    public void setManagername(String managername)
    {
        this.managername = managername;
    }

    public Set<Registration> getRegistrations()
    {
        return Collections.unmodifiableSet(registrations);
    }

}
