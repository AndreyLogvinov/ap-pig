#!/bin/bash
rm -rf validate/toyproblem.result
./exec_workflow.sh \
    -x local \
    -p MAX_ITER=2000 \
    -p CONV_ITER=10 \
    -p DAMP=0.5 \
    -p INPUT=validate/toyproblem.txt \
    -p OUTPUT=validate/toyproblem.result \
    -- \
    load_edges.pig
cat validate/toyproblem.result/* > validate/result.txt
if diff validate/result.txt validate/toyproblem_solution.txt >/dev/null ; then
  echo Validation successfull
else
  echo Validation failed
fi
