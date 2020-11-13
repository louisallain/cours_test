#!/bin/sh
# run-dshell <command> <nodes number> <jar HDFS path>

yarn jar dshell.jar dshell.Client $1 $2 /user/e1602246/apps/dshell.jar
