package org.alogvinov.affprop.test;

import org.apache.pig.pigunit.Cluster;
import org.apache.pig.pigunit.PigTest;
import org.junit.Test;
import org.junit.Before;

public class LoadVerticesTests {
    private PigTest test;
    private static Cluster cluster;
 
    @Before
    public void createLoad() throws Exception {
        String[] params = {
            "PIGGYBANK_JAR=" + System.getenv("PIGGYBANK_JAR"),
            "SIMILARITY_JAR=build/test.jar",
            "SIMILARITY_FUN=org.alogvinov.affprop.test.TestSim.testSimExclude",
            "STORAGE=PigStorage('\\\\t')",
            "INPUT=dummy",
            "EDGES_OUT=dummy",
            "PIGUNIT_SORT='edges = ORDER edges BY i,k;'"
        };
        test = new PigTest("../../load_vertices.pig", params);
    }

    @Test
    public void noData() throws Exception {
        String[] input = {};
        String[] output = {};
        test.assertOutput("source", input, "edges", output);
    }

    @Test
    public void singleElementNoPref() throws Exception {
        String[] input = {"1,2"};
        String[] output = {"(1,2,1,2,0.0,0,0,false,false)"};
        test.assertOutput("source", input, "edges", output);
    }

    @Test
    public void singleElementPref() throws Exception {
        String[] input = {"1,2\t3"};
        String[] output = {"(1,2,1,2,3.0,0,0,false,false)"};
        test.assertOutput("source", input, "edges", output);
    }

    @Test
    public void withPrefernce() throws Exception {
        String[] input = {
            "0,0\t2",
            "0,1",
            "0,5"
        };
        String[] output = {
            "(0,0,0,0,2.0,0,0,false,false)",  // 2.0 source preference
            "(0,0,0,1,-1.0,0,0,false,false)",
            "(0,0,0,5,-5.0,0,0,false,false)",
            "(0,1,0,0,-1.0,0,0,false,false)",
            "(0,1,0,1,-3.0,0,0,false,false)", // -3.0 similarity median
            "(0,1,0,5,-4.0,0,0,false,false)",
            "(0,5,0,0,-5.0,0,0,false,false)",
            "(0,5,0,1,-4.0,0,0,false,false)",
            "(0,5,0,5,-3.0,0,0,false,false)" // -3.0 similarity median
        };
        test.assertOutput("source", input, "edges", output);
    }

    @Test
    public void omitEdges() throws Exception {
        String[] input = {
            "0,0\t1",
            "0,1",
            "20,20"
        };
        String[] output = {
            "(0,0,0,0,1.0,0,0,false,false)",
            "(0,0,0,1,-1.0,0,0,false,false)",
            "(0,1,0,0,-1.0,0,0,false,false)",
            "(0,1,0,1,-1.0,0,0,false,false)",
            "(20,20,20,20,-1.0,0,0,false,false)",
        };
        test.assertOutput("source", input, "edges", output);
    }
}