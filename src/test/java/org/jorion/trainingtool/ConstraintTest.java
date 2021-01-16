package org.jorion.trainingtool;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import jdepend.framework.JDepend;

/**
 * Check constraints on the whole application.
 */
public class ConstraintTest
{
    // --- Variables ---
    private JDepend jdepend;

    // --- Methods ---
    @Before
    public void setUp()
            throws IOException
    {
        jdepend = new JDepend();
        jdepend.addDirectory("./target/classes/org/jorion");
    }

    /**
     * Tests that a package dependency cycle does not exist for any of the analyzed packages.
     */
    @Test
    public void testPackageCycles()
    {
        boolean cycles = jdepend.containsCycles();
        assertEquals("Cycles exist", false, cycles);
    }
}
