#!/bin/bash
# F. Raimbault 
# 13/02/2012
#
# script de lancement d'un programme JAVASPACE sur le cluster pedago
# variables JAR_FILE, MAIN_CLASS a adapter
#
 
# executable Java
JAVA=/usr/bin/java

# emplacement des classes de JAVASPACES
JAVASPACE_HOME=/home/forum/m2info/INF4003/javaspaces
JAVASPACE_CLASSPATH=$JAVASPACE_HOME/lib/*

# fichier policy.all
POLICY_FILE=$PWD/policy.all

# prefixe du nom des machines sur le cluster
NODE=np
# jar contenant le programme a executer
JAR_FILE=$PWD/philosophers_V0/build/philosophers.jar
# classe de la tache contenant le programme principal 
MAIN_CLASS=allain1.philosophers_V0.Philosopher

# lancement du Philosophe i sur la machine i+1
for phi in {0..9}
do
let "nid= $phi + 1"
echo "ssh $NODE$nid -x $JAVA -Djava.security.policy=$POLICY_FILE -cp $JAVASPACE_CLASSPATH:$JAR_FILE $MAIN_CLASS $phi 10 &"
ssh $NODE$nid -x $JAVA -Djava.security.policy=$POLICY_FILE -cp $JAVASPACE_CLASSPATH:$JAR_FILE $MAIN_CLASS $phi 10 &
done
