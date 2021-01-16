package org.jorion.trainingtool.dto;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import org.jorion.trainingtool.types.RegistrationStatus;

/**
 * Container for the registration request search criteria (page report).
 */
public class ReportDTO implements Serializable
{
    // --- Variables ---
    private String lastname;

    @DateTimeFormat(iso = ISO.DATE)
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;
    
    private String username;

    private String manager;

    // --- Methods ---
    /**
     * @return true if all the selection criteria are empty
     */
    public boolean isEmpty()
    {
        return StringUtils.isBlank(lastname) && (startDate == null) && (status == null);
    }

    // --- Getters and Setters ---
    public String getLastname()
    {
        return lastname;
    }

    @Override
    public String toString()
    {
        return "ReportDTO [lastname=" + lastname + ", startDate=" + startDate + ", status=" + status + ", manager=" + manager + "]";
    }

    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    public LocalDate getStartDate()
    {
        return startDate;
    }

    public void setStartDate(LocalDate startDate)
    {
        this.startDate = startDate;
    }

    public RegistrationStatus getStatus()
    {
        return status;
    }

    public void setStatus(RegistrationStatus status)
    {
        this.status = status;
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getManager()
    {
        return manager;
    }

    public void setManager(String manager)
    {
        this.manager = manager;
    }

}
