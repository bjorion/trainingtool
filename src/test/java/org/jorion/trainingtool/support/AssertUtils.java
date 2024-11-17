package org.jorion.trainingtool.support;

import java.util.Set;

public class AssertUtils {

    @SafeVarargs
    public static <T> boolean containsExactly(Set<T> objects, T... expectedObjects) {

        if (objects.size() != expectedObjects.length) {
            return false;
        }
        for (var expectedObj : expectedObjects) {
            if (!objects.contains(expectedObj)) {
                return false;
            }
        }
        return true;
    }
}
