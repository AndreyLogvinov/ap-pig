%DEFAULT PIGUNIT_SORT ''

-- Load source data (element, preference)
source = LOAD '$INPUT' USING $STORAGE AS (i: chararray, k: chararray, s_ik: double);

-- Append missing fields
edges = FOREACH source GENERATE *, 0 AS a_ik, 0 AS r_ik, false AS ex_prev, false AS ex_new;

-- Sort edges if we are running a PigUnit test
$PIGUNIT_SORT

-- Store edges
STORE edges INTO '$EDGES_OUT' USING $STORAGE;
