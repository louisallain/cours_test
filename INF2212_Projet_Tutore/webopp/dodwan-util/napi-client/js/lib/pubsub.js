/* 
 * This module provides means to implement a client to DoDWAN that allow 
 * content-based publication-subscription 
 * 
 * The main following tools are defined:
 *   - class Descriptor that models a descriptor 
 *   - a PubsubService singleton object, named service, that offers the methods and callabcks for
 *        . publishing messages, 
 *        . subscribing to messages according to their content 
 *        . receiving messages
 */


console.log("Start module pubsub.js");

import * as util from "./util.js"
import * as connection from "./connection.js"
import * as session from "./session.js"

/*
 * A descriptor that contains (name, value) pairs used to describe a message
 * The getAttribute and setAttribute methods are alternative to the usual notation 
 * for accessing property names and values of Javascript objects.
 */
export class Descriptor {

    constructor() {
    }

    /*-----------------------------------------------------------
    * Gives the value associated to the given name
    * @param {string} name - a name
    * @return {string} value - the value associated to the name (may be null)
    */
    getAttribute(name) { 
        if (this.hasOwnProperty(name)) return null;
        return this[name]; 
    }


    /*-----------------------------------------------------------
    * Registers a name,value pair. If the given name is already registered in this
    * descriptor with another value, the given value replaces the previous one
    * @param {string} name - a name
    * @param {string} value - the value to be associated to the name
    */
    setAttribute(name, value) {
        if (name === "") 
            return null;
        if (this.hasOwnProperty(name))
            return false;

        this[name] = value;
        return true;
    }

    /*-----------------------------------------------------------
    * Gives a string (JSON) representation of this descriptor
    * @return {string} the string representation of this descriptor
    */
    toJson() {
        return JSON.stringify(this);
    }


    /*-----------------------------------------------------------
    * Gives a descriptor instance from a string (JSON) representation. The string
    * representation returned by the {@link toString()} method should give an
    * instance having the same value
    * @param {string} str - string representation of a descriptor
    * @return {Descriptor} a descriptor instance. Null if the given string format is not correct
    */
    static fromJson(str) {
        return Object.assign(new Descriptor(), JSON.parse(str));
    }

}




/*
 * Class PubsubService ---------------------------------------------
 */
class PubsubService {

    constructor() {
       
       // How to handle notifications
       session.service.setOnReceivePubsubNotification((pdu) => {this.receiveNotif(pdu);});
         
       // Map <skey, processor> : map of the processors associated to subscription keys
       this.processors = new Map;          
    }  
       
   
       
    /*-----------------------------------------------------------
    * Publishes a message composed of the given binary data
    *
    * @param desc {Descriptor} the message descriptor, which message id (mid) should not be null
    * @param buffer {ArrayBuffer} the payload to be transferred
    */
    publishBuffer(desc, buffer, okcallback, errorcallback) {   
        let command = new connection.Pdu();
        command.name = "publish";
        command.tkn = util.uniqueId();
        command.desc = desc;
        command.data = buffer;
        session.service.sendCommand(command, okcallback, errorcallback);
        return;
    }


    /*-----------------------------------------------------------
    * Publishes a message composed of binary data stored in the given file. If the
    * client and server share the same file system, the absolute path of the file
    * is transfered to the server. Otherwise, the content of the file is transfered
    * to the server as the payload of a message.
    *
    * @param desc {Descriptor} the message descriptor, which message id (mid) should not be null
    * @param file {File} the file to be transferred
    */
    publishFile(desc, file, okcallback, errorcallback) {
        
    }


    /*-----------------------------------------------------------
    * Subscribes in order to receive messages whose descriptors match the given pattern
    * @param skey {string} the subscription (unique) key
    * @param pattern {Descriptor} a descriptor that contains values that should match the received messages
    * @param processor {function(Descriptor)} function that will process the received descriptor
    */
    addSubscription(skey, pattern, processor, okcallback, errorcallback) { 
       
        console.log("addSubscription "+ skey + " : " + JSON.stringify(pattern));
        
        let proc = (desc) => {processor(desc);};
        console.log("registering processor : "+proc);
        this.processors.set(skey, proc);
        
        let command = new connection.Pdu();
        command.name = "add_sub";
        command.tkn = util.uniqueId();
        command.key = skey;
        command.desc = pattern;
        session.service.sendCommand(command, 
            //ok
            () => {
               okcallback();
            },
                    
            //error
            (error) => {if (errorcallback !== undefined) errorcallback(error);}
        );
        return;
    }


    /*-----------------------------------------------------------
    * Unregisters a subscription
    * @param {string} skey - the key of the subscription to be unregistered
    */
    removeSubscription(skey, okcallback, errorcallback) { 
        console.log("removeSubscription : " + skey);
        let command = new connection.Pdu();
        command.name = "remove_sub";
        command.tkn = util.uniqueId();
        command.subs = [skey];
        session.service.sendCommand(command, 
            //ok
            () => {
               this.processors.delete(skey);
               okcallback();
            },
                    
            //error
            (error) => {if (errorcallback !== undefined) errorcallback(error);}
        );
    }

    
    /*-----------------------------------------------------------
    * Gives all the ids of the messages currently in the server's cache that match the subscriptions
    * having the given keys
    * @param {string[]} skeys - the subscription keys. If some keys are unknown, they are ignored
    * @param {function(string[])} okcallback - the callback passing the array of message ids (may be empty)
    * @param {function(string)} [errorcallback] - the callback passing a reason when an error occurred 
   */
    getMatching(skeys, okcallback, errorcallback) {
        let command = new connection.Pdu();
        command.name = "get_matching";
        command.tkn = util.uniqueId();
        command.subs = skeys;
        console.log("PubSub sending get_matching: " + JSON.stringify(command));  
        session.service.sendCommand(command, 
            (midsPdu) => {
                if (midsPdu.name !== "recv_mids") {
                  console.log("Strange: response to get_matching is not recv_mids");
                  return;
                }
                console.log("PubsSub received recv_mids |mids="+ JSON.stringify(midsPdu.mids)+"|");
                okcallback(midsPdu.mids);
            },
            
            (error) => { 
                console.log("Pubsub: Error when executing get_matching"); 
                 if (errorcallback !== undefined) errorcallback(error);
            }
        );
    }




    /*-----------------------------------------------------------
    * Gives the descriptor of a message present in the server's cache
    * @param {string} mid - the id of the message to search for
    * @param {function(Descriptor)} okcallback - the callback passing the searched descriptor
    * @param {function(string)} [errorcallback] - the callback when an error occurred (e.g no such message found)
    */
     getDescriptor(mid, okcallback, errorcallback) {
        let command = new connection.Pdu();
        command.name = "get_desc";
        command.tkn = util.uniqueId();
        command.mid = mid;
        console.log("PubSub sending get_desc: " + JSON.stringify(command));  
        session.service.sendCommand(command,
            //ok   
            (descPdu) => {
                if (descPdu.name !== "recv_desc") {
                    console.log("Strange: response to get_desc is not recv_desc");
                    return;
                }
                console.log("PubsSub received response recv_desc  |desc=" + JSON.stringify(descPdu.desc)+"|"); 
                okcallback(descPdu.desc);
            },
            
            // error
            (error) => { 
                console.log("Pubsub: Error when executing get_desc"); 
                if (errorcallback !== undefined) errorcallback(error);
            }
        );
    }


    /*-----------------------------------------------------------
    * Gives the content of a message
    * @param {string} mid - the requested message id
    * @param {function(ArrayBuffer)} okcallback - the callback passing the message payload
    * @param {function(string)} [errorcallback] - the callback when an error occurred
    */
    getAsBuffer(mid, okcallback, errorcallback)  { 
        console.log("calling getAsBuffer with mid="+mid);
        let command = new connection.Pdu();
        command.name = "get_payload";
        command.tkn = util.uniqueId();
        command.mid = mid;
        console.log("PubsSub sending get_payload: " + JSON.stringify(command));
        session.service.sendCommand(command, 
           (pdu) => {
               if (pdu.name !== "recv_payload") {
                    console.log("Strange: response to get_payload is not recv_payload");
                    return; 
               }
               console.log("PubsSub received recv_payload mid=" + pdu.mid);
               if (pdu.hasOwnProperty("expired") && pdu.expired) {
                  console.log("PubsSub received recv_payload expired  |mid=" + pdu.mid+"|");
               }
               else {
                   okcallback(pdu.data);
               }
               
           },
           
           (error) => { console.log("Pubsub: Error when executing get_payload"); 
                if (errorcallback !== undefined)  errorcallback(error);
           }  
                
        );
    
    }


    /*-----------------------------------------------------------
    * Handles the reception of a notification (PDU without token).
    * The only case is the notification of the reception of a message, 
    * that triggers the execution of the associated processor.
    * Note that several such notifications may occur for the same message, 
    * if several subscriptions were set with patterns matching the message.
    * @private
    * @param {Pdu} notifPdu - the received PDU 
    */
    receiveNotif(notifPdu) {
        console.log("PubsSub received notification PDU " + JSON.stringify(notifPdu)); 
        
         if (notifPdu.name !== "recv_desc") {
             console.log("Strange: notification is not recv_desc");
             return;
         }
         
         console.log("PubsSub execute processor for key " + notifPdu.key);
        
        // Execute the processor that was registered at subscription time
        let proc = this.processors.get(notifPdu.key);  
        proc(notifPdu.desc);
    }

}






// Singleton
// 
export var service = new PubsubService();

