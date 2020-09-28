#!/bin/bash

CSP_JAR=$HOME/CSP/csp_V1.jar
APP_JAR=$HOME/CSP/buildnet.jar
MAIN_CLASS=build_net.BuildNet
CONF_FILE=$HOME/CSP/conf-buildnet.txt
# prefixe du nom des machines sur le cluster
NODE=np

set -x

SIZE=9
# lancement de MAIN_CLASS sur tous les noeuds du cluster
for ((pid=0 ; $SIZE - $pid ; pid++))
do
  ssh $NODE$pid killall java ;
  ssh $NODE$pid java -cp $CSP_JAR:$APP_JAR $MAIN_CLASS $CONF_FILE $pid &
done



  
