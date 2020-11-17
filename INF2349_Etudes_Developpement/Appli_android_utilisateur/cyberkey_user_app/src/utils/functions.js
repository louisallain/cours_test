export function atob(input) {
    var keyStr = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/='
    var output = ''
    var chr1, chr2, chr3
    var enc1, enc2, enc3, enc4
    var i = 0
    input = input.replace(/[^A-Za-z0-9\+\/\=]/g, '')
    do {
        enc1 = keyStr.indexOf(input.charAt(i++))
        enc2 = keyStr.indexOf(input.charAt(i++))
        enc3 = keyStr.indexOf(input.charAt(i++))
        enc4 = keyStr.indexOf(input.charAt(i++))
        chr1 = (enc1 << 2) | (enc2 >> 4)
        chr2 = ((enc2 & 15) << 4) | (enc3 >> 2)
        chr3 = ((enc3 & 3) << 6) | enc4
        output = output + String.fromCharCode(chr1)
        if (enc3 !== 64) {
            output = output + String.fromCharCode(chr2)
        }
        if (enc4 !== 64) {
            output = output + String.fromCharCode(chr3)
        }
    } while (i < input.length)
    return output
}

export function base64ToHex(str) {
    const raw = atob(str);
    let result = '';
    for (let i = 0; i < raw.length; i++) {
      const hex = raw.charCodeAt(i).toString(16);
      result += (hex.length === 2 ? hex : '0' + hex);
    }
    return result.toUpperCase();
}

export function toHexString(byteArray) {
    return Array.prototype.map.call(byteArray, function(byte) {
      return ('0' + (byte & 0xFF).toString(16)).slice(-2);
    }).join('');
}

export function toByteArray(hexString) {
    var result = [];
    for (var i = 0; i < hexString.length; i += 2) {
        result.push(parseInt(hexString.substr(i, 2), 16));
    }
    return result;
}

/**
 * Converti une clef publique au format PEM en JSON de la forme {n: <modulus>, e: <exponent>}
 * @param {string} pemString une chaine de caractère de la forme "-----BEGIN RSA PUBLIC KEY-----<base64_encoded_public_key>-----END RSA PUBLIC KEY-----"
 */
export function publicKey_PEM_to_hex(pemString) {

    let publicKeybase64 = pemString.replace(/-----.*-----/g, "").replace(/\n/g, "")
    let publicKeyHex = base64ToHex(publicKeybase64)
    let publicKeyByteArray = toByteArray(publicKeyHex)
    let nbBytesOfTotalLength = (publicKeyByteArray[1] & 0x7F)

    let idxOfModulusSequence = 2 + nbBytesOfTotalLength; // 2 car [0] = SEQUENCE et [1] = lengthOfLength
    let nbBytesOfModulusSequenceLength = (publicKeyByteArray[idxOfModulusSequence+1] & 0x7F)
    let lengthOfModulusInteger = parseInt(toHexString(publicKeyByteArray.slice((idxOfModulusSequence+2), (idxOfModulusSequence+2+nbBytesOfModulusSequenceLength))), 16)
    let modulusHexStr = toHexString(publicKeyByteArray.slice(idxOfModulusSequence+2+nbBytesOfModulusSequenceLength, idxOfModulusSequence+2+nbBytesOfModulusSequenceLength+lengthOfModulusInteger))
    
    let idxOfExponentSequence = idxOfModulusSequence+2+nbBytesOfModulusSequenceLength+lengthOfModulusInteger
    let nbBytesOfExponentSequenceLength = (publicKeyByteArray[idxOfExponentSequence+1] & 0x7F)
    let lengthOfExponentInteger = parseInt(nbBytesOfExponentSequenceLength, 16)
    let exponentHexStr = toHexString(publicKeyByteArray.slice(idxOfExponentSequence+2, idxOfExponentSequence+2+lengthOfExponentInteger))

    return {
        n: modulusHexStr,
        e: exponentHexStr
    }
}