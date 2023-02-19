package org.jorion.trainingtool.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {

    CEVORA("Cevora", "E", true, true),

    COURSERA("Coursera", "E", false, true),

    ACADEMIA("Academia Percipio", "I", false, true),

    INTERNAL("Internal", "I", false, false),

    OTHER("Other", "E", false, false);

    private final String title;
    private final String internalExternal;
    private final boolean ssinMandatory;
    private final boolean fieldMandatory;

    public String getName() {
        return name();
    }

}
