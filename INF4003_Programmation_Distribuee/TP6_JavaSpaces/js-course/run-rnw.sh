#!/bin/bash

set -x

JAVASPACE_HOME=/home/forum/m2info/INF4003/javaspaces
JAVASPACE_CLASSPATH=$JAVASPACE_HOME/lib/*:$PWD/jscourse.jar
ssh np9 java -Djava.security.policy=$HOME/policy.all -cp $JAVASPACE_CLASSPATH $LOGNAME.jscourse.Reader &
ssh np5 java -Djava.security.policy=$HOME/policy.all -cp $JAVASPACE_CLASSPATH $LOGNAME.jscourse.NotWriter &
