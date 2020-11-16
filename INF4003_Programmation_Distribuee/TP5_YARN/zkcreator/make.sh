javac --release 8 -d . *.java && 
jar cvf ./zkcreator.jar zkcreator org && 
hadoop fs -rm /user/e1602246/apps/*.jar  && 
hadoop fs -copyFromLocal ./zkcreator.jar /user/e1602246/apps/ &&
yarn jar ./zkcreator.jar zkcreator.Client zkcreator.PrintNode $1 /user/e1602246/apps/zkcreator.jar