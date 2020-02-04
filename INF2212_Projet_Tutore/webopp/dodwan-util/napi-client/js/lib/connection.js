/* 
 * This module provides 
 *   - class PDU that models a Dodwan-NAPI PDU 
 *   - a WebsocketConnectionService singleton object, named service, thats offers methods and callbacks to 
 *        . handle a websocket connection 
 *        . transfer PDUs on this websocket
 */

console.log("Start module connection.js");
import * as util from "./util.js"


/*
 * Class Pdu -------------------------------------------------
 * Command, response or notification
 */ 
export class Pdu {
    constructor() {}
    
}



/*
 * Class WebsocketConnectionService -----------------------------------------
 *
 */
export class WebsocketConnectionService {

    constructor() {
        this.socket = null;

        
        /** private **/
        this.reconnectNbTries = 0;
        
        this.reconnectNbTriesMax = 0;
        this.reconnectPeriod = 0;
        
        /*
         * Callback when the transmistter produced an error in sending or receiving
         */
        this.onSocketError = function () {console.log("Empty onSocketError()");};
        
        
        /*
         * Callback when the transmistter has opened the socket
         */
        this.onSocketOpen = function () {console.log("Empty onSocketOpen()");};
        
        /*
         * Callback when the transmistter produced an error in sending or receiving
         */
        this.onSocketClose = function () {console.log("Empty onSocketClose()");};

        /*
         * Callback when a PDU has been received
         */
        this.onReceivePdu = function () {console.log("Empty onreceivePdu()");};
    }
    ;
    
    
    
    /*
     * Setters for callbacks
     */
    
    setOnSocketError(f) { this.onSocketError = f; }
    setOnSocketOpen(f) { this.onSocketOpen = f; }
    setOnSocketClose(f) { this.onSocketClose = f; }
    setOnReceivePdu(f) { this.onReceivePdu = f; }

    
    
    /*
     * Opens the websocket  
     */
    connect(server) {
        try {
            console.log("Connection to websocket server: "+"ws://"+server+"/")
            // Rq: a socket error is triggered when this fails
            this.socket = new WebSocket("ws://"+server+"/");
        } catch (exception) {
            //console.error(exception);
            return;
        }
        
        this.socket.onerror =  (error) => {
            console.log("Connection failed");
            //console.error(error);
            ///this.reconnectLater(server);
            this.onSocketError(error);
        };

        this.socket.onopen = (event) => {
            console.log("Connection established.");
            this.onSocketOpen();
            
            this.socket.onclose =  (event) => {
                console.log("Connection closed.");
                //this.reconnectLater(server);
                this.onSocketClose();
            };

            // when receiving a ws message
            this.socket.onmessage =  (event) => {
                console.log("data=" + event.data);

                let reader = new FileReader();
                let blobReceived = new Blob();
                 
                reader.onload = () => {
                    //console.log("wsclient received message (length="+reader.result.length+") : "+reader.result);
                    let pdu = JSON.parse(reader.result);
                    console.log("wsclient received PDU:" +JSON.stringify(pdu));
                    this.onReceivePdu(pdu);
                };

                // Read in the blob as a json string 
                reader.readAsText(event.data);
                //reader.readAsArrayBuffer(blobReceived);
            };

        };
    }



    disconnect()  {
        this.socket.onclose = function() {};
        this.socket.close();
    }


    /*
     * Sends a PDU on the websocket
     * @param {Pdu} the PDU to be sent
     */
    sendPdu(pdu) {
        let serializedPdu = JSON.stringify(pdu);
        console.log("Sending PDU: "+serializedPdu);
        let buffer = new Uint8Array(util.stringToUtf8Array(serializedPdu));
        this.socket.send(buffer);
        return; 
    }
   
    
}






// =======================================================================

// Singleton
export var service = new WebsocketConnectionService();


