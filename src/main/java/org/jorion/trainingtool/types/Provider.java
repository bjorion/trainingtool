package org.jorion.trainingtool.types;

/**
 * Enumeration of providers.
 */
public enum Provider
{
    // --- Enum ---
    CEVORA("Cevora", "E", true, true),

    COURSERA("Coursera", "E", false, true),

    ACADEMIA("Academia Percipio", "I", false, true), 

    INTERNAL("Internal", "I", false, false),

    OTHER("Other", "E", false, false);

    // --- Constants ---
    private final String title;

    private final String internalExternal;

    private final boolean ssinMandatory;

    private final boolean fieldMandatory;

    // --- Methods ---
    /**
     * Constructor.
     * 
     * @param title The provider title
     * @param internalExternal E or I. E = External; I = Internal.
     * @param ssinMandatory true if the SSIN is mandatory for that provider
     * @param fieldMandatory true if some fields are mandatory for that provider
     */
    Provider(String title, String internalExternal, boolean ssinMandatory, boolean fieldMandatory)
    {
        this.title = title;
        this.internalExternal = internalExternal;
        this.ssinMandatory = ssinMandatory;
        this.fieldMandatory = fieldMandatory;
    }

    public String getName()
    {
        return name();
    }

    public String getTitle()
    {
        return title;
    }

    public String getInternalExternal()
    {
        return internalExternal;
    }

    public boolean isSsinMandatory()
    {
        return ssinMandatory;
    }
    
    public boolean isFieldMandatory()
    {
        return fieldMandatory;
    }

}
