#!/bin/bash
$PIG_HOME/bin/pig -e "explain -param_file alg.params -param PIGGYBANK_JAR=$PIGGYBANK_JAR -param EDGES_IN=x -param EDGES_OUT=x -param CONVERGENCE_OUT=x -script iteration.pig"
