edges_in = LOAD '$EDGES_IN' USING $STORAGE AS (
        i: chararray, 
        k: chararray, 
        s_ik: double, 
        a_ik: double, 
        r_ik: double,
        ex_prev: boolean,
        ex_new: boolean);

edges_ex = FILTER edges_in BY ex_new == true;
result = ORDER (FOREACH edges_ex GENERATE  k, i) BY k, i;
    
STORE result INTO '$OUTPUT' USING $STORAGE;