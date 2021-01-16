package org.jorion.trainingtool.types;

/**
 * Enumeration of providers.
 */
public enum Location
{
    // --- Enum ---
    CLASSROOM("Classroom"),

    VIRTUAL("Virtual");

    // --- Constants ---
    private final String title;

    // --- Methods ---
    Location(String title)
    {
        this.title = title;
    }
    
    public String getName()
    {
        return name();
    }
    
    public String getTitle()
    {
        return title;
    }
}
