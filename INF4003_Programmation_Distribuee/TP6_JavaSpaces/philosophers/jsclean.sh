#!/bin/bash
# F. Raimbault 
# 13/02/2012
#
 
# executable Java
JAVA=java
# emplacement des classes de JAVASPACES
JAVASPACE_HOME=/home/forum/m2info/INF4003/javaspaces
JAVASPACE_CLASSPATH=$JAVASPACE_HOME/lib/*
# fichier policy.all
POLICY_FILE=$PWD/policy.all
# jar contenant le programme a executer
JAR_FILE=$PWD/build/philosophers_V1.jar
# classe de la tache contenant le programme principal 
MAIN_CLASS=allain1.philosophers_V1.MyEntry

echo "$JAVA -Djava.security.policy=$POLICY_FILE -cp $JAVASPACE_CLASSPATH:$JAR_FILE $MAIN_CLASS $1"
$JAVA -Djava.security.policy=$POLICY_FILE -cp $JAVASPACE_CLASSPATH:$JAR_FILE $MAIN_CLASS $1



