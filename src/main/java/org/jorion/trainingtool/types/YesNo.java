package org.jorion.trainingtool.types;

/**
 * Yes / No enumeration.
 * <p>
 * This replaces the Boolean object that I do not want to use because a Boolean can be {@code null}, and this not happen. Here,
 * the {@code null} value is allowed, meaning the user did not make a choice yet.
 */
public enum YesNo
{
    // --- Enum ---
    YES("Yes"),

    NO("No");

    // --- Constants ---
    private final String title;

    // --- Methods ---
    YesNo(String title)
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

    /**
     * @return True if the given enumeration is not null and is YES.
     */
    public static boolean isYes(YesNo yn)
    {
        return (yn != null) && (yn == YES);
    }

}
