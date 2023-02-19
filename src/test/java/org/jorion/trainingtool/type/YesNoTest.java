package org.jorion.trainingtool.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class YesNoTest {

    @Test
    public void testIsYes() {

        assertFalse(YesNo.isYes(null));
        assertTrue(YesNo.isYes(YesNo.YES));
        assertFalse(YesNo.isYes(YesNo.NO));
    }

}
