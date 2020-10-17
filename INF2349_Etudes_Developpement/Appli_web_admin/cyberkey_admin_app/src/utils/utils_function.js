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

export const downloadFileFromText = (text, filename) => {
    let a = document.createElement("a");
    let file = new Blob([text], {type: 'text/plain'});
    a.href = URL.createObjectURL(file);
    a.download = filename;
    a.click();
}