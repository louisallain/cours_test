Les sources se trouvent dans ./src

Lancement des exécutables :
    - Déployer les EJB ["./bin/convertisseur-ejb.jar", "stock-ejb.jar"] dans "$JBOSS_HOME/server/defaults/deploy"
    - Exécuter le client test de "convertisseur-ejb.jar" :
        - Commande : "cd bin/build && java client.ConvertisseurClient"
    - Exécuter le client test de "stock-ejb.jar" :
        - Commande : "cd bin/build && java client.StockClient"