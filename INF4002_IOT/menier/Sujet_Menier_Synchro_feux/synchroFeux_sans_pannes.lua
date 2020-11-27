pio.pin.setdir(pio.OUTPUT, pio.GPIO2) -- rouge
pio.pin.setdir(pio.OUTPUT, pio.GPIO4) -- vert

currentColor = "v" -- couleur du feu ("v" pour vert et "r" pour rouge)
nb_feux = 0 -- nombre de feu que l'on connait
nb_feux_pret = 0 -- nombre de feu qui ont indiques qu'ils etaient prets a changer de couleur

TOPIC_COMPTEUR_FEUX = "/e1602246/compteurFeux"
TOPIC_NOMBRE_FEUX = "/e1602246/nombreFeux"
TOPIC_FEUX_PRET = "/e1602246/feuxPret"

-- Crer un client mqtt au broker de M Menier
client = mqtt.client("e1602246-1", "mini.arsaniit.com", 1883, false)
clientSub = mqtt.client("e1602246-2", "mini.arsaniit.com", 1883, false)

-- booléen indiquant si la boucle doit tourner ou pas
running = false

-- uuid du feu
uuid = "feu1"

-- Fonction activant la wifi
initSTA = function() net.wf.setup(net.wf.mode.STA, "Bbox-80CDC60A", "7y2uDWWK92utNXdCpq") net.wf.start() end

-- fonction permettant de savoir si un élément fait parti d'un tableau
local function has_value (tab, val)
        for index, value in ipairs(tab) do
            if value == val then
                return true
            end
        end
    
        return false
end

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

-- Fonction exécutée périodiquement
boucle = function()
        client:connect("", "")
        while(running == true) do
                client:publish(TOPIC_NOMBRE_FEUX, nb_feux, mqtt.QOS0) -- publie périodiquement le nombre de feu qu'il connait afin de permettre aux feux rentrants de connaître le nombre de feu
                tmr.delayms(2000)
                client:publish(TOPIC_NOMBRE_FEUX, nb_feux, mqtt.QOS0) -- publie périodiquement le nombre de feu qu'il connait afin de permettre aux feux rentrants de connaître le nombre de feu
                client:publish(TOPIC_FEUX_PRET, uuid, mqtt.QOS0) -- publie pour indiquer qu'il est prêt à changer de couleur
                tmr.delayms(2000)
                while(nb_feux_pret == nb_feux) do 
                        tmr.delayms(10)
                        client:publish(TOPIC_FEUX_PRET, uuid, mqtt.QOS0) -- re-publique "prêt" en cas de mauvais envoi
                end -- met en pause la boucle tant que tous les feux ne sont pas arrivés au même point
        end
        client:disconnect()
end 

-- arrête le feu (pour maintenance par exemple)
stopFeu = function()
        pio.pin.setlow(pio.GPIO2)
        pio.pin.setlow(pio.GPIO4)
        
        -- Déclare que le feu n'existe plus en publiant dans le sujet TOPIC_COMPTEUR_FEUX
        client:connect("", "")
        client:publish(TOPIC_COMPTEUR_FEUX, -1, mqtt.QOS0) -- ie. -1 feu
        client:disconnect()

        -- Arrête la boucle
        running = false
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

        -- uuid des feux pret en cours
        feux_prets = {}

        -- Méthode exécutée lors de la réception d'un message depuis le sujet TOPIC_COMPTEUR_FEUX
        onCompteurFeux = function(length, msg)
            -- print("onCompteurFeux")
            nb_feux = nb_feux + msg -- modifie le nombre de feux que l'on connait (-1 ou +1)
            clientSub:publish(TOPIC_NOMBRE_FEUX, nb_feux, mqtt.QOS0) -- publie sur le sujet TOPIC_NOMBRE_FEUX le nombre de feux que l'on connait
        end

        -- Méthode exécutée lors de la réception d'un message depuis le sujet TOPIC_NOMBRE_FEUX
        onNombreFeux = function(length, msg)
            -- print("Nouveau nombre de feux : feu reçu : "..msg.." nbre de feux connus "..nb_feux)
            if(nb_feux < msg) then nb_feux = msg end
        end

        -- Méthode exécutée lors de la réception d'un message depuis le sujet TOPIC_FEUX_PRET
        onFeuxPret = function(length, msg)
                
                -- vérifie si c'est un nouveau qui est pret avant d'incrémenter le nombre de feux prêts
                if(has_value(feux_prets, msg) == false) then
                        nb_feux_pret = nb_feux_pret + 1
                        table.insert(feux_prets, msg)
                end
                
                if(nb_feux_pret == nb_feux) then 
                        switchColor()
                        -- Réinitialise le tableau des feux prêts
                        feux_prets = {}
                        nb_feux_pret = 0
                end
        end

        -- S'abonne aux files "/e1602246/compteurFeux", "/e1602246/nombreFeux" et "/e1602246/feuxPret".
        clientSub:connect("", "")
        clientSub:subscribe(TOPIC_COMPTEUR_FEUX, mqtt.QOS0, onCompteurFeux) -- callback = onCompteurFeux
        clientSub:subscribe(TOPIC_NOMBRE_FEUX, mqtt.QOS0, onNombreFeux) -- callback = onCompteurFeux
        clientSub:subscribe(TOPIC_FEUX_PRET, mqtt.QOS0, onFeuxPret) -- callback = onFeuxPret
        
        -- Déclare son existance en publiant dans le sujet TOPIC_COMPTEUR_FEUX
        client:connect("", "")
        client:publish(TOPIC_COMPTEUR_FEUX, 1, mqtt.QOS0) -- ie. +1 feu
        client:disconnect()
        
        -- Commence la boucle
        running = true
        th1 = thread.start(boucle)
end