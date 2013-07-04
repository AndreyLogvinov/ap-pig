#!/bin/bash

# parameters before -- will be passed as Pig command line params, the rest are workflow script params
PIG_PARAMS=''
WORKFLOW_PARAMS=''
while [[ "$1" != "" && "$1" != "--" ]]; do
    PIG_PARAMS="$PIG_PARAMS $1" 
    shift
done
shift
while [[ "$1" != "" ]]; do
    WORKFLOW_PARAMS="$WORKFLOW_PARAMS $1" 
    shift
done
$PIG_HOME/bin/pig $PIG_PARAMS \
    -propertyFile pig-local.properties \
    -param PIGGYBANK_JAR=$PIGGYBANK_JAR \
    -param_file alg.params \
    workflow.py \
    $WORKFLOW_PARAMS