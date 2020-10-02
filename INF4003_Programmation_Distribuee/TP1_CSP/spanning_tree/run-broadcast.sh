#!/bin/bash

CSP_JAR=$HOME/INF4003/CSP/csp_FR.jar
APP_JAR=$HOME/INF4003/CSP/broadcast.jar
MAIN_CLASS=spanning_tree.Broadcast
CONF_FILE=$HOME/INF4003/CSP/conf-broadcast.txt
# prefixe du nom des machines sur le cluster
NODE=np

set -x

#dsh -r ssh -M -f /share/cluster/cpedago/etc/machines.list killall java

SIZE=9
# lancement de MAIN_CLASS sur tous les noeuds du cluster
for ((pid=0 ; $SIZE - $pid ; pid++))
do
  ssh $NODE$pid java -cp $CSP_JAR:$APP_JAR $MAIN_CLASS $CONF_FILE $pid &
done



  
