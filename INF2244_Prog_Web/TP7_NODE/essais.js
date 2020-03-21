const request = require('request')
const fs = require('fs')
const jsdom = require("jsdom")
const { JSDOM } = jsdom

/**
 * Charge un URL et l'enregistre sous le nom nomFichier.
 * En cas d'erreur, le callback errCallback est invoqué.
 */
chargement = (url, nomFichier, errCallback) => {

    request
    .get(url)
    .on('error', (err) => errCallback(err))
    .on('response', (response) => {
        console.log(response.statusCode)
        console.log(response.headers['content-type'])
    })
    .pipe(fs.createWriteStream(nomFichier))
}

/**
 * Affiche les noms des images présentes depuis un URL.
 */
recupereImages = (url, prefixe, suffixe, errCallback) => {

    JSDOM
    .fromURL(url)
    .then(dom => {

        let images = dom.window.document.querySelectorAll("img")
        let i = 0;
        for(let img of images) {
            if(img.src) chargement(img.src, (prefixe + i), errCallback)
            i = i + 1
        }
    })
    .catch((error) => errCallback(error))
}

recupereImages('https://wamiz.com/chats/chats-heureux.html', 'chat')