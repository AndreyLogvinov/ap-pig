package org.alogvinov.affprop.test;

import org.apache.pig.pigunit.Cluster;
import org.apache.pig.pigunit.PigTest;
import org.junit.Test;
import org.junit.Before;

public class StoreResultTests {
    private PigTest test;
    private static Cluster cluster;
 
    @Before
    public void createStore() throws Exception {
        String[] params = {
            "STORAGE=PigStorage('\\\\t')",
            "OUTPUT=dummy",
            "EDGES_IN=dummy"
        };
        test = new PigTest("../../store.pig", params);
    }

    @Test
    public void noData() throws Exception {
        String[] input = {};
        String[] output = {};
        test.assertOutput("edges_in", input, "result", output);
    }

    @Test
    public void testData() throws Exception {
        String[] input = {
            "a\tb\t1\t1\t1\tfalse\tfalse",
            "c\td\t1\t1\t1\tfalse\ttrue",
            "e\tf\t1\t1\t1\ttrue\tfalse",
            "g\th\t1\t1\t1\ttrue\ttrue",
        };
        String[] output = {
            "(d,c)",
            "(h,g)",
        };
        test.assertOutput("edges_in", input, "result", output);
    }

}