#!/bin/bash
# F. Raimbault 
# 13/02/2012
#
# script de lancement d'un programme JAVASPACE sur le cluster pedago
# variables JAR_FILE, MAIN_CLASS a adapter
#

set -x

# emplacement des classes de JAVASPACES
JAVASPACE_HOME=/home/forum/m2info/INF4003/javaspaces
JAVASPACE_CLASSPATH=$JAVASPACE_HOME/lib/*
# fichier policy.all
POLICY_FILE=$PWD/policy.all
# prefixe du nom des machines sur le cluster
NODE=np
# jar contenant le programme a executer
JAR_FILE=$PWD//jscourse.jar
# classe de la tache contenant le Producteur
PRODUCER_CLASS=raimbaul.jscourse.Producer
# classe de la tache contenant les Consommateurs
CONSUMER_CLASS=raimbaul.jscourse.Consumer
# nombre de valeurs
NB_VALUE=100

# lancement du producteur
ssh ${NODE}0 -x java -Djava.security.policy=$POLICY_FILE -cp $JAVASPACE_CLASSPATH:$JAR_FILE $PRODUCER_CLASS $NB_VALUE&

# lancement des consommateurs
SIZE=10
# lancement du Consommateur i sur la machine i
for ((nid=0 ; $SIZE - $nid ; nid++)) 
do
ssh $NODE$nid -x java -Djava.security.policy=$POLICY_FILE -cp $JAVASPACE_CLASSPATH:$JAR_FILE $CONSUMER_CLASS $nid &
done
