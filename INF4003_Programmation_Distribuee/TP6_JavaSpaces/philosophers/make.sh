javac -d ./build/ -cp "/home/forum/m2info/INF4003/javaspaces/lib/*:." ./allain1/philosophers_V1/*.java &&
cd build &&
jar cf philosophers_V1.jar allain1/ && cd ..