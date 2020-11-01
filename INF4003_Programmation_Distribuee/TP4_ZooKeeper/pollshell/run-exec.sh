#!/bin/bash
APP_JAR=$HOME/Documents/INF4003_Programmation_Distribuee/TP4_ZooKeeper/pollshell/pollshell.jar
MAIN_CLASS=pollshell.Exec
# prefixe du nom des machines sur le cluster
NODE=np

set -x

#dsh -r ssh -M -f /share/cluster/cpedago/etc/machines.list killall java

SIZE=9
# lancement de MAIN_CLASS sur tous les noeuds du cluster
for ((pid=0 ; $pid < $SIZE ; pid++))
do
  ssh $NODE$pid killall java ;
  ssh $NODE$pid java -cp $CLASSPATH:$APP_JAR $MAIN_CLASS $SIZE ip &
done



  
