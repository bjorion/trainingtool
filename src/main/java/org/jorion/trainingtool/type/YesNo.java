package org.jorion.trainingtool.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Yes / No enumeration.
 * <p>
 * This replaces the Boolean object that I do not want to use because a Boolean can be {@code null}, and this not happen. Here,
 * the {@code null} value is allowed, meaning the user did not make a choice yet.
 */
@Getter
@RequiredArgsConstructor
public enum YesNo {

    YES("Yes"),

    NO("No");

    private final String title;

    /**
     * @return True if the given enumeration is not null and is YES.
     */
    public static boolean isYes(YesNo yn) {
        return (yn == YES);
    }

    public String getName() {
        return name();
    }

}
