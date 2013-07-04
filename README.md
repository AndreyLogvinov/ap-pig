# Affinity Propagation clustering on Hadoop using Apache Pig
**ap-pig** implements Affinity Propagation clustering algorithm as defined in [Clustering by Passing Messages Between Data Points][1] by Brendan J. Frey and Delbert Dueck on Hadoop platform using PigLatin as algorithm description language.

For detailed information about Affinity Propagation visit algorithm authors' [website][2].

## License
This software is provided under the [MIT License](http://opensource.org/licenses/MIT)

Copyright (c) 2013 Andrey Logvinov

## Prerequisites
**ap-pig** requires Apache Hadoop and Apache Pig to be installed on the system. Cloudera CDH4.3 distribution was used for development.
Additionally, JUnit is required for unit tests.

## Operation principle
**ap-pig** operates on directed graph constructed on data points for which clustering is performed. Each edge _(i,k)_ contains, among other information, similarty of data point _i_ to _k_, current responsibility value from _i_ to _k_, and current availability value from _k_ to _i_.

There are three processing stages:

1. Load data: transform input located at `$INPUT` to edges
2. Perform iterations until convergence is achieved (there has been `$CONV_ITER` iterations without change in decisions) or maximum number of iterations `$MAX_ITER` is reached
3. Store result:transform edges to program output located at `$OUTPUT`
These steps are performed using Python workflow script `workflow.py`.

For intermediate output, current working directory (local mode) or HDFS user home directory (mapreduce mode) is used.

## Data loading
**ap-pig** can use arbitrary pig script as data loader as long as it satisfies the following conditions:

1. Input is read form `$INPUT`
2. Output is written to `$EDGES_OUT` using `$STORAGE`
3. Output has the format required by `iteration.pig` script

Two such loaders are prodived:

1. Vertex loader - reads data points and optionally preferences, calls user-specified Java function which determines similarities between data points.
2. Edge loader - reads pre-computed similarity values (including self-similarities - preferences)

### Vertex loader
Vertex loader `load_vertices.pig` reads data points from `$INPUT`, calls user similarity function for each pair and outputs an edge if similarity value is not negative infinity.
For self-edges, a different aproach is used: if input row has an additional element after data point, it is used as preference value. If it is absent, similarity median is used as preference value.
User similarity function is invoked using Pig's 'InvokeFor' mechanism, i.e. a proper UDF is not required. The function is defined by `$SIMILARITY_JAR` (specifies Jar file) and `$SIMILARITY_FUN` (specifies class and method name as *class_name.method_name*) parameters.
The method which implements similarity function must have the following signature:
    
    public static double method_name(String p1, String p2)

### Edge loader
Edge loader `load_edges.pig` reads data from `$INPUT` in the form *(i, k, s_ik)* where _i_ and _k_ are data points and *s_ik* is the similarity value between them. Preferences (self-similarities) must be specified as *(i, i, s_ii)*. 

## Iteration
Algorithm iteration is performed by `iteration.pig` script. It consists of the following steps:

1. Compute responsibility values
2. Compute availability values
3. Compute decisions (decide which edges represent the member-exemplar relationship)
4. Count number of decision which have changed compared to the previous iteration

For `iteration.pig`, each edge is represented by a single data row with the following schema:

| Name | Type | Meaning |
| --- | --- | --- |
| *i* | chararray | data point in member role |
| *k* | chararray | data point in exemplar role |
| *s_ik* | double | similarity of _i_ to _k_ |
| *a_ik* | double | availability of _k_ to _i_ |
| *r_ik* | double | responsibility from _i_ to _k_ |
| *ex_prev* | boolean | whether this edge represented a member-exemplar relationship on the previous iteration |
| *ex_new* | boolean | whether this edge represents a member-exemplar relationship on this iteration |

This format is expected for the output of the loader script. Before the first iteration, *ex_new* must be initialized to `false` for all rows.
The damping factor value is determined by `$DAMP` parameter.

Limitations on graph structure:

* Each data point must have an edge to self with known self-similarity
* Each data point must have at least one outgoing edge with known similarity to another data point
* Each data point must have at least one incoming edge with known similarity from another data point

Currently each iteration is executed in four MapReduce jobs (one for each step).

## Output
Results are stored to location denoted by `$OUTPUT` as _(exemplar, member)_ records where _exemplar_ is cluster exemplar and _member_ is member data point belonging to cluster identified by _exemplar_. There is one such record for every _member_ data point.

Output results are sorted first by _exemplar_ and then by _member_. Note that as the data points are stored as _chararray_, sorting order is lexicographical rather than e.g. numerical if data points are represented by numbers.

## Data record format
By default, data records for input, edges, output, and intermediate results are stored as tab-separated values. This can be overrided by altering `$STORAGE` parameter.

## Expected environment variables
**ap-pig** requires the following environment variables to be set:

| Name | Value |
| --- | --- |
| JAVA_HOME | Points to JDK home directory |
| HADOOP_HOME | Points to Hadoop home directory |
| PIG_HOME | Points to Pig home directory |
| PIG_CLASSPATH | Points to Hadoop site configuration directory, e.g. `$HADOOP_HOME/etc/hadoop` |
| PIG_JAR | Path to Pig jar file, e.g. `$PIG_HOME/$PIG_HOME/pig-0.11.0-cdh4.3.0.jar`. Required for running PigUnit tests. |
| PIGGYBANK_JAR | Path to piggybank jar, e.g. `$PIG_HOME/contrib/piggybank/java/piggybank.jar` |
| PIGUNIT_JAR | Path to pigunit jar, e.g. `$PIG_HOME/pigunit.jar`. Required for running PigUnit tests. This file is not included in the Pig distribution and has to be built from sources. |
| JUNIT_LIB | Path to JUnit. Required for running PigUnit tests. |

## Parameters
Algorithm parameters are specified in `alg.params` file. This file is automatically passed to Pig in `exec_workflow.sh` script. Also, each parameter can be overriden by specifying `-p name=value` or `-param name=value` when invoking `exec_workflow.sh`. 

| Parameter name | Parameter description |
| --- | --- |
| STORAGE | Storage function used by Pig to load and store data |
| SIMILARITY_JAR | Path to Jar file containing similarity function |
| SIMILARITY_FUN | Similarity function in *class_name.method_name* format where class name should be fully qualified |
| DAMP | Damping factor value |
| MAX_ITER | Maximum number of iterations before algorithm stops |
| CONV_ITER | Number of consecutive stable iterations (decisions unchanged) until algorithm stops |
| INPUT | Input data location |
| OUTPUT | Result output location |

## Running ap-pig
`exec_workflow.sh` script is used to execute the algorithm.
Invocation format is as follows:
 
    exec_workflow.sh <pig_params> -- <loader_script_name>
 
 Pig parameters are placed before the double-hyphen, workflow parameters (loader script name) - after the double-hyphen.
 
 Example:

    exec_workflow.sh -x local -- load_vertices.pig

This will execute the algorithm in Pig local mode (Pig mapreduce mode is used by default) using vertex loader and algorithm parameters specified in `alg.params`.

## Explaining scripts
Every Pig script in **ap-pig** can be explained (using Pig 'explain' feature) by ivoking the corresponding `explain_<script_name>.sh` shell script.

## PigUnit tests
PigUnit tests are located in `java/test`. To run PigUnit tests, execute `exec_unittests.sh` which will build and execute the tests.

## Validation
Toy problem from [Affinity Propagation website][2] is used to validate that **ap-pig** produces the same results as AP authors' original software. Problem input and expected output are provided in `validate` directory. To perform validation, execute `validate.sh` script which will run the workflow in local mode and compare actual result with expected solution. It should print `Validation successfull` if results are the same.

## Feedback
Any kind of feedback regarding **ap-pig** is welcome. While I created **ap-pig** mainly for the purpose of learning Pig and haven't yet tried to process real data on a real cluster, it'd be awesome to hear feedback from someone doing so.

Please contact me via email: Andrey.Logvinov.81@gmail.com

[1]: http://www.psi.toronto.edu/affinitypropagation/FreyDueckScience07.pdf

[2]: http://www.psi.toronto.edu/index.php?q=affinity%20propagation