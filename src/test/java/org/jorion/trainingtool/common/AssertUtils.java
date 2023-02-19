package org.jorion.trainingtool.common;

import java.util.Set;

public class AssertUtils {

    public static <T> boolean containsExactly(Set<T> objects, Object... expectedObjects) {

        if (objects.size() != expectedObjects.length) {
            return false;
        }
        for (Object expectedObj : expectedObjects) {
            if (!objects.contains(expectedObj)) {
                return false;
            }
        }
        return true;
    }
}
