/**
 * File contains utils functions.
 */

/**
 * Hash text using SHA-256 algorithm.
 * @param  {String} text the text to be hashed
 * @return {String} hashed text of 64 bytes length.
 */
export const digestText = async (text) => {

    const encoder = new TextEncoder();
    const data = encoder.encode(text);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));                     // convertit le buffer en tableau d'octet
    const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join(''); // convertit le tableau en chaîne hexadélimale
    return hashHex;
}
/**
 * Hash text using SH-256 algorithm a number of times.
 * @param  {String} text the text to be hashed.
 * @param  {Number} N the number of time the text should be hashed.
 * @return {String} hashed text N times of 64 bytes length.
 */
export const digestText_N_Times = async (text, N) => {

    let tmp = await digestText(text)
    for(let i=1; i===N; i++) tmp = await digestText(tmp)
    return tmp;
}

/**
 * Télécharge un fichier dans lequel sera contenu le "text" en paramètre.
 * @param {string} text texte qui sera contenu dans le fichier à télécharger
 * @param {string} filename nom du fichier qui sera téléchargé
 */
export const downloadFileFromText = (text, filename) => {
    let a = document.createElement("a");
    let file = new Blob([text], {type: 'text/plain'});
    a.href = URL.createObjectURL(file);
    a.download = filename;
    a.click();
}

/**
 * Converti un objet JSON en tableau.
 * @param {object} json l'objet json à convertir
 */
export const json2array = (json) => {
    let result = [];
    let keys = Object.keys(json);
    keys.forEach((key) =>{
        result.push(json[key]);
    });
    return result;
}

export const spliceSlice = (str, index, count, add) => {
    if (index < 0) {
      index = str.length + index;
      if (index < 0) {
        index = 0;
      }
    }
  
    return str.slice(0, index) + (add || "") + str.slice(index + count);
  }

/**
 * Parse un fichier ICS et retourne un tableau d'object JSON de la forme : 
 *  [{title : string, start : Date, end : Date, isCourse : boolean}]
 * Le booléen isCourse présent dans chaque object JSON du tableau retourné indique s'il s'agit d'un cours classique avec un enseignant
 * ou un créneau libre.
 * @param {string} text le text du fichier ics
 * @param {boolean} getOnlyCourses si vrai alors le tableau retourné ne contiendra pas les créneaux libres (sans enseignant).
 */
export const parseICS = (text, getOnlyCourses) => {

    let parseCustomDateStringToJSDateString = (customeDateString) => {
        let ret = spliceSlice(customeDateString, 4, 0, "-")
        ret = spliceSlice(ret, 7, 0, "-")
        ret = spliceSlice(ret, 13, 0, ":")
        ret = spliceSlice(ret, 16, 0, ":")
        return ret
    }

    let eventsArray = text.split(/BEGIN:VEVENT/g).slice(1) // enlève les métadonnées
    let events = []
    eventsArray.map(str => {
        
        let startDateStr = str.match(/DTSTART:.+\r\n/g)[0].replace(/DTSTART:/g, "").replace(/[\r\n]/g, "")
        startDateStr = parseCustomDateStringToJSDateString(startDateStr)
        let endDateStr = str.match(/DTEND:.+\r\n/g)[0].replace(/DTEND:/g, "").replace(/[\r\n]/g, "")
        endDateStr = parseCustomDateStringToJSDateString(endDateStr)
        let isCourse = (str.match(/DESCRIPTION:.+\r\n/g)[0].replace(/DESCRIPTION:/g, "").match(/\\n/g).length) > 4 ? true : false 

        events.push({
            title: str.match(/SUMMARY:.+\r\n/g)[0].replace(/SUMMARY:/g, "").replace(/[\r\n]/g, ""),
            start: new Date(startDateStr),
            end: new Date(endDateStr),
            isCourse: isCourse
        })
    })
   return events
}