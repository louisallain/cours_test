#!/bin/bash
# F. Raimbault 
# 13/02/2012
# liste des entrees personnelles dans la Javaspace
 
set -x

# emplacement des classes de JAVASPACES
JAVASPACE_HOME=/home/forum/m2info/INF4003/javaspaces
JAVASPACE_CLASSPATH=$JAVASPACE_HOME/lib/*
# fichier policy.all
POLICY_FILE=$PWD/policy.all
# jar contenant le programme a executer
JAR_FILE=$PWD/jscourse.jar
# Classe contenant le main() a executer
MAIN_CLASS=$LOGNAME.jscourse.MyEntry

ssh np9 java -Djava.security.policy=$POLICY_FILE -cp $JAVASPACE_CLASSPATH:$JAR_FILE $MAIN_CLASS list






