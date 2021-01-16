package org.jorion.trainingtool.dto;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.jorion.trainingtool.types.Role;

/**
 * Encapsulate information from LDAP.
 */
public class GroupinfraIdentityDTO
{
    // --- Variables ---
    // common data
    private boolean subcontractor;

    private String pnr;

    private String employeeType;

    private String division;

    private String account;

    private String firstname;

    private String lastname;

    private String email;

    private String phone;

    private String mobile;

    private String function;

    private String managername;

    private Set<Role> roles = new HashSet<>();

    // --- Methods ---
    @Override
    public String toString()
    {
        // @formatter:off
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
                ", manager=" + managername +
                "]";
        // @formatter:on
    }

    /**
     * @return A string of concatenated roles
     */
    public String getRolesAsStr()
    {
        return roles.stream().map(Role::getName).collect(Collectors.joining(":"));
    }

    /**
     * @return mobile number if not empty, otherwise phone number
     */
    public String getMobileOrPhone()
    {
        String value = StringUtils.isBlank(getMobile()) ? getPhone() : getMobile();
        return value;
    }

    // --- Getters and Setters ---
    public boolean isSubcontractor()
    {
        return subcontractor;
    }

    public void setSubcontractor(boolean subcontractor)
    {
        this.subcontractor = subcontractor;
    }

    public String getPnr()
    {
        return pnr;
    }

    public void setPnr(String pnr)
    {
        this.pnr = pnr;
    }

    public String getEmployeeType()
    {
        return employeeType;
    }

    public void setEmployeeType(String employeeType)
    {
        this.employeeType = employeeType;
    }

    public String getDivision()
    {
        return division;
    }

    public void setDivision(String division)
    {
        this.division = division;
    }

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
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

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getFunction()
    {
        return function;
    }

    public void setFunction(String function)
    {
        this.function = function;
    }

    public Set<Role> getRoles()
    {
        return Collections.unmodifiableSet(roles);
    }
    
    public void addRole(Role role)
    {
        this.roles.add(role);
    }

    public void addRoles(Set<Role> roles)
    {
        this.roles.addAll(roles);
    }

    public String getManagername()
    {
        return managername;
    }

    public void setManagername(String managername)
    {
        this.managername = managername;
    }

}
