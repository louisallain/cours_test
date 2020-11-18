javac --release 8 -d ./build ./src/csp_V3/*.java && 
cd build &&
jar cvf ../csp_V3.jar csp_V3 org &&
cd .. &&
hadoop fs -rm /user/e1602246/apps/csp_V3/*.jar  && 
hadoop fs -copyFromLocal ./csp_V3.jar /user/e1602246/apps/csp_V3/ &&
hadoop fs -rm /user/e1602246/apps/csp_V3/net_conf.txt &&
hadoop fs -copyFromLocal ./net_conf.txt /user/e1602246/apps/csp_V3/ &&
yarn jar ./csp_V3.jar csp_V3.YARNClient /user/e1602246/apps/csp_V3/net_conf.txt /user/e1602246/apps/csp_V3/csp_V3.jar