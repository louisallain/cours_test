#!/bin/sh
# $1=server host 
ssh $1 java -cp $PWD/sensor.jar:. -Djava.security.policy=$HOME/policy.all sensor.TempServer &
sleep 2s
java -cp $PWD/sensor.jar:. -Djava.security.policy=$HOME/policy.all sensor.TempListener $1 
