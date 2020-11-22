# Pour compiler le package csp_V3 :
 - Exécuter la commande : javac --release 8 -d ./build ./src/csp_V3/*.java
 - Créer un jar : cd build && jar cvf ../csp_V3.jar csp_V3 org

# Pour lancer BuildNet qui permet de tester le package csp_V3 :

 - Déposer le jar csp_V3.jar dans le système de fichier hdfs (ex: /user/e1602246/apps/csp_V3/csp_V3.jar)
 - Déposer le fichier de configuration du réseau net_conf.txt dans le système de fichier hdfs (ex : /user/e1602246/apps/csp_V3/csp_V3.jar)
 - Exécuter la commande yarn jar ./csp_V3.jar csp_V3.YARNClient /user/e1602246/apps/csp_V3/net_conf.txt /user/e1602246/apps/csp_V3/csp_V3.jar