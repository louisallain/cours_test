- Exécuter le server CoAP gérant les capteurs de lumière et de présence :
   - java -cp ./bin/lib/* allain.coap.Server
   - Cette commande de plus permet de simuler l'action des capteurs .

- Exéctuer le serveur CoAP gérant la porte d'entrée :
   - java -cp ./bin/lib/* allain.coap.DoorServer
   - Cette commande lance également un observateur sur les ressources des capteurs de présence du serveur précédent
     afin de contrôler automatiquement la porte d'entrée si rien n'est détecté par les capteurs de présence.
    
- Exéctuer le client CoAP :
   - java -cp ./bin/lib/* allain.coap.client
   - Cette commande permet d'obtenir des informations depuis tous les capteurs des deux serveurs CoAP. 
     Elle permet également de contrôler l'ouverture de la porte d'entrée "manuellement" ainsi que les lumières de chaque pièce.