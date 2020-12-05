pio.pin.setdir(pio.OUTPUT, pio.GPIO2) -- rouge
pio.pin.setdir(pio.OUTPUT, pio.GPIO4) -- vert

currentColor = "v" -- couleur du feu ("v" pour vert et "r" pour rouge)
nb_feux = 0 -- nombre de feu que l'on connait
nb_feux_pret = 0 -- nombre de feu qui ont indiques qu'ils etaient prets a changer de couleur
feux_prets = {} -- tableaux des feux prêts pour ce cycle

k = 0 -- sert à compter le nombre de cycles avant la détection d'une panne

TOPIC_COMPTEUR_FEUX = "/e1602246/compteurFeux"
TOPIC_NOMBRE_FEUX = "/e1602246/nombreFeux"
TOPIC_FEUX_PRET = "/e1602246/feuxPret"

-- Crer un client mqtt au broker
client = mqtt.client("e1602246-1", "mini.arsaniit.com", 1883, false)
clientSub = mqtt.client("e1602246-2", "mini.arsaniit.com", 1883, false)

-- booléen indiquant si la boucle doit tourner ou pas
running = false

-- uuid du feu
uuid = "feu1"

-- Fonction activant la wifi
initSTA = function() 
        net.wf.setup(net.wf.mode.STA, "Bbox-80CDC60A", "7y2uDWWK92utNXdCpq") net.wf.start() 
end

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
                if(client:connected() == false) then client:connect("", "") end
                -- publie le nombre de feux connu pour la mise à jour des feux rentrants
                client:publish(TOPIC_NOMBRE_FEUX, nb_feux, mqtt.QOS0) 
                tmr.delayms(2000)
                if(client:connected() == false) then client:connect("", "") end
                -- publie le nombre de feux connu pour la mise à jour des feux rentrants
                client:publish(TOPIC_NOMBRE_FEUX, nb_feux, mqtt.QOS0) 
                -- publie pour indiquer qu'il est prêt à changer de couleur
                client:publish(TOPIC_FEUX_PRET, uuid, mqtt.QOS0) 
                tmr.delayms(2000)
                
                -- pause lorsque tous les feux sont prêts
                -- débloqué dans la fonction onFeuxPret
                while(nb_feux_pret == nb_feux) do 
                        tmr.delayms(10)
                        if(client:connected() == false) then client:connect("", "") end
                        -- re-publie "prêt" dans le cas où un feu sort du système
                        client:publish(TOPIC_FEUX_PRET, uuid, mqtt.QOS0)
                end
        end
        client:disconnect()
end 

-- arrête le feu (pour maintenance par exemple)
stopFeu = function()
    
        -- Arrête la boucle
        running = false
    
        -- Extinction du feu
        pio.pin.setlow(pio.GPIO2)
        pio.pin.setlow(pio.GPIO4)
        
        -- Déclare que le feu n'existe plus en publiant dans le sujet TOPIC_COMPTEUR_FEUX
        if(client:connected() == false) then client:connect("", "") end
        client:publish(TOPIC_COMPTEUR_FEUX, -1, mqtt.QOS0) -- ie. -1 feu
end

initFeu = function(color)
    
        -- Wifi
        initSTA()

        -- Initialise la couleur du feu au depart
        currentColor = color
        pio.pin.setlow(pio.GPIO2)
        pio.pin.setlow(pio.GPIO4)
        if(currentColor == "v") then 
                pio.pin.sethigh(pio.GPIO4) 
        else 
                pio.pin.sethigh(pio.GPIO2) 
        end

        -- Initialise les variables globales
        nb_feux = 0
        nb_feux_pret = 0

        -- uuid des feux pret en cours
        feux_prets = {}

        -- Abonnements
        clientSub:connect("", "")
        clientSub:subscribe(TOPIC_COMPTEUR_FEUX, mqtt.QOS0, onCompteurFeux) 
        clientSub:subscribe(TOPIC_NOMBRE_FEUX, mqtt.QOS0, onNombreFeux)
        clientSub:subscribe(TOPIC_FEUX_PRET, mqtt.QOS0, onFeuxPret)
        
        -- Déclare son existance en publiant dans le sujet TOPIC_COMPTEUR_FEUX
        client:connect("", "")
        client:publish(TOPIC_COMPTEUR_FEUX, 1, mqtt.QOS0) -- ie. +1 feu
        client:disconnect()
        
        -- Commence la boucle
        running = true
        th1 = thread.start(boucle)
end

-- Méthode exécutée lors de la réception d'un message depuis 
-- le sujet TOPIC_COMPTEUR_FEUX
onCompteurFeux = function(length, msg)
        -- modifie le nombre de feux connus
        nb_feux = nb_feux + msg
        -- publie sur le sujet TOPIC_NOMBRE_FEUX le nombre de feux connus
        clientSub:publish(TOPIC_NOMBRE_FEUX, nb_feux, mqtt.QOS0)
    end

-- Méthode exécutée lors de la réception d'un message depuis
--  le sujet TOPIC_NOMBRE_FEUX
onNombreFeux = function(length, msg)
        if(nb_feux < msg) then nb_feux = msg end
end

-- Méthode exécutée lors de la réception d'un message depuis le sujet TOPIC_FEUX_PRET
onFeuxPret = function(length, msg)

        if(uuid == msg) then
                 -- Gestion des pannes :
                k = k + 1
                if(k == 2) then 
                        k = 0
                        -- décrémente le nombre de feux car on a été bloqué pendant 
                        -- 2 cycles donc au moins un feu est en panne
                        nb_feux = nb_feux - 1 
                        clientSub:publish(TOPIC_NOMBRE_FEUX, nb_feux, mqtt.QOS0)
                        
                end
        end
       
        
        -- vérifie si c'est un nouveau qui est pret avant 
        -- d'incrémenter le nombre de feux prêts
        if(has_value(feux_prets, msg) == false) then
                nb_feux_pret = nb_feux_pret + 1
                table.insert(feux_prets, msg)
        end
        
        if(nb_feux_pret == nb_feux) then 
                k = 0
                switchColor()
                -- Réinitialisation
                feux_prets = {}
                nb_feux_pret = 0
        end
end