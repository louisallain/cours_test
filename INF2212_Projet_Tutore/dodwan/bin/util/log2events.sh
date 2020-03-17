#!/bin/sh 

version=3.2

classpath="libs/dodwan-${version}.jar"

java    -cp $classpath \
     	casa.dodwan.util.Log2Events $*
