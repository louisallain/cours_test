#!/bin/bash

JAR_PATH=$HOME/INF4003/ZK
WATCHER_NODE=np3
UPDATER_NODE=np9

ssh $WATCHER_NODE java -cp $ZK_HOME/lib/*:$ZK_HOME/zookeeper-3.4.12.jar:$JAR_PATH/watcher_V0.jar:$JAR_PATH watcher_V0.DataWatcher &
ssh $UPDATER_NODE java -cp $ZK_HOME/lib/*:$ZK_HOME/zookeeper-3.4.12.jar:$JAR_PATH/watcher_V0.jar:$JAR_PATH/. watcher_V0.DataUpdater &

