package org.alogvinov.affprop.test;

import org.apache.pig.pigunit.Cluster;
import org.apache.pig.pigunit.PigTest;
import org.junit.Test;
import org.junit.Before;

public class LoadEdgesTests {
    private PigTest test;
    private static Cluster cluster;
 
    @Before
    public void createLoad() throws Exception {
        String[] params = {
            "STORAGE=PigStorage('\\\\t')",
            "INPUT=dummy",
            "EDGES_OUT=dummy",
            "PIGUNIT_SORT='edges = ORDER edges BY i,k;'"
        };
        test = new PigTest("../../load_edges.pig", params);
    }

    @Test
    public void noData() throws Exception {
        String[] input = {};
        String[] output = {};
        test.assertOutput("source", input, "edges", output);
    }

    @Test
    public void testData() throws Exception {
        String[] input = {"a\tb\t1"};
        String[] output = {"(a,b,1.0,0,0,false,false)"};
        test.assertOutput("source", input, "edges", output);
    }

}