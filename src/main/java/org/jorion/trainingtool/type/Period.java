package org.jorion.trainingtool.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration for the day period of the training.
 */
@Getter
@RequiredArgsConstructor
public enum Period {

    DAY("D", "During Business Hours"),

    EVENING("E", "After Business Hours (>= 18h30)");

    private final String title;
    private final String shortTitle;

    public String getName() {
        return name();
    }

}
