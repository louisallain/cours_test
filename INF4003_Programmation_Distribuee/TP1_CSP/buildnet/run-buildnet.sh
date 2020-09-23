#!/bin/bash

CSP_JAR=$HOME/Documents/Prog_distri/TP1/csp_V0.jar
APP_JAR=$HOME/Documents/Prog_distri/TP1/ping_pong_V0.jar
MAIN_CLASS=ping_pong_V0.PingPong
CONF_FILE=$HOME/Documents/Prog_distri/TP1/buildnet/conf-buildnet.txt
# prefixe du nom des machines sur le cluster
NODE=np

set -x

SIZE=2
# lancement de MAIN_CLASS sur tous les noeuds du cluster
for ((pid=0 ; $SIZE - $pid ; pid++))
do
  ssh $NODE$pid killall java ;
  ssh $NODE$pid java -cp $CSP_JAR:$APP_JAR $MAIN_CLASS $CONF_FILE $pid 2
done



  
