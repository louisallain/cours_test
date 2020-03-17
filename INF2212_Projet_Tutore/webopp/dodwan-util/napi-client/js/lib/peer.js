/* 
 * This module provides means to implement a listener to DoDWAN event 
 * related to changes its neighborhood
 */


console.log("Start module pubsub.js");

import * as connection from "./connection.js"
import * as util from "./util.js"
import * as session from "./session.js"

/*
 * Class PeerService ---------------------------------------------
 */
class PeerService {

    constructor() {
        this.listeners = new Set();
        session.service.setOnReceivePeerNotification((pdu) => {this.receiveNotif(pdu)});   
    }
    
    
    /**
    * Gives the id of the local peer (host name)
    * @param {function(string)} the callback passing the local peer id
    */
    getMyPeerName(okcallback, errorcallback) { 
        let command = new connection.Pdu();
        command.name = "get_my_peer";
        command.tkn = util.uniqueId();
        console.log("PeerService sending get_my_peer" + JSON.stringify(command));
        session.service.sendCommand(command, 
            (pdu) => {
                if (pdu.name !== "recv_my_pid") {
                  console.log("Strange: response to get_my_peer is not recv_my_pid");
                  return;
                }
                okcallback(pdu.pid);
            },
          
            (error) => { 
                console.log("PeerService: error when issuing get_my_peer"); 
                if (errorcallback) errorcallback(error);
            }
        );
    }
    
    
    
    /**
    * Gives the ids of the peers that are currently in the direct neighborhood
    * @param {function(string[])} the callback passing the array of ids
    */
    getPeers(okcallback, errorcallback) {
        let command = new connection.Pdu();
        command.name = "get_peers";
        command.tkn = util.uniqueId();
        console.log("PeerService sending get_peers: " + JSON.stringify(command));
        session.service.sendCommand(command, 
            (pdu) => {
                if (pdu.name !== "recv_pids") {
                  console.log("Strange: response to get_peers is not recv_pids");
                  return;
                }
                okcallback(pdu.pids);
            },
          
            (error) => { 
                console.log("PeerService: error when issuing get_peers"); 
                if (errorcallback) errorcallback(error);
            }
        );
    
    }
    
    
    /**
    * Register a listener to observe the peers in the direct neighborhood
    *
    * @param {PeerListener} listener - the listener to be registered
    */
    addListener(listener) {  
       this.listeners.add(listener);
    }


    /**
    * Unregister a listener
    *
    * @param {PeerListener} listener - the listener to be unregistered
    */
    removeListener(listener) { 
        this.listeners.delete(listener);
    }
    
    
    
     /**
     * Handles the reception of a peer notification (PDU without token).
     * @param {Pdu} notifPdu - the received PDU 
     */
    receiveNotif(notifPdu) {
        // ..... add_peer ..........................................................
        if (notifPdu.name === "add_peer") {
            console.log("Peer service received add_peer |pid=" + JSON.stringify(notifPdu.pid) + "|");
            for (const listener of this.listeners) {
                listener.addPeer(notifPdu.pid);
            }
            return;
        }
        // ..... remove_peer ..........................................................
        else if (notifPdu.name === "remove_peer") {
            console.log("Peer service received remove_peer |pid=" + JSON.stringify(notifPdu.pid) + "|");
            for (const listener of this.listeners) {
                listener.removePeer(notifPdu.pid);
            }
            return;
        }
        // ..... clear_peers ..........................................................
        else if (notifPdu.name === "clear_peers") {
            console.log("Peer service received clear_peers");
            for (const listener of this.listeners) {
                listener.clearPeers();
            }
            return;
        }
    }
} 
    

    

/*
 * Interface PeerListener ---------------------------------------------
 */
export class PeerListener {

    constructor(addPeer, removePeer, clearPeers) {
        this.addPeer = addPeer;
        this.removePeer = removePeer;
        this.clearPeers = clearPeers;
    }
    

}



// Singleton
export var service = new PeerService();




