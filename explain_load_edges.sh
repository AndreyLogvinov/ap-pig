#!/bin/bash
$PIG_HOME/bin/pig -e "explain -param_file alg.params -param PIGGYBANK_JAR=$PIGGYBANK_JAR -param EDGES_OUT=x -script load_edges.pig"
