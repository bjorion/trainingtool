package org.jorion.trainingtool.types;

/**
 * Enumeration for the day period of the training. 
 */
public enum Period
{
    // --- Enum ---
    DAY("D", "During Business Hours"),

    EVENING("E", "After Business Hours (>= 18h30)");

    // --- Constants ---
    private final String title;

    private final String shortTitle;

    // --- Methods ---
    Period(String shortTitle, String title)
    {
        this.shortTitle = shortTitle;
        this.title = title;
    }

    public String getShortTitle()
    {
        return shortTitle;
    }

    public String getTitle()
    {
        return title;
    }

    public String getName()
    {
        return name();
    }

}
