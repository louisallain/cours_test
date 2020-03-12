// mini serveur web pour le TD Ajax
// G.MÃ©nier UBS

const http = require('http')
const URL = require('url')
const FS = require('fs')
const port = 3000

// faux news feed
const msg = [
    "Tempete de neige sur la Bretagne : l'UBS attaque par des Grizzlys",
    "Vannes sous le soleil : tous en maillot de bain",
    "Chutes de meteorites a prevoir ce soir : sortez avec des parapluies en titane",
    "Le maire de Vannes declare : 'NguZobut'. Notre decryptage en page centrale",
    "Penurie de glace au cassis"
]

// tableaux de donnÃ©es
tempTab = []
tempCourante = 45

pressionTab = []
pressCourante = 1200

// repÃ¨re commun pour les donnÃ©es
heureTab = []
heureCourante = 0

// production des donnÃ©es
function productionDonnees() {
    heureCourante = heureCourante +1

    tempCourante = tempCourante + 1-2*Math.random()*5
    pressCourante = pressCourante + 1-2*Math.random()*50

    pressionTab.push(pressCourante)
    tempTab.push(tempCourante)
    heureTab.push(heureCourante)
}

setInterval(productionDonnees, 1000)

// callback de rÃ©action
const requestHandler = (requete, reponse) => {
    let ulp = URL.parse(requete.url)
    let maintenant = new Date()
    console.log("requete : "+ulp.pathname)
    switch(ulp.pathname) {


        case '/news' : // news feed
            reponse.write(msg[Math.floor(5*Math.random())])
            reponse.end()
            break;

        case '/temp' : // tempÃ©ratures

            reponse.write(JSON.stringify({
                heure: heureTab,
                temp: tempTab,
                quand: maintenant.toLocaleString()}),encoding='utf8')
            reponse.end()
            break;

        case '/pression' : // pressions

            reponse.write(JSON.stringify({
                heure: heureTab,
                pression: pressionTab,
                quand: maintenant.toLocaleString()}),encoding='utf8')
            reponse.end()
            break;


        default:
            let rq = ulp.pathname
            if (rq.length > 1) rq = rq.slice(1); else rq = "index.html"
            FS.readFile(rq,(err,contents) => {
                
                if(!err){
                    reponse.end(contents)
                } else {
                    reponse.writeHead(404, {'Content-Type': 'text/html'});
                    reponse.write("Probleme de lecture d'index.html !!")
                    reponse.end()
                };
            });
            break;
    }

}




const server = http.createServer(requestHandler)

server.listen(port, (err) => {
    if (err) {
        return console.log('Probleme : ', err)
    }

    console.log(`Le mini serveur est en ligne sur le port : ${port}`)
})