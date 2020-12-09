javac --release 8 -d ./build ./dtspace/*.java &&
cd ./build &&
jar cvf ../dtspace.jar dtspace org &&
cd .. &&
hadoop fs -rm /user/e1602246/apps/globalspace/*.jar &&
hadoop fs -copyFromLocal ./dtspace.jar /user/e1602246/apps/globalspace/