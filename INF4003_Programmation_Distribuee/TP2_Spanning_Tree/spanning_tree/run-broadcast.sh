#!/bin/bash

CSP_JAR=$HOME/Documents/INF4003_Programmation_Distribuee/TP2_Spanning_Tree/CSP_Perso.jar
APP_JAR=$HOME/Documents/INF4003_Programmation_Distribuee/TP2_Spanning_Tree/build/spanning_tree.jar
MAIN_CLASS=spanning_tree.Broadcast
CONF_FILE=$HOME/Documents/INF4003_Programmation_Distribuee/TP2_Spanning_Tree/spanning_tree/conf-broadcast.txt
# prefixe du nom des machines sur le cluster
NODE=np

set -x

#dsh -r ssh -M -f /share/cluster/cpedago/etc/machines.list killall java

SIZE=9
# lancement de MAIN_CLASS sur tous les noeuds du cluster
for ((pid=0 ; $SIZE - $pid ; pid++))
do
  ssh $NODE$pid killall java ;
  ssh $NODE$pid java -cp $CSP_JAR:$APP_JAR $MAIN_CLASS $CONF_FILE $pid 50000 &
done



  
