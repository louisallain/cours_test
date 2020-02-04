/* 
 * This module provides 
 *   - a Session singleton object, named service, that offers methods and callabcks for
 *        . handling the lifecycle of a session 
 *        . sending commands, and receiving responses and notifcations
 *   - Interface SessionListener to receive events related to the status of the session
 */

console.log("Start module session.js");

import * as util from "./util.js"
import * as connection from "./connection.js"
import * as configuration from "./configuration.js"


 
/*
 * Class Session ---------------------------------------------
 */
class Session {
    

    constructor() {
 
        // List of subscriptions registered in DoDWAN
        this.subscriptions = [];
        
        this.client = clientId(); 
        
        this.continuation = false;
        
        this.reconnectPeriod = 15;
        this.reconnectNbTriesMax = 5;
        
        this.listeners = new Set();
        
      
        this.onReceivePubsubNotification = function () {console.log("Empty onReceivePubsubNotification");};
        this.onReceivePeerNotification = function () {console.log("Empty onReceivePeerNotification");};
    
      
      
        // Maps of the callbacks to execute when receiving a response or an error,
        // for each token.  A callback should be removed just after its execution
        this.responseCallbacks = new Map();
        this.errorCallbacks = new Map();

        // Handles incoming PDUS (notifications and responses)
        connection.service.setOnReceivePdu((pdu) => {
            this.receivePdu(pdu);
        });
      
    }
    
    
   /*-----------------------------------------------------------
    * Sets a callback for pubsub notifications
    * @param {function} f - the callback to be registered
    */
    setOnReceivePubsubNotification(f) { 
        this.onReceivePubsubNotification = f; 
    }
     
    /*-----------------------------------------------------------
    * Sets a callback for peer notifications
    * @param {function} f - the callback to be registered
    */    
    setOnReceivePeerNotification(f) { 
        this.onReceivePeerNotification = f; 
    }
     
     
   /*-----------------------------------------------------------
    * Registers a session listener, to observe the changes in the status of the session
    * @param {SessionListener} listener - the listener to be registered
    */
    addListener(listener) {
       this.listeners.add(listener);
    }


   /*-----------------------------------------------------------
    * Unregisters a session listener
    * @param {SessionListener} listener - the listener to be unregistered
    */
    removeListener(listener) { 
        this.listeners.delete(listener);
    }
    
    
    /*-----------------------------------------------------------
     * Tries to reconnect periodically to the server
     * @param {string} server - the server address in the form host:port 
     * @private
     */
    reconnectLater(server) {
        this.continuation = true;
        if ((this.reconnectNbTriesMax === 0) ||  (this.reconnectPeriod === 0)) return;
        this.reconnectNbTries ++;     
          if (this.reconnectNbTries < this.reconnectNbTriesMax) {
             console.log("Will try to reconnect in "+this.reconnectPeriod+"s (try #" + this.reconnectNbTries + ")");
             window.setTimeout(() => {connection.service.connect(server);}, this.reconnectPeriod*1000);
          }
          else {
              console.log("Give up trying to reconnect"); 
              for (const listener of this.listeners) listener.onSessionStopped();
          }
    }


    
    /*-----------------------------------------------------------
    * Closes the session. All subscriptions are forgotten and the connection is closed
    */
    bye() {
        this.subscriptions = [];
        connection.service.disconnect();  
        for (const listener of this.listeners) listener.onSessionStopped();
    }
    
    
    
     /*-----------------------------------------------------------
     * Asks to be ready to publish and receive notifications: 
     * open a connection, and when it is opened, tells the server to open a session, and start notifications
     */
    start() {
        let server = configuration.service.config.server_host + ":" + configuration.service.config.server_port;
        this.reconnectPeriod = configuration.service.config.reconnect_period;
        this.reconnectNbTriesMax = configuration.service.config.reconnect_nb_tries_max;
        
        // Begin in the starting state
        for (const listener of this.listeners) listener.onSessionStarting();      
        
        connection.service.setOnSocketError((error) => {
            for (const listener of this.listeners) listener.onConnectionError(error);
            this.reconnectLater(server);   
        });
        
         connection.service.setOnSocketClose(() => {
            for (const listener of this.listeners) listener.onConnectionClosed();
            this.reconnectLater(server);       
        });
        
        
        connection.service.setOnSocketOpen(() => {
            
            for (let listener of this.listeners) listener.onConnectionOpened();
            
            // Opens a session 
            let command = new connection.Pdu();
            command.name = "hello";
            command.client = this.client;
            command.cont = this.continuation;
            command.tkn = util.uniqueId();
            this.sendCommand(command,
                () => {
                    // OK : authorize traffic to and from DoDWAN
                    let command = new connection.Pdu();
                    command.name = "start";
                    command.tkn = util.uniqueId();
                    this.sendCommand(command,
                         (okPdu) => { console.log("Traffic to/from DoDWAN authorized");
                                      for (const listener of this.listeners) listener.onSessionStarted();
                         },
                             
                         (error) => {console.log("Strange error when authorizing traffic :"+error);
                                     bye(); // error considered fatal
                                    }
                    );
                  
                  
                  
                  
                },
                
                (error) => {console.log("Strange error when opening session : "+error);
                            bye(); // error considered fatal
                           }
            );
            
          
            
        });
        
        
        // Initial connection
        connection.service.connect(server); 
         
    }
    
    
    
    /*-----------------------------------------------------------
    * Stops the traffic from/to DoDWAN
    */
    stop() {
        let command = new connection.Pdu();
        command.name = "stop";
        command.tkn = util.uniqueId();
        sendCommand(command,
            (response) => { console.log("Traffic to/from DoDWAN blocked")
                    for (const listener of this.listeners) listener.onSessionStopped();
                  },
                             
                  (error) => {console.log("Strange error when blocking traffic :"+error);
                              bye(); // error considered fatal
                  }
        );            
    }
    
    
    
    /*-----------------------------------------------------------
     * Sends a command PDU
     * @param {Pdu} pdu - The PDU to be sent
     * @param {function(Pdu)} responseCallback - the function passing the response to call when all goes well 
     * @param {function(string)} errorCallback - the function passing the error reason when an error occurs 
     */ 
    sendCommand(pdu, responseCallback, errorCallback) {
       if (pdu.tkn) {
           if (responseCallback) this.responseCallbacks.set(pdu.tkn, responseCallback); 
           if (errorCallback) this.errorCallbacks.set(pdu.tkn, errorCallback);
        }
        connection.service.sendPdu(pdu);
        return;
    }
    


    //////////////////////////////////////////////////////////////////////////////////////
    // Private methods
    //////////////////////////////////////////////////////////////////////////////////////

    /*-----------------------------------------------------------
     * Dispatch the received PDU to the good notifier
     * @param {Pdu} pdu - The received PDU
     * @private
     */ 
    receivePdu(pdu) {
        if (pdu.tkn) {
            this.receiveResponse(pdu);
        }
        else {
            this.receiveNotification(pdu);
        }
    }
    
      

    /*-----------------------------------------------------------
     * Handles a response PDU: triggers the ok or error callback
     * corresponding to the command, according to the token present in the PDU
     * @param {Pdu} pdu - The received response PDU
     * @private
     */
    receiveResponse(pdu) {
        if (pdu.name === "error") {
            this.errorCallbacks.get(pdu.tkn)(pdu.reason);
            this.errorCallbacks.delete(pdu.tkn);
        } else { // any other name, including ok
            this.responseCallbacks.get(pdu.tkn)(pdu);
            this.responseCallbacks.delete(pdu.tkn);
        }
    }
     
     
   /*-----------------------------------------------------------
    * Dispatch a notification PDU to the right pre-registered notifier:
    * (pubsub or peer notifier)
    * @param {Pdu} pdu - The received notification PDU
    * @private
    */
    receiveNotification(pdu) {
        
        if (pdu.name === "recv_desc") {
            console.log("Session received pubsub notification PDU "+JSON.stringify(pdu))
            this.onReceivePubsubNotification(pdu);
        }
        else if ((pdu.name === "add_peer") || (pdu.name === "remove_peer") || (pdu.name === "clear_peers")){
            this.onReceivePeerNotification(pdu);
        }
        else {
            console.log("Unknown notification "+pdu.name);
        }
    }
     
     
    
  

}



  /*-----------------------------------------------------------
     * Get the unique client id, or create if it is the first call.
     * This id is stored in the LocalStorage, that is bound to the browser 
     * and the host from which the javascript application is downloaded. 
     * @private
     */
     function clientId() {
        let id = window.localStorage.getItem("clientId");
        if (id !== null) {
            console.log("Got client id from local storage :" + id);
            return id;
        }
        id = util.uniqueId();
        window.localStorage.setItem("clientId", id);
        console.log("Set new client id in local storage :" + id);
        return id;
    }

   



/*
 * Interface SessionListener ---------------------------------------------
 */
export class SessionListener {

    constructor( onConnectionOpened, onConnectionClosed, onConnectionError, 
                 onSessionStarting, onSessionStarted, onSessionStopped) {
                     
    /**
    * Method called when the connection associated to the session is opened
    */             
    this.onConnectionOpened = onConnectionOpened;
        
    /**
    * Method called when the connection associated to the session is closed
    */
    this.onConnectionClosed = onConnectionClosed;

    /**
    * Method called when an error occurs while opening the connection associated
    * to the session
    * @param {string} reason 
    */
    this.onConnectionError = onConnectionError;

    /**
    * Method called when the session is being starting: trying to (re)open a connection
    * and transfer opening pdus to the server
    */
    this.onSessionStarting = onSessionStarting;

    /**
    * Method called when the session is started
    */
    this.onSessionStarted = onSessionStarted;

    /**
    * Method called when the session is stopped. Can occur either after a
    * {@link Session#stop()} invocation or when the connection cannot be established
    * or reestablished after having been disconnected 
    */
    this.onSessionStopped = onSessionStopped;
    }
    

}




// Singleton
export var service =  new Session();