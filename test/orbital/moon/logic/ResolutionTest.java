/**
 * @(#)ResolutionTest.java 1.1 2002-09-15 Andre Platzer
 * 
 * Copyright (c) 2002 Andre Platzer. All Rights Reserved.
 */

package orbital.moon.logic;

import junit.framework.*;

/**
 * A sample test case, testing Resolution.
 * @version $Id$
 * @attribute run-time 3min assuming java -da
 * @attribute run-time 50min assuming java -ea
 */
public class ResolutionTest extends ClassicalLogicTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        return new TestSuite(ResolutionTest.class);
    }

    protected void test(String name) {
        try {
            ClassicalLogic.main(new String[] {"-inference=RESOLUTION_INFERENCE", name});
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            fail(ex.getMessage() + " in file " + name);
        }
    }
    public void testFol() {
        test("fol");
    }
}
