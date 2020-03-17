

console.log("Start module util.js");



/**
 * Produces a string of digits that is (almost) unique across browsers and time
 * Two different browsers cannot generated the same id (at least it is very improbable).
 * In addition, successive calls to the function will produces different ids 
 * (even after browser shutdown).
 * @return {String}
 */
export function uniqueId() {
    let nav = window.navigator;
    let screen = window.screen;
    let numid = "";
    numid += nav.mimeTypes.length;
    //id += nav.userAgent.replace(/\D+/g, "");
    numid += nav.plugins.length;
    numid += screen.height || "";
    //id += screen.width || "";
    //id += screen.pixelDepth || "";
    
    numid += uniqueLocalId();
    return numid;
};


var counter = 0;
/**
 * Produces a string of digits that is unique along time, even across browser shutdowns.
 * We simply take the number of ms since 01/01/2018, 
 * concatenated with a counter, just to ensure that multiple calls in the same ms yield different ids
 * @return {String}
 */
export function uniqueLocalId() {
     let numid = "";
     const epoch_01_01_2018 = 1514761200000; // nb ms since EPOCH on 01/01/2018
     numid += (Date.now()-epoch_01_01_2018) ;
     numid += counter++; // just to ensure that multiple calls in the same ms yield different ids
     return numid;
}










        
// Adapted from https://github.com/beatgammit/base64-js


var lookup = [];
var revLookup = [];


var code = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
for (var i = 0, len = code.length; i < len; ++i) {
  lookup[i] = code[i];
  revLookup[code.charCodeAt(i)] = i;
}

// Support decoding URL-safe base64 strings, as Node.js does.
// See: https://en.wikipedia.org/wiki/Base64#URL_applications
revLookup['-'.charCodeAt(0)] = 62;
revLookup['_'.charCodeAt(0)] = 63;

function placeHoldersCount (b64) {
  var len = b64.length;
  if (len % 4 > 0) {
    throw new Error('Invalid string. Length must be a multiple of 4');
  }

  // the number of equal signs (place holders)
  // if there are two placeholders, than the two characters before it
  // represent one byte
  // if there is only one, then the three characters before it represent 2 bytes
  // this is just a cheap hack to not do indexOf twice
  return b64[len - 2] === '=' ? 2 : b64[len - 1] === '=' ? 1 : 0;
}

export function b64ByteLength (b64) {
  // base64 is 4/3 + up to two characters of the original data
  return (b64.length * 3 / 4) - placeHoldersCount(b64);
}

export function b64ToUint8Array (b64) {
  var i, l, tmp, placeHolders, arr;
  var len = b64.length;
  placeHolders = placeHoldersCount(b64);

  arr = new Uint8Array((len * 3 / 4) - placeHolders);

  // if there are placeholders, only get up to the last complete 4 chars
  l = placeHolders > 0 ? len - 4 : len;

  var L = 0;

  for (i = 0; i < l; i += 4) {
    tmp = (revLookup[b64.charCodeAt(i)] << 18) | (revLookup[b64.charCodeAt(i + 1)] << 12) | (revLookup[b64.charCodeAt(i + 2)] << 6) | revLookup[b64.charCodeAt(i + 3)];
    arr[L++] = (tmp >> 16) & 0xFF;
    arr[L++] = (tmp >> 8) & 0xFF;
    arr[L++] = tmp & 0xFF;
  }

  if (placeHolders === 2) {
    tmp = (revLookup[b64.charCodeAt(i)] << 2) | (revLookup[b64.charCodeAt(i + 1)] >> 4);
    arr[L++] = tmp & 0xFF;
  } else if (placeHolders === 1) {
    tmp = (revLookup[b64.charCodeAt(i)] << 10) | (revLookup[b64.charCodeAt(i + 1)] << 4) | (revLookup[b64.charCodeAt(i + 2)] >> 2);
    arr[L++] = (tmp >> 8) & 0xFF;
    arr[L++] = tmp & 0xFF;
  }

  return arr;
}

function tripletToBase64 (num) {
  return lookup[num >> 18 & 0x3F] + lookup[num >> 12 & 0x3F] + lookup[num >> 6 & 0x3F] + lookup[num & 0x3F];
}

function encodeChunk (uint8, start, end) {
  var tmp;
  var output = [];
  for (var i = start; i < end; i += 3) {
    tmp = ((uint8[i] << 16) & 0xFF0000) + ((uint8[i + 1] << 8) & 0xFF00) + (uint8[i + 2] & 0xFF);
    output.push(tripletToBase64(tmp));
  }
  return output.join('');
}

export function uint8ArrayToB64 (uint8) {
  var tmp;
  var len = uint8.length;
  var extraBytes = len % 3 ;// if we have 1 byte left, pad 2 bytes
  var output = '';
  var parts = [];
  var maxChunkLength = 16383 ;// must be multiple of 3

  // go through the array every three bytes, we'll deal with trailing stuff later
  for (var i = 0, len2 = len - extraBytes; i < len2; i += maxChunkLength) {
    parts.push(encodeChunk(uint8, i, (i + maxChunkLength) > len2 ? len2 : (i + maxChunkLength)));
  }

  // pad the end with zeros, but make sure to not forget the extra bytes
  if (extraBytes === 1) {
    tmp = uint8[len - 1];
    output += lookup[tmp >> 2];
    output += lookup[(tmp << 4) & 0x3F];
    output += '==';
  } else if (extraBytes === 2) {
    tmp = (uint8[len - 2] << 8) + (uint8[len - 1]);
    output += lookup[tmp >> 10];
    output += lookup[(tmp >> 4) & 0x3F];
    output += lookup[(tmp << 2) & 0x3F];
    output += '=';
  }

  parts.push(output);

  return parts.join('');
}




export function stringToUtf8Array(str) {
    let te = new TextEncoder("utf-8");
    return te.encode(str);
}

export function utf8ArrayToString(array) {
    let td = new TextDecoder("utf-8");
    return td.decode(array);
}


export function stringToB64(str) {
    return uint8ArrayToB64(stringToUtf8Array(str));
}

export function b64ToString(b64) {
    return utf8ArrayToString(b64ToUint8Array(b64));
}




/*
 * Computes the basename of a filename
 * @param {type} str the filename
 * @return {String} the substring after the last /
 */
function baseName(str) {
    let lastSlash = str.lastIndexOf('/');
    if (lastSlash === -1)  return str;
    if (lastSlash === str.length - 1) return "";
    return str.slice(lastSlash, str.length);
}
 
     