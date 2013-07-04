%DEFAULT PIGUNIT_SORT ''

REGISTER $PIGGYBANK_JAR;

DEFINE SMAX org.apache.pig.piggybank.evaluation.math.MAX();
DEFINE SMIN org.apache.pig.piggybank.evaluation.math.MIN();

edges = LOAD '$EDGES_IN' USING $STORAGE AS (
    i: chararray, 
    k: chararray, 
    s_ik: double, 
    a_ik: double, 
    r_ik: double,
    ex_prev: boolean,
    ex_new: boolean);

-- Calculate responsibilities
edges_tmp_r = FOREACH (GROUP edges BY i) {
    edges_aps = FOREACH edges GENERATE k, a_ik + s_ik;
    -- Here we generate two maximums with their corresponding k values so that we can later exclude k'=k case
    edges_aps_max12 = TOP(2, 1, edges_aps);
    edges_aps_max1 = TOP(1, 1, edges_aps_max12);
    edges_aps_max2 = DIFF(edges_aps_max12, edges_aps_max1);

    GENERATE FLATTEN(edges), 
        FLATTEN(edges_aps_max1) as (kmax1: chararray, aps_ik_max1: double),
        FLATTEN(edges_aps_max2) as (kmax2: chararray, aps_ik_max2: double);
}

edges_r = FOREACH edges_tmp_r GENERATE 
    i AS i, 
    k AS k, 
    s_ik AS s_ik, 
    a_ik AS a_ik,
    ($DAMP * r_ik + (1 - $DAMP) * (s_ik - (k == kmax1 ? aps_ik_max2 : aps_ik_max1))) as r_ik,
    ex_prev AS ex_prev, 
    ex_new AS ex_new;

-- Calculate availabilities
edges_tmp_a = FOREACH (GROUP edges_r BY k) {
    edges_kk = FILTER edges_r BY i == k;
    edges_ik = FILTER edges_r BY i != k;
    edges_ik_add = FOREACH edges_ik GENERATE SMAX(0, r_ik);
    GENERATE FLATTEN(edges_r), FLATTEN(edges_kk.r_ik) as r_kk, SUM(edges_ik_add) as sum_r_ik;
};

edges_a = FOREACH edges_tmp_a GENERATE 
    i AS i, 
    k AS k,
    s_ik AS s_ik, 
    ($DAMP * a_ik + 
        (1 - $DAMP) *
            (i == k ? sum_r_ik : SMIN(0, r_kk + sum_r_ik - SMAX(0, r_ik)))) AS a_ik,
    r_ik AS r_ik, 
    ex_prev AS ex_prev, 
    ex_new AS ex_new;

-- Calculate exemplar decision
edges_tmp_d = FOREACH (GROUP edges_a BY i) {
    edges_apr = FOREACH edges_a GENERATE k AS kmax, a_ik + r_ik AS apr_ik_max;
    edges_apr_max = TOP(1, 1, edges_apr);
    GENERATE FLATTEN(edges_a), FLATTEN(edges_apr_max);
}

edges_d = FOREACH edges_tmp_d GENERATE 
    i AS i, 
    k AS k, 
    s_ik AS s_ik, 
    a_ik AS a_ik, 
    r_ik AS r_ik, 
    ex_new AS ex_prev, 
    (k == kmax ? true : false) AS ex_new;

-- Sort edges if we are running a PigUnit test
$PIGUNIT_SORT

-- Calculate convergence
edges_diff = FOREACH edges_d GENERATE (ex_prev == ex_new ? 0 : 1);
convergence = FOREACH (GROUP edges_diff ALL) GENERATE $MAX_ITER, $CONV_ITER, SUM(edges_diff);

STORE edges_d INTO '$EDGES_OUT' USING $STORAGE;

STORE convergence INTO '$CONVERGENCE_OUT' USING $STORAGE;