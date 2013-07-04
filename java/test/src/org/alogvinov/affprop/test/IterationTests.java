package org.alogvinov.affprop.test;

import org.apache.pig.pigunit.Cluster;
import org.apache.pig.pigunit.PigTest;
import org.junit.Test;
import org.junit.Before;

public class IterationTests {
    private PigTest test;
    private static Cluster cluster;
 
    @Before
    public void createIteration() throws Exception {
        String[] params = {
            "PIGGYBANK_JAR=" + System.getenv("PIGGYBANK_JAR"),
            "STORAGE=PigStorage('\\\\t')",
            "EDGES_IN=dummy",
            "EDGES_OUT=dummy",
            "CONVERGENCE_OUT=dummy",
            "DAMP=0.5",
            "MAX_ITER=2",
            "CONV_ITER=1",
            "PIGUNIT_SORT='edges_d = ORDER edges_d BY i,k;'"
        };
        test = new PigTest("../../iteration.pig", params);
    }

    /** 
     * What is going to be excercised:
     * - grouping is correct for availability and responsibility
     * - damping is correct
     * - for responsibility:
     *   - k' = k is not inclided in maximum calculation for each edge
     * - for availability:
     *   - i' = i and i' = k are not included in sum calculation
     *   - only positiver responsibilities are included in sum
     *   - self-availability is updated differently
     * - decision indeed selects edges with max a + r
     * - convergence correctly determines the number of changed decisions
     */
    @Test
    public void test1() throws Exception {
        String[] input = {
            "a\ta\t-1\t-2\t3\tfalse\tfalse\t",
            "a\tb\t-1\t-4\t5\tfalse\tfalse\t",
            "a\tc\t-1\t-6\t-7\tfalse\tfalse\t",
            "b\ta\t-2\t-2\t-3\tfalse\tfalse\t",
            "b\tb\t-2\t-4\t5\tfalse\tfalse\t",
            "b\tc\t-2\t-6\t7\tfalse\tfalse\t",
            "c\ta\t-3\t-2\t3\tfalse\tfalse\t",
            "c\tb\t-4\t-4\t5\tfalse\tfalse\t",
            "c\tc\t-3\t-6\t-7\tfalse\tfalse\t"
        };
        String[] output = {
            "(a,a,-1.0,1.25,3.5,false,true)",
            "(a,b,-1.0,-2.0,3.5,false,false)",
            "(a,c,-1.0,-3.0,-2.5,false,false)",
            "(b,a,-2.0,-1.0,0.5,false,false)",
            "(b,b,-2.0,1.25,3.5,false,true)",
            "(b,c,-2.0,-4.25,4.5,false,false)",
            "(c,a,-3.0,-1.0,4.0,false,true)",
            "(c,b,-4.0,-2.0,3.0,false,false)",
            "(c,c,-3.0,-0.75,-2.5,false,false)"
        };
        String[] conv = {
            "(2,1,3)"
        };
        test.assertOutput("edges", input, "edges_d", output);
        test.assertOutput("edges", input, "convergence", conv);
    }

}