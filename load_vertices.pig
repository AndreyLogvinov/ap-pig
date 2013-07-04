%DEFAULT PIGUNIT_SORT ''

-- Jar file containing similarity UDF
REGISTER $SIMILARITY_JAR;

-- Similarity class and function (i.e. pakage.Class.method)
DEFINE similarity InvokeForDouble('$SIMILARITY_FUN', 'String String'); 

-- Load source data (element, preference)
source = LOAD '$INPUT' USING $STORAGE AS (element: chararray, preference: double);

-- Generate cross product (future edges) and calculate similarities in non-self pairs
source_left = FOREACH source GENERATE  
    element AS i,
    preference AS preference;

source_right = FOREACH source GENERATE  
    element AS k;

source_cross = CROSS source_left, source_right;

source_cross_sim = FOREACH source_cross GENERATE 
    i AS i,
    k AS k,
    preference as preference,
    ((i != k) ? similarity(i, k) : null) AS s_ik;

-- Only emit self-edges and edges with similarity != -infinity
source_cross_sim_f = FILTER source_cross_sim BY i == k OR (chararray) s_ik != '-Infinity';

-- Calculate similarity median
sim_range = FOREACH (GROUP source_cross_sim_f ALL) GENERATE 
    (MAX(source_cross_sim_f.s_ik) + MIN(source_cross_sim_f.s_ik)) / 2 AS sim_median;

-- Generate edges. Where preference ("self-similarity") not set, use median value as self-similarity.
edges = FOREACH source_cross_sim_f GENERATE 
    i AS i, 
    k AS k, 
    (i != k ? s_ik : (preference IS NOT null ? preference : 
        (sim_range.sim_median IS NOT null ? sim_range.sim_median : 0))) AS s_ik,
    0 AS a_ik, 
    0 AS r_ik,
    false AS ex_prev,
    false AS ex_new;

-- Sort edges if we are running a PigUnit test
$PIGUNIT_SORT

-- Store edges
STORE edges INTO '$EDGES_OUT' USING $STORAGE;
