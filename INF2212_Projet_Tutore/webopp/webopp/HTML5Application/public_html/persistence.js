/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

console.log("Start module persistence.js");

import * as pubsub from "./pubsub.js"
import * as message from "./message.js"

/*
 * Update the local storage for the lists of published messages, set subscriptions and 
 * received messages
 */

export function addPubListStorage(msg) {
    //console.log("addPubListStorage: " + JSON.stringify(msg));
    try {
         window.localStorage.setItem("_p" + msg.mid, msg.toJson());
    }
    catch (exception) {
        window.alert("Can't save published message : local storage full");
    }
}

export function addRecepListStorage(msg) {
    //console.log("addRecepListStorage: " + JSON.stringify(msg));
    try {
      window.localStorage.setItem("_r" + msg.mid, msg.toJson());
    }
    catch (exception) {
        window.alert("Can't save received message : local storage full");
    }
}

export function addSubsListStorage(key, desc) {
    //console.log("addSubsListStorage: " + JSON.stringify(desc));
    try {
      window.localStorage.setItem("_s" + key, desc.toJson());
    }
    catch (exception) {
        window.alert("Can't save subscription : local storage full");
    }  
}

export function removePubListStorage(msgid) {
    window.localStorage.removeItem("_p" + msgid);
}

export function removeRecepListStorage(msgid) {
    window.localStorage.removeItem("_r" + msgid);
}

export function removeSubsListStorage(key) {
    window.localStorage.removeItem("_s" + key);
}

export function removeAllListStorage(prefix) {
    let list = [];
    let k = 0;
    for (let i = 0; i < window.localStorage.length; i++) {
        let key = window.localStorage.key(i);
        if (key.startsWith(prefix))
            list[k++] = key;
    }

    for (let i = 0; i < list.length; i++) {
        window.localStorage.removeItem(list[i]);
    }
}

export function removeAllPubListStorage() {
    removeAllListStorage("_p");
}

export function removeAllSubsListStorage() {
    removeAllListStorage("_s");
}

export function removeAllRecepListStorage() {
    removeAllListStorage("_r");
}


export function loadListsStorage(pubList, subsList, recepList) {
    for (let i = 0; i < window.localStorage.length; i++) {
        let key = window.localStorage.key(i);

        if (key.startsWith("_p")) {
            let msg = message.Message.fromJson(window.localStorage.getItem(key));
            pubList.set(msg.mid, msg);
        } else if (key.startsWith("_r")) {
            let msg = message.Message.fromJson(window.localStorage.getItem(key));
            recepList.set(msg.mid, msg);
        } else if (key.startsWith("_s")) {
            let desc = pubsub.Descriptor.fromJson(window.localStorage.getItem(key));
            let subsKey = key.substring(2);
            //console.log("retreive subs " + subsKey + "  desc=" + desc.toJson());
            subsList.set(subsKey, desc);
        }
    }

}


 