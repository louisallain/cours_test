#!/bin/bash

JAR_PATH=$HOME/Documents/INF4003_Programmation_Distribuee/TP4_ZooKeeper/watcher_V1/watcher_V1.jar
WATCHER_NODE=np3
UPDATER_NODE=np9

ssh $WATCHER_NODE java -cp $ZK_HOME/lib/*:$ZK_HOME/zookeeper-3.4.12.jar:$JAR_PATH/watcher_V0.jar:$JAR_PATH watcher_V1.DataWatcher &
ssh $UPDATER_NODE java -cp $ZK_HOME/lib/*:$ZK_HOME/zookeeper-3.4.12.jar:$JAR_PATH/watcher_V0.jar:$JAR_PATH watcher_V1.DataUpdater 5 1000 &

WATCHER_NODE=np4
UPDATER_NODE=np8

ssh $WATCHER_NODE java -cp $ZK_HOME/lib/*:$ZK_HOME/zookeeper-3.4.12.jar:$JAR_PATH/watcher_V0.jar:$JAR_PATH watcher_V1.DataWatcher &
ssh $UPDATER_NODE java -cp $ZK_HOME/lib/*:$ZK_HOME/zookeeper-3.4.12.jar:$JAR_PATH/watcher_V0.jar:$JAR_PATH watcher_V1.DataUpdater 5 1000 &

