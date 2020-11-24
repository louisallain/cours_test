INF4002-IOT . TP1-RTC . Louis ALLAIN

1. Consulter les registres du DS3231 et afficher les valeurs de ces registres en format binaire :
    . Il y a 19 registres dans le DS3231 faisant chacun 8 bits.
    . Se positionner au début de la mémoire puis afficher un octet par un octet jusqu'à 19 octets.
    . Sortie du terminal série après exécution de la commande '@' :
    > @
    00100000
    00110001
    00100001
    00000011
    00100100
    00010001
    11000000
    01100100
    00110111
    00010111
    00000101
    00001110
    00000001
    00101111
    00011100
    10001001
    00000000
    00010101
    01000000


2. Récupérer l'heure depuis les registres 0x00 à 0x06 du DS3231 :
    . Les 7 premiers registres du DS3231 concernent la date.
    . Se positionner au début de la mémoire (0x00) puis "parser" les 7 octets pour obtenier la date.
    . Pour les secondes : récupère l'octet et lui applique le masque 0x7F car les dizaines de secondes
    sont encodées sur les bits 6, 5 et 4 et les secondes unitaires sont encodées sur les bits 3, 2, 1 et 0.
    . Pour les minutes : récupère l'octet et lui applique le masque 0x7F car même chose que pour les secondes.
    . Pour les heures : après vérification des registres le bit 6 est à 0, c'est donc le mode 24h qui est actif.
    Donc en appliquant le masque 0x3F, on récupère l'heure entre 0 et 23.
    . Pour le jour : encodés sur les bits 2, 1 et 0 appliquer le masque 0x07 donne le jour de la semaine entre 1 et 7 (lundi = 1).
    . Pour le jour du mois : encodés sur les bits 5, 4, 3, 2, 1 et 0 donc le masque 0x3F nous donne le jour du mois entre 1 et 31.
    . Pour le mois : encodés sur les bits 4, 3, 2, 1 et 0 donc le masque 0x1F nous donne le jour du mois entre 1 et 31.
    . Pour l'année : encodés sur les de 7 à 0 donc le masque 0xFF nous donne l'année entre 00 et 99.
    . Sortie du terminal série après exécution de la commande '?' (après modification de l'heure par la commande '!') : 
    > ?
    Current RTC time is Tue Nov 24 21:32:39 2120

3. Saisir l'heure via la console l'heure actuelle (exprimée en nombre de secondes depuis le 01/01/1970) et fixer en conséquence les valeurs des registres du DS3231 :
    . Se positionner (0x00) puis écrire les 7 prochains octets en fonction de la date donnée.
    . Dans la fonction setTime la date rentrée est directement modifié en fonction l'heure locale (GMT+1 pour l'heure d'hiver).
    . . Sortie du terminal série après exécution de la commande '!' :
    > !1606253339
    Setting RTC to Tue Nov 24 21:28:59 2020