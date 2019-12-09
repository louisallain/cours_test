#!/bin/sh
ssh $1 java -cp $PWD/echo.jar:. -Djava.security.policy=$HOME/policy.all echo.EchoServer &
sleep 2s
java -cp $PWD/echo.jar:. -Djava.security.policy=$HOME/policy.all echo.EchoClient $1
