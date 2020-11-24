pio.pin.setdir(pio.OUTPUT, pio.GPIO2) -- rouge
pio.pin.setdir(pio.OUTPUT, pio.GPIO4) -- vert

currentColor = "v" -- couleur du feu ("v" pour vert et "r" pour rouge)
nb_feux = 0 -- nombre de feu que l'on connait
nb_feux_pret = 0 -- nombre de feu qui ont indiques qu'ils etaient prets a changer de couleur

TOPIC_COMPTEUR_FEUX = "/e1602246/compteurFeux"
TOPIC_NOMBRE_FEUX = "/e1602246/nombreFeux"
TOPIC_FEUX_PRET = "/e1602246/feuxPret"

thread_abonnement = nil -- thread où l'on va exécuter les abonnements

-- Fonction activant la wifi
initSTA = function() net.wf.setup(net.wf.mode.STA, "Bbox-80CDC60A", "7y2uDWWK92utNXdCpq") net.wf.start() end

-- change de couleur, si rouge alors vert et vice versa
switchColor = function()
        if(currentColor == "v") then
                currentColor = "r"
                pio.pin.setlow(pio.GPIO4)
                pio.pin.sethigh(pio.GPIO2)
        else
                currentColor = "v"
                pio.pin.setlow(pio.GPIO2)
                pio.pin.sethigh(pio.GPIO4)
        end
end

-- arrête le feu (pour maintenance par exemple)
stopFeu = function()
    pio.pin.setlow(pio.GPIO2)
    pio.pin.setlow(pio.GPIO4)
end

initFeu = function(color)
    
        -- Wifi
        initSTA()

        -- Initialise la couleur du feu au depart
        currentColor = color
        pio.pin.setlow(pio.GPIO2)
        pio.pin.setlow(pio.GPIO4)
        if(currentColor == "v") then pio.pin.sethigh(pio.GPIO4) else pio.pin.sethigh(pio.GPIO2) end

        -- Initialise les variables globales
        nb_feux = 0
        nb_feux_pret = 0

        -- Crer un client mqtt au broker de M Menier
        client = mqtt.client("e1602246-1", "mini.arsaniit.com", 1883, false)
        clientSub = mqtt.client("e1602246-2", "mini.arsaniit.com", 1883, false)

        -- Méthode exécutée lors de la réception d'un message depuis le sujet TOPIC_COMPTEUR_FEUX
        onCompteurFeux = function(length, msg)
            nb_feux = nb_feux + msg -- modifie le nombre de feux que l'on connait (-1 ou +1)
            clientSub:publish(TOPIC_NOMBRE_FEUX, nb_feux, mqtt.QOS0) -- publie sur le sujet TOPIC_NOMBRE_FEUX le nombre de feux que l'on connait
        end

        -- Méthode exécutée lors de la réception d'un message depuis le sujet TOPIC_NOMBRE_FEUX
        onNombreFeux = function(length, msg)
            if(nb_feux < msg) then nb_feux = msg end
            print("onNombreFeux nb_feux = "..nb_feux)
        end

        -- S'abonne aux files "/e1602246/compteurFeux", "/e1602246/nombreFeux" et "/e1602246/feuxPret".
        clientSub:connect("", "")
        clientSub:subscribe(TOPIC_COMPTEUR_FEUX, mqtt.QOS0, onCompteurFeux) -- callback = onCompteurFeux
        clientSub:subscribe(TOPIC_NOMBRE_FEUX, mqtt.QOS0, onNombreFeux) -- callback = onCompteurFeux
        
        -- Déclare son existance en publiant dans le sujet TOPIC_COMPTEUR_FEUX
        client:connect("", "")
        client:publish(TOPIC_COMPTEUR_FEUX, 1, mqtt.QOS0) -- ie. +1 feu
        client:disconnect()
        
end