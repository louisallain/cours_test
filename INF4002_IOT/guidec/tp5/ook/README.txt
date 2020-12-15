- Le code du récepteur se trouve dans le fichier ook_rcv.cpp
- Le code de l'émetteur se trouve dans le fichier ook_snd.cpp

- Le transmetteur se charge de collecter les données auprès du capteur DHT11 (température et humidité, car 
  pas de capteur de luminosité). Créer une structure de données comportant également l'identifiant du
  transmetteur et l'identifiant du récepteur. Et envoie ces données sous forme d'un tableau d'octets.

- Le récepteur récupère les données transmises et recréer la structure correspondante au tableau d'octets reçu. 
  Et affiche les résultats sur l'écran OLED. A noter qu'il n'affiche les résultats seulement si l'identifiant
  du récepteur décodé depuis le tableau d'octets reçu correspond à son identifiant ou à celui du "broadcast"
  (255).