package org.jorion.trainingtool.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of providers.
 */
@Getter
@RequiredArgsConstructor
public enum Location {

    CLASSROOM("Classroom"),

    VIRTUAL("Virtual");

    private final String title;

    public String getName() {
        return name();
    }

}
