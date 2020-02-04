(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
"use strict";

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

console.log("Start module configuration.js");

/*
* Class ConfigurationService ---------------------------------------------
*/

var ConfigurationService = (function () {
  function ConfigurationService() {
    _classCallCheck(this, ConfigurationService);

    this.config = {
      "server_host": "localhost",
      "server_port": "8025",
      "reconnect_period": "15",
      "reconnect_nb_tries_max": "5"
    };
  }

  // Singleton

  ConfigurationService.prototype.init = function init(conf) {
    this.config = _extends({}, this.config, conf);
  };

  return ConfigurationService;
})();

var service = new ConfigurationService();
exports.service = service;

},{}],2:[function(require,module,exports){
/* 
 * This module provides 
 *   - class PDU that models a Dodwan-NAPI PDU 
 *   - a WebsocketConnectionService singleton object, named service, thats offers methods and callbacks to 
 *        . handle a websocket connection 
 *        . transfer PDUs on this websocket
 */

"use strict";

exports.__esModule = true;

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj["default"] = obj; return newObj; } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var _utilJs = require("./util.js");

var util = _interopRequireWildcard(_utilJs);

/*
 * Class Pdu -------------------------------------------------
 * Command, response or notification
 */
console.log("Start module connection.js");

var Pdu = function Pdu() {
    _classCallCheck(this, Pdu);
}

/*
 * Class WebsocketConnectionService -----------------------------------------
 *
 */
;

exports.Pdu = Pdu;

var WebsocketConnectionService = (function () {
    function WebsocketConnectionService() {
        _classCallCheck(this, WebsocketConnectionService);

        this.socket = null;

        /** private **/
        this.reconnectNbTries = 0;

        this.reconnectNbTriesMax = 0;
        this.reconnectPeriod = 0;

        /*
         * Callback when the transmistter produced an error in sending or receiving
         */
        this.onSocketError = function () {
            console.log("Empty onSocketError()");
        };

        /*
         * Callback when the transmistter has opened the socket
         */
        this.onSocketOpen = function () {
            console.log("Empty onSocketOpen()");
        };

        /*
         * Callback when the transmistter produced an error in sending or receiving
         */
        this.onSocketClose = function () {
            console.log("Empty onSocketClose()");
        };

        /*
         * Callback when a PDU has been received
         */
        this.onReceivePdu = function () {
            console.log("Empty onreceivePdu()");
        };
    }

    // =======================================================================

    // Singleton

    /*
     * Setters for callbacks
     */

    WebsocketConnectionService.prototype.setOnSocketError = function setOnSocketError(f) {
        this.onSocketError = f;
    };

    WebsocketConnectionService.prototype.setOnSocketOpen = function setOnSocketOpen(f) {
        this.onSocketOpen = f;
    };

    WebsocketConnectionService.prototype.setOnSocketClose = function setOnSocketClose(f) {
        this.onSocketClose = f;
    };

    WebsocketConnectionService.prototype.setOnReceivePdu = function setOnReceivePdu(f) {
        this.onReceivePdu = f;
    };

    /*
     * Opens the websocket  
     */

    WebsocketConnectionService.prototype.connect = function connect(server) {
        var _this = this;

        try {
            console.log("Connection to websocket server: " + "ws://" + server + "/");
            // Rq: a socket error is triggered when this fails
            this.socket = new WebSocket("ws://" + server + "/");
        } catch (exception) {
            //console.error(exception);
            return;
        }

        this.socket.onerror = function (error) {
            console.log("Connection failed");
            //console.error(error);
            ///this.reconnectLater(server);
            _this.onSocketError(error);
        };

        this.socket.onopen = function (event) {
            console.log("Connection established.");
            _this.onSocketOpen();

            _this.socket.onclose = function (event) {
                console.log("Connection closed.");
                //this.reconnectLater(server);
                _this.onSocketClose();
            };

            // when receiving a ws message
            _this.socket.onmessage = function (event) {
                console.log("data=" + event.data);

                var reader = new FileReader();
                var blobReceived = new Blob();

                reader.onload = function () {
                    //console.log("wsclient received message (length="+reader.result.length+") : "+reader.result);
                    var pdu = JSON.parse(reader.result);
                    console.log("wsclient received PDU:" + JSON.stringify(pdu));
                    _this.onReceivePdu(pdu);
                };

                // Read in the blob as a json string
                reader.readAsText(event.data);
                //reader.readAsArrayBuffer(blobReceived);
            };
        };
    };

    WebsocketConnectionService.prototype.disconnect = function disconnect() {
        this.socket.onclose = function () {};
        this.socket.close();
    };

    /*
     * Sends a PDU on the websocket
     * @param {Pdu} the PDU to be sent
     */

    WebsocketConnectionService.prototype.sendPdu = function sendPdu(pdu) {
        var serializedPdu = JSON.stringify(pdu);
        console.log("Sending PDU: " + serializedPdu);
        var buffer = new Uint8Array(util.stringToUtf8Array(serializedPdu));
        this.socket.send(buffer);
        return;
    };

    return WebsocketConnectionService;
})();

exports.WebsocketConnectionService = WebsocketConnectionService;
var service = new WebsocketConnectionService();
exports.service = service;

},{"./util.js":6}],3:[function(require,module,exports){
/* 
 * This module provides means to implement a listener to DoDWAN event 
 * related to changes its neighborhood
 */

"use strict";

exports.__esModule = true;

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj["default"] = obj; return newObj; } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var _connectionJs = require("./connection.js");

var connection = _interopRequireWildcard(_connectionJs);

var _utilJs = require("./util.js");

var util = _interopRequireWildcard(_utilJs);

var _sessionJs = require("./session.js");

var session = _interopRequireWildcard(_sessionJs);

/*
 * Class PeerService ---------------------------------------------
 */
console.log("Start module pubsub.js");

var PeerService = (function () {
    function PeerService() {
        var _this = this;

        _classCallCheck(this, PeerService);

        this.listeners = new Set();
        session.service.setOnReceivePeerNotification(function (pdu) {
            _this.receiveNotif(pdu);
        });
    }

    /*
     * Interface PeerListener ---------------------------------------------
     */

    /**
    * Gives the id of the local peer (host name)
    * @param {function(string)} the callback passing the local peer id
    */

    PeerService.prototype.getMyPeerName = function getMyPeerName(okcallback, errorcallback) {
        var command = new connection.Pdu();
        command.name = "get_my_peer";
        command.tkn = util.uniqueId();
        console.log("PeerService sending get_my_peer" + JSON.stringify(command));
        session.service.sendCommand(command, function (pdu) {
            if (pdu.name !== "recv_my_pid") {
                console.log("Strange: response to get_my_peer is not recv_my_pid");
                return;
            }
            okcallback(pdu.pid);
        }, function (error) {
            console.log("PeerService: error when issuing get_my_peer");
            if (errorcallback) errorcallback(error);
        });
    };

    /**
    * Gives the ids of the peers that are currently in the direct neighborhood
    * @param {function(string[])} the callback passing the array of ids
    */

    PeerService.prototype.getPeers = function getPeers(okcallback, errorcallback) {
        var command = new connection.Pdu();
        command.name = "get_peers";
        command.tkn = util.uniqueId();
        console.log("PeerService sending get_peers: " + JSON.stringify(command));
        session.service.sendCommand(command, function (pdu) {
            if (pdu.name !== "recv_pids") {
                console.log("Strange: response to get_peers is not recv_pids");
                return;
            }
            okcallback(pdu.pids);
        }, function (error) {
            console.log("PeerService: error when issuing get_peers");
            if (errorcallback) errorcallback(error);
        });
    };

    /**
    * Register a listener to observe the peers in the direct neighborhood
    *
    * @param {PeerListener} listener - the listener to be registered
    */

    PeerService.prototype.addListener = function addListener(listener) {
        this.listeners.add(listener);
    };

    /**
    * Unregister a listener
    *
    * @param {PeerListener} listener - the listener to be unregistered
    */

    PeerService.prototype.removeListener = function removeListener(listener) {
        this.listeners["delete"](listener);
    };

    /**
    * Handles the reception of a peer notification (PDU without token).
    * @param {Pdu} notifPdu - the received PDU 
    */

    PeerService.prototype.receiveNotif = function receiveNotif(notifPdu) {
        // ..... add_peer ..........................................................
        if (notifPdu.name === "add_peer") {
            console.log("Peer service received add_peer |pid=" + JSON.stringify(notifPdu.pid) + "|");
            for (var _iterator = this.listeners, _isArray = Array.isArray(_iterator), _i = 0, _iterator = _isArray ? _iterator : _iterator[Symbol.iterator]();;) {
                var _ref;

                if (_isArray) {
                    if (_i >= _iterator.length) break;
                    _ref = _iterator[_i++];
                } else {
                    _i = _iterator.next();
                    if (_i.done) break;
                    _ref = _i.value;
                }

                var listener = _ref;

                listener.addPeer(notifPdu.pid);
            }
            return;
        }
        // ..... remove_peer ..........................................................
        else if (notifPdu.name === "remove_peer") {
                console.log("Peer service received remove_peer |pid=" + JSON.stringify(notifPdu.pid) + "|");
                for (var _iterator2 = this.listeners, _isArray2 = Array.isArray(_iterator2), _i2 = 0, _iterator2 = _isArray2 ? _iterator2 : _iterator2[Symbol.iterator]();;) {
                    var _ref2;

                    if (_isArray2) {
                        if (_i2 >= _iterator2.length) break;
                        _ref2 = _iterator2[_i2++];
                    } else {
                        _i2 = _iterator2.next();
                        if (_i2.done) break;
                        _ref2 = _i2.value;
                    }

                    var listener = _ref2;

                    listener.removePeer(notifPdu.pid);
                }
                return;
            }
            // ..... clear_peers ..........................................................
            else if (notifPdu.name === "clear_peers") {
                    console.log("Peer service received clear_peers");
                    for (var _iterator3 = this.listeners, _isArray3 = Array.isArray(_iterator3), _i3 = 0, _iterator3 = _isArray3 ? _iterator3 : _iterator3[Symbol.iterator]();;) {
                        var _ref3;

                        if (_isArray3) {
                            if (_i3 >= _iterator3.length) break;
                            _ref3 = _iterator3[_i3++];
                        } else {
                            _i3 = _iterator3.next();
                            if (_i3.done) break;
                            _ref3 = _i3.value;
                        }

                        var listener = _ref3;

                        listener.clearPeers();
                    }
                    return;
                }
    };

    return PeerService;
})();

var PeerListener = function PeerListener(addPeer, removePeer, clearPeers) {
    _classCallCheck(this, PeerListener);

    this.addPeer = addPeer;
    this.removePeer = removePeer;
    this.clearPeers = clearPeers;
}

// Singleton
;

exports.PeerListener = PeerListener;
var service = new PeerService();
exports.service = service;

},{"./connection.js":2,"./session.js":5,"./util.js":6}],4:[function(require,module,exports){
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

"use strict";

exports.__esModule = true;

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj["default"] = obj; return newObj; } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var _utilJs = require("./util.js");

var util = _interopRequireWildcard(_utilJs);

var _connectionJs = require("./connection.js");

var connection = _interopRequireWildcard(_connectionJs);

var _sessionJs = require("./session.js");

var session = _interopRequireWildcard(_sessionJs);

/*
 * A descriptor that contains (name, value) pairs used to describe a message
 * The getAttribute and setAttribute methods are alternative to the usual notation 
 * for accessing property names and values of Javascript objects.
 */
console.log("Start module pubsub.js");

var Descriptor = (function () {
    function Descriptor() {
        _classCallCheck(this, Descriptor);
    }

    /*
     * Class PubsubService ---------------------------------------------
     */

    /*-----------------------------------------------------------
    * Gives the value associated to the given name
    * @param {string} name - a name
    * @return {string} value - the value associated to the name (may be null)
    */

    Descriptor.prototype.getAttribute = function getAttribute(name) {
        if (this.hasOwnProperty(name)) return null;
        return this[name];
    };

    /*-----------------------------------------------------------
    * Registers a name,value pair. If the given name is already registered in this
    * descriptor with another value, the given value replaces the previous one
    * @param {string} name - a name
    * @param {string} value - the value to be associated to the name
    */

    Descriptor.prototype.setAttribute = function setAttribute(name, value) {
        if (name === "") return null;
        if (this.hasOwnProperty(name)) return false;

        this[name] = value;
        return true;
    };

    /*-----------------------------------------------------------
    * Gives a string (JSON) representation of this descriptor
    * @return {string} the string representation of this descriptor
    */

    Descriptor.prototype.toJson = function toJson() {
        return JSON.stringify(this);
    };

    /*-----------------------------------------------------------
    * Gives a descriptor instance from a string (JSON) representation. The string
    * representation returned by the {@link toString()} method should give an
    * instance having the same value
    * @param {string} str - string representation of a descriptor
    * @return {Descriptor} a descriptor instance. Null if the given string format is not correct
    */

    Descriptor.fromJson = function fromJson(str) {
        return Object.assign(new Descriptor(), JSON.parse(str));
    };

    return Descriptor;
})();

exports.Descriptor = Descriptor;

var PubsubService = (function () {
    function PubsubService() {
        var _this = this;

        _classCallCheck(this, PubsubService);

        // How to handle notifications
        session.service.setOnReceivePubsubNotification(function (pdu) {
            _this.receiveNotif(pdu);
        });

        // Map <skey, processor> : map of the processors associated to subscription keys
        this.processors = new Map();
    }

    // Singleton
    //

    /*-----------------------------------------------------------
    * Publishes a message composed of the given binary data
    *
    * @param desc {Descriptor} the message descriptor, which message id (mid) should not be null
    * @param buffer {ArrayBuffer} the payload to be transferred
    */

    PubsubService.prototype.publishBuffer = function publishBuffer(desc, buffer, okcallback, errorcallback) {
        var command = new connection.Pdu();
        command.name = "publish";
        command.tkn = util.uniqueId();
        command.desc = desc;
        command.data = buffer;
        session.service.sendCommand(command, okcallback, errorcallback);
        return;
    };

    /*-----------------------------------------------------------
    * Publishes a message composed of binary data stored in the given file. If the
    * client and server share the same file system, the absolute path of the file
    * is transfered to the server. Otherwise, the content of the file is transfered
    * to the server as the payload of a message.
    *
    * @param desc {Descriptor} the message descriptor, which message id (mid) should not be null
    * @param file {File} the file to be transferred
    */

    PubsubService.prototype.publishFile = function publishFile(desc, file, okcallback, errorcallback) {};

    /*-----------------------------------------------------------
    * Subscribes in order to receive messages whose descriptors match the given pattern
    * @param skey {string} the subscription (unique) key
    * @param pattern {Descriptor} a descriptor that contains values that should match the received messages
    * @param processor {function(Descriptor)} function that will process the received descriptor
    */

    PubsubService.prototype.addSubscription = function addSubscription(skey, pattern, processor, okcallback, errorcallback) {

        console.log("addSubscription " + skey + " : " + JSON.stringify(pattern));

        var proc = function proc(desc) {
            processor(desc);
        };
        console.log("registering processor : " + proc);
        this.processors.set(skey, proc);

        var command = new connection.Pdu();
        command.name = "add_sub";
        command.tkn = util.uniqueId();
        command.key = skey;
        command.desc = pattern;
        session.service.sendCommand(command,
        //ok
        function () {
            okcallback();
        },

        //error
        function (error) {
            if (errorcallback !== undefined) errorcallback(error);
        });
        return;
    };

    /*-----------------------------------------------------------
    * Unregisters a subscription
    * @param {string} skey - the key of the subscription to be unregistered
    */

    PubsubService.prototype.removeSubscription = function removeSubscription(skey, okcallback, errorcallback) {
        var _this2 = this;

        console.log("removeSubscription : " + skey);
        var command = new connection.Pdu();
        command.name = "remove_sub";
        command.tkn = util.uniqueId();
        command.subs = [skey];
        session.service.sendCommand(command,
        //ok
        function () {
            _this2.processors["delete"](skey);
            okcallback();
        },

        //error
        function (error) {
            if (errorcallback !== undefined) errorcallback(error);
        });
    };

    /*-----------------------------------------------------------
    * Gives all the ids of the messages currently in the server's cache that match the subscriptions
    * having the given keys
    * @param {string[]} skeys - the subscription keys. If some keys are unknown, they are ignored
    * @param {function(string[])} okcallback - the callback passing the array of message ids (may be empty)
    * @param {function(string)} [errorcallback] - the callback passing a reason when an error occurred 
    */

    PubsubService.prototype.getMatching = function getMatching(skeys, okcallback, errorcallback) {
        var command = new connection.Pdu();
        command.name = "get_matching";
        command.tkn = util.uniqueId();
        command.subs = skeys;
        console.log("PubSub sending get_matching: " + JSON.stringify(command));
        session.service.sendCommand(command, function (midsPdu) {
            if (midsPdu.name !== "recv_mids") {
                console.log("Strange: response to get_matching is not recv_mids");
                return;
            }
            console.log("PubsSub received recv_mids |mids=" + JSON.stringify(midsPdu.mids) + "|");
            okcallback(midsPdu.mids);
        }, function (error) {
            console.log("Pubsub: Error when executing get_matching");
            if (errorcallback !== undefined) errorcallback(error);
        });
    };

    /*-----------------------------------------------------------
    * Gives the descriptor of a message present in the server's cache
    * @param {string} mid - the id of the message to search for
    * @param {function(Descriptor)} okcallback - the callback passing the searched descriptor
    * @param {function(string)} [errorcallback] - the callback when an error occurred (e.g no such message found)
    */

    PubsubService.prototype.getDescriptor = function getDescriptor(mid, okcallback, errorcallback) {
        var command = new connection.Pdu();
        command.name = "get_desc";
        command.tkn = util.uniqueId();
        command.mid = mid;
        console.log("PubSub sending get_desc: " + JSON.stringify(command));
        session.service.sendCommand(command,
        //ok  
        function (descPdu) {
            if (descPdu.name !== "recv_desc") {
                console.log("Strange: response to get_desc is not recv_desc");
                return;
            }
            console.log("PubsSub received response recv_desc  |desc=" + JSON.stringify(descPdu.desc) + "|");
            okcallback(descPdu.desc);
        },

        // error
        function (error) {
            console.log("Pubsub: Error when executing get_desc");
            if (errorcallback !== undefined) errorcallback(error);
        });
    };

    /*-----------------------------------------------------------
    * Gives the content of a message
    * @param {string} mid - the requested message id
    * @param {function(ArrayBuffer)} okcallback - the callback passing the message payload
    * @param {function(string)} [errorcallback] - the callback when an error occurred
    */

    PubsubService.prototype.getAsBuffer = function getAsBuffer(mid, okcallback, errorcallback) {
        console.log("calling getAsBuffer with mid=" + mid);
        var command = new connection.Pdu();
        command.name = "get_payload";
        command.tkn = util.uniqueId();
        command.mid = mid;
        console.log("PubsSub sending get_payload: " + JSON.stringify(command));
        session.service.sendCommand(command, function (pdu) {
            if (pdu.name !== "recv_payload") {
                console.log("Strange: response to get_payload is not recv_payload");
                return;
            }
            console.log("PubsSub received recv_payload mid=" + pdu.mid);
            if (pdu.hasOwnProperty("expired") && pdu.expired) {
                console.log("PubsSub received recv_payload expired  |mid=" + pdu.mid + "|");
            } else {
                okcallback(pdu.data);
            }
        }, function (error) {
            console.log("Pubsub: Error when executing get_payload");
            if (errorcallback !== undefined) errorcallback(error);
        });
    };

    /*-----------------------------------------------------------
    * Handles the reception of a notification (PDU without token).
    * The only case is the notification of the reception of a message, 
    * that triggers the execution of the associated processor.
    * Note that several such notifications may occur for the same message, 
    * if several subscriptions were set with patterns matching the message.
    * @private
    * @param {Pdu} notifPdu - the received PDU 
    */

    PubsubService.prototype.receiveNotif = function receiveNotif(notifPdu) {
        console.log("PubsSub received notification PDU " + JSON.stringify(notifPdu));

        if (notifPdu.name !== "recv_desc") {
            console.log("Strange: notification is not recv_desc");
            return;
        }

        console.log("PubsSub execute processor for key " + notifPdu.key);

        // Execute the processor that was registered at subscription time
        var proc = this.processors.get(notifPdu.key);
        proc(notifPdu.desc);
    };

    return PubsubService;
})();

var service = new PubsubService();
exports.service = service;

},{"./connection.js":2,"./session.js":5,"./util.js":6}],5:[function(require,module,exports){
/* 
 * This module provides 
 *   - a Session singleton object, named service, that offers methods and callabcks for
 *        . handling the lifecycle of a session 
 *        . sending commands, and receiving responses and notifcations
 *   - Interface SessionListener to receive events related to the status of the session
 */

"use strict";

exports.__esModule = true;

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj["default"] = obj; return newObj; } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var _utilJs = require("./util.js");

var util = _interopRequireWildcard(_utilJs);

var _connectionJs = require("./connection.js");

var connection = _interopRequireWildcard(_connectionJs);

var _configurationJs = require("./configuration.js");

var configuration = _interopRequireWildcard(_configurationJs);

/*
 * Class Session ---------------------------------------------
 */
console.log("Start module session.js");

var Session = (function () {
    function Session() {
        var _this = this;

        _classCallCheck(this, Session);

        // List of subscriptions registered in DoDWAN
        this.subscriptions = [];

        this.client = clientId();

        this.continuation = false;

        this.reconnectPeriod = 15;
        this.reconnectNbTriesMax = 5;

        this.listeners = new Set();

        this.onReceivePubsubNotification = function () {
            console.log("Empty onReceivePubsubNotification");
        };
        this.onReceivePeerNotification = function () {
            console.log("Empty onReceivePeerNotification");
        };

        // Maps of the callbacks to execute when receiving a response or an error,
        // for each token.  A callback should be removed just after its execution
        this.responseCallbacks = new Map();
        this.errorCallbacks = new Map();

        // Handles incoming PDUS (notifications and responses)
        connection.service.setOnReceivePdu(function (pdu) {
            _this.receivePdu(pdu);
        });
    }

    /*-----------------------------------------------------------
       * Get the unique client id, or create if it is the first call.
       * This id is stored in the LocalStorage, that is bound to the browser 
       * and the host from which the javascript application is downloaded. 
       * @private
       */

    /*-----------------------------------------------------------
     * Sets a callback for pubsub notifications
     * @param {function} f - the callback to be registered
     */

    Session.prototype.setOnReceivePubsubNotification = function setOnReceivePubsubNotification(f) {
        this.onReceivePubsubNotification = f;
    };

    /*-----------------------------------------------------------
    * Sets a callback for peer notifications
    * @param {function} f - the callback to be registered
    */

    Session.prototype.setOnReceivePeerNotification = function setOnReceivePeerNotification(f) {
        this.onReceivePeerNotification = f;
    };

    /*-----------------------------------------------------------
     * Registers a session listener, to observe the changes in the status of the session
     * @param {SessionListener} listener - the listener to be registered
     */

    Session.prototype.addListener = function addListener(listener) {
        this.listeners.add(listener);
    };

    /*-----------------------------------------------------------
     * Unregisters a session listener
     * @param {SessionListener} listener - the listener to be unregistered
     */

    Session.prototype.removeListener = function removeListener(listener) {
        this.listeners["delete"](listener);
    };

    /*-----------------------------------------------------------
     * Tries to reconnect periodically to the server
     * @param {string} server - the server address in the form host:port 
     * @private
     */

    Session.prototype.reconnectLater = function reconnectLater(server) {
        this.continuation = true;
        if (this.reconnectNbTriesMax === 0 || this.reconnectPeriod === 0) return;
        this.reconnectNbTries++;
        if (this.reconnectNbTries < this.reconnectNbTriesMax) {
            console.log("Will try to reconnect in " + this.reconnectPeriod + "s (try #" + this.reconnectNbTries + ")");
            window.setTimeout(function () {
                connection.service.connect(server);
            }, this.reconnectPeriod * 1000);
        } else {
            console.log("Give up trying to reconnect");
            for (var _iterator = this.listeners, _isArray = Array.isArray(_iterator), _i = 0, _iterator = _isArray ? _iterator : _iterator[Symbol.iterator]();;) {
                var _ref;

                if (_isArray) {
                    if (_i >= _iterator.length) break;
                    _ref = _iterator[_i++];
                } else {
                    _i = _iterator.next();
                    if (_i.done) break;
                    _ref = _i.value;
                }

                var listener = _ref;
                listener.onSessionStopped();
            }
        }
    };

    /*-----------------------------------------------------------
    * Closes the session. All subscriptions are forgotten and the connection is closed
    */

    Session.prototype.bye = function bye() {
        this.subscriptions = [];
        connection.service.disconnect();
        for (var _iterator2 = this.listeners, _isArray2 = Array.isArray(_iterator2), _i2 = 0, _iterator2 = _isArray2 ? _iterator2 : _iterator2[Symbol.iterator]();;) {
            var _ref2;

            if (_isArray2) {
                if (_i2 >= _iterator2.length) break;
                _ref2 = _iterator2[_i2++];
            } else {
                _i2 = _iterator2.next();
                if (_i2.done) break;
                _ref2 = _i2.value;
            }

            var listener = _ref2;
            listener.onSessionStopped();
        }
    };

    /*-----------------------------------------------------------
    * Asks to be ready to publish and receive notifications: 
    * open a connection, and when it is opened, tells the server to open a session, and start notifications
    */

    Session.prototype.start = function start() {
        var _this2 = this;

        var server = configuration.service.config.server_host + ":" + configuration.service.config.server_port;
        this.reconnectPeriod = configuration.service.config.reconnect_period;
        this.reconnectNbTriesMax = configuration.service.config.reconnect_nb_tries_max;

        // Begin in the starting state
        for (var _iterator3 = this.listeners, _isArray3 = Array.isArray(_iterator3), _i3 = 0, _iterator3 = _isArray3 ? _iterator3 : _iterator3[Symbol.iterator]();;) {
            var _ref3;

            if (_isArray3) {
                if (_i3 >= _iterator3.length) break;
                _ref3 = _iterator3[_i3++];
            } else {
                _i3 = _iterator3.next();
                if (_i3.done) break;
                _ref3 = _i3.value;
            }

            var listener = _ref3;
            listener.onSessionStarting();
        }connection.service.setOnSocketError(function (error) {
            for (var _iterator4 = _this2.listeners, _isArray4 = Array.isArray(_iterator4), _i4 = 0, _iterator4 = _isArray4 ? _iterator4 : _iterator4[Symbol.iterator]();;) {
                var _ref4;

                if (_isArray4) {
                    if (_i4 >= _iterator4.length) break;
                    _ref4 = _iterator4[_i4++];
                } else {
                    _i4 = _iterator4.next();
                    if (_i4.done) break;
                    _ref4 = _i4.value;
                }

                var listener = _ref4;
                listener.onConnectionError(error);
            }_this2.reconnectLater(server);
        });

        connection.service.setOnSocketClose(function () {
            for (var _iterator5 = _this2.listeners, _isArray5 = Array.isArray(_iterator5), _i5 = 0, _iterator5 = _isArray5 ? _iterator5 : _iterator5[Symbol.iterator]();;) {
                var _ref5;

                if (_isArray5) {
                    if (_i5 >= _iterator5.length) break;
                    _ref5 = _iterator5[_i5++];
                } else {
                    _i5 = _iterator5.next();
                    if (_i5.done) break;
                    _ref5 = _i5.value;
                }

                var listener = _ref5;
                listener.onConnectionClosed();
            }_this2.reconnectLater(server);
        });

        connection.service.setOnSocketOpen(function () {

            for (var _iterator6 = _this2.listeners, _isArray6 = Array.isArray(_iterator6), _i6 = 0, _iterator6 = _isArray6 ? _iterator6 : _iterator6[Symbol.iterator]();;) {
                var _ref6;

                if (_isArray6) {
                    if (_i6 >= _iterator6.length) break;
                    _ref6 = _iterator6[_i6++];
                } else {
                    _i6 = _iterator6.next();
                    if (_i6.done) break;
                    _ref6 = _i6.value;
                }

                var listener = _ref6;
                listener.onConnectionOpened();
            } // Opens a session
            var command = new connection.Pdu();
            command.name = "hello";
            command.client = _this2.client;
            command.cont = _this2.continuation;
            command.tkn = util.uniqueId();
            _this2.sendCommand(command, function () {
                // OK : authorize traffic to and from DoDWAN
                var command = new connection.Pdu();
                command.name = "start";
                command.tkn = util.uniqueId();
                _this2.sendCommand(command, function (okPdu) {
                    console.log("Traffic to/from DoDWAN authorized");
                    for (var _iterator7 = _this2.listeners, _isArray7 = Array.isArray(_iterator7), _i7 = 0, _iterator7 = _isArray7 ? _iterator7 : _iterator7[Symbol.iterator]();;) {
                        var _ref7;

                        if (_isArray7) {
                            if (_i7 >= _iterator7.length) break;
                            _ref7 = _iterator7[_i7++];
                        } else {
                            _i7 = _iterator7.next();
                            if (_i7.done) break;
                            _ref7 = _i7.value;
                        }

                        var listener = _ref7;
                        listener.onSessionStarted();
                    }
                }, function (error) {
                    console.log("Strange error when authorizing traffic :" + error);
                    bye(); // error considered fatal
                });
            }, function (error) {
                console.log("Strange error when opening session : " + error);
                bye(); // error considered fatal
            });
        });

        // Initial connection
        connection.service.connect(server);
    };

    /*-----------------------------------------------------------
    * Stops the traffic from/to DoDWAN
    */

    Session.prototype.stop = function stop() {
        var _this3 = this;

        var command = new connection.Pdu();
        command.name = "stop";
        command.tkn = util.uniqueId();
        sendCommand(command, function (response) {
            console.log("Traffic to/from DoDWAN blocked");
            for (var _iterator8 = _this3.listeners, _isArray8 = Array.isArray(_iterator8), _i8 = 0, _iterator8 = _isArray8 ? _iterator8 : _iterator8[Symbol.iterator]();;) {
                var _ref8;

                if (_isArray8) {
                    if (_i8 >= _iterator8.length) break;
                    _ref8 = _iterator8[_i8++];
                } else {
                    _i8 = _iterator8.next();
                    if (_i8.done) break;
                    _ref8 = _i8.value;
                }

                var listener = _ref8;
                listener.onSessionStopped();
            }
        }, function (error) {
            console.log("Strange error when blocking traffic :" + error);
            bye(); // error considered fatal
        });
    };

    /*-----------------------------------------------------------
     * Sends a command PDU
     * @param {Pdu} pdu - The PDU to be sent
     * @param {function(Pdu)} responseCallback - the function passing the response to call when all goes well 
     * @param {function(string)} errorCallback - the function passing the error reason when an error occurs 
     */

    Session.prototype.sendCommand = function sendCommand(pdu, responseCallback, errorCallback) {
        if (pdu.tkn) {
            if (responseCallback) this.responseCallbacks.set(pdu.tkn, responseCallback);
            if (errorCallback) this.errorCallbacks.set(pdu.tkn, errorCallback);
        }
        connection.service.sendPdu(pdu);
        return;
    };

    //////////////////////////////////////////////////////////////////////////////////////
    // Private methods
    //////////////////////////////////////////////////////////////////////////////////////

    /*-----------------------------------------------------------
     * Dispatch the received PDU to the good notifier
     * @param {Pdu} pdu - The received PDU
     * @private
     */

    Session.prototype.receivePdu = function receivePdu(pdu) {
        if (pdu.tkn) {
            this.receiveResponse(pdu);
        } else {
            this.receiveNotification(pdu);
        }
    };

    /*-----------------------------------------------------------
     * Handles a response PDU: triggers the ok or error callback
     * corresponding to the command, according to the token present in the PDU
     * @param {Pdu} pdu - The received response PDU
     * @private
     */

    Session.prototype.receiveResponse = function receiveResponse(pdu) {
        if (pdu.name === "error") {
            this.errorCallbacks.get(pdu.tkn)(pdu.reason);
            this.errorCallbacks["delete"](pdu.tkn);
        } else {
            // any other name, including ok
            this.responseCallbacks.get(pdu.tkn)(pdu);
            this.responseCallbacks["delete"](pdu.tkn);
        }
    };

    /*-----------------------------------------------------------
     * Dispatch a notification PDU to the right pre-registered notifier:
     * (pubsub or peer notifier)
     * @param {Pdu} pdu - The received notification PDU
     * @private
     */

    Session.prototype.receiveNotification = function receiveNotification(pdu) {

        if (pdu.name === "recv_desc") {
            console.log("Session received pubsub notification PDU " + JSON.stringify(pdu));
            this.onReceivePubsubNotification(pdu);
        } else if (pdu.name === "add_peer" || pdu.name === "remove_peer" || pdu.name === "clear_peers") {
            this.onReceivePeerNotification(pdu);
        } else {
            console.log("Unknown notification " + pdu.name);
        }
    };

    return Session;
})();

function clientId() {
    var id = window.localStorage.getItem("clientId");
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

var SessionListener = function SessionListener(onConnectionOpened, onConnectionClosed, onConnectionError, onSessionStarting, onSessionStarted, onSessionStopped) {
    _classCallCheck(this, SessionListener);

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

// Singleton
;

exports.SessionListener = SessionListener;
var service = new Session();
exports.service = service;

},{"./configuration.js":1,"./connection.js":2,"./util.js":6}],6:[function(require,module,exports){
"use strict";

exports.__esModule = true;
exports.uniqueId = uniqueId;
exports.uniqueLocalId = uniqueLocalId;
exports.b64ByteLength = b64ByteLength;
exports.b64ToUint8Array = b64ToUint8Array;
exports.uint8ArrayToB64 = uint8ArrayToB64;
exports.stringToUtf8Array = stringToUtf8Array;
exports.utf8ArrayToString = utf8ArrayToString;
exports.stringToB64 = stringToB64;
exports.b64ToString = b64ToString;

console.log("Start module util.js");

/**
 * Produces a string of digits that is (almost) unique across browsers and time
 * Two different browsers cannot generated the same id (at least it is very improbable).
 * In addition, successive calls to the function will produces different ids 
 * (even after browser shutdown).
 * @return {String}
 */

function uniqueId() {
  var nav = window.navigator;
  var screen = window.screen;
  var numid = "";
  numid += nav.mimeTypes.length;
  //id += nav.userAgent.replace(/\D+/g, "");
  numid += nav.plugins.length;
  numid += screen.height || "";
  //id += screen.width || "";
  //id += screen.pixelDepth || "";

  numid += uniqueLocalId();
  return numid;
}

;

var counter = 0;
/**
 * Produces a string of digits that is unique along time, even across browser shutdowns.
 * We simply take the number of ms since 01/01/2018, 
 * concatenated with a counter, just to ensure that multiple calls in the same ms yield different ids
 * @return {String}
 */

function uniqueLocalId() {
  var numid = "";
  var epoch_01_01_2018 = 1514761200000; // nb ms since EPOCH on 01/01/2018
  numid += Date.now() - epoch_01_01_2018;
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

function placeHoldersCount(b64) {
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

function b64ByteLength(b64) {
  // base64 is 4/3 + up to two characters of the original data
  return b64.length * 3 / 4 - placeHoldersCount(b64);
}

function b64ToUint8Array(b64) {
  var i, l, tmp, placeHolders, arr;
  var len = b64.length;
  placeHolders = placeHoldersCount(b64);

  arr = new Uint8Array(len * 3 / 4 - placeHolders);

  // if there are placeholders, only get up to the last complete 4 chars
  l = placeHolders > 0 ? len - 4 : len;

  var L = 0;

  for (i = 0; i < l; i += 4) {
    tmp = revLookup[b64.charCodeAt(i)] << 18 | revLookup[b64.charCodeAt(i + 1)] << 12 | revLookup[b64.charCodeAt(i + 2)] << 6 | revLookup[b64.charCodeAt(i + 3)];
    arr[L++] = tmp >> 16 & 0xFF;
    arr[L++] = tmp >> 8 & 0xFF;
    arr[L++] = tmp & 0xFF;
  }

  if (placeHolders === 2) {
    tmp = revLookup[b64.charCodeAt(i)] << 2 | revLookup[b64.charCodeAt(i + 1)] >> 4;
    arr[L++] = tmp & 0xFF;
  } else if (placeHolders === 1) {
    tmp = revLookup[b64.charCodeAt(i)] << 10 | revLookup[b64.charCodeAt(i + 1)] << 4 | revLookup[b64.charCodeAt(i + 2)] >> 2;
    arr[L++] = tmp >> 8 & 0xFF;
    arr[L++] = tmp & 0xFF;
  }

  return arr;
}

function tripletToBase64(num) {
  return lookup[num >> 18 & 0x3F] + lookup[num >> 12 & 0x3F] + lookup[num >> 6 & 0x3F] + lookup[num & 0x3F];
}

function encodeChunk(uint8, start, end) {
  var tmp;
  var output = [];
  for (var i = start; i < end; i += 3) {
    tmp = (uint8[i] << 16 & 0xFF0000) + (uint8[i + 1] << 8 & 0xFF00) + (uint8[i + 2] & 0xFF);
    output.push(tripletToBase64(tmp));
  }
  return output.join('');
}

function uint8ArrayToB64(uint8) {
  var tmp;
  var len = uint8.length;
  var extraBytes = len % 3; // if we have 1 byte left, pad 2 bytes
  var output = '';
  var parts = [];
  var maxChunkLength = 16383; // must be multiple of 3

  // go through the array every three bytes, we'll deal with trailing stuff later
  for (var i = 0, len2 = len - extraBytes; i < len2; i += maxChunkLength) {
    parts.push(encodeChunk(uint8, i, i + maxChunkLength > len2 ? len2 : i + maxChunkLength));
  }

  // pad the end with zeros, but make sure to not forget the extra bytes
  if (extraBytes === 1) {
    tmp = uint8[len - 1];
    output += lookup[tmp >> 2];
    output += lookup[tmp << 4 & 0x3F];
    output += '==';
  } else if (extraBytes === 2) {
    tmp = (uint8[len - 2] << 8) + uint8[len - 1];
    output += lookup[tmp >> 10];
    output += lookup[tmp >> 4 & 0x3F];
    output += lookup[tmp << 2 & 0x3F];
    output += '=';
  }

  parts.push(output);

  return parts.join('');
}

function stringToUtf8Array(str) {
  var te = new TextEncoder("utf-8");
  return te.encode(str);
}

function utf8ArrayToString(array) {
  var td = new TextDecoder("utf-8");
  return td.decode(array);
}

function stringToB64(str) {
  return uint8ArrayToB64(stringToUtf8Array(str));
}

function b64ToString(b64) {
  return utf8ArrayToString(b64ToUint8Array(b64));
}

/*
 * Computes the basename of a filename
 * @param {type} str the filename
 * @return {String} the substring after the last /
 */
function baseName(str) {
  var lastSlash = str.lastIndexOf('/');
  if (lastSlash === -1) return str;
  if (lastSlash === str.length - 1) return "";
  return str.slice(lastSlash, str.length);
}

},{}],7:[function(require,module,exports){
/* 
 * GUI for the Webopp application
 */

"use strict";

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj["default"] = obj; return newObj; } }

var _configurationJs = require("./configuration.js");

var configuration = _interopRequireWildcard(_configurationJs);

var _pubsubJs = require("./pubsub.js");

var pubsub = _interopRequireWildcard(_pubsubJs);

var _messageJs = require("./message.js");

var message = _interopRequireWildcard(_messageJs);

var _sessionJs = require("./session.js");

var session = _interopRequireWildcard(_sessionJs);

var _peerJs = require("./peer.js");

var peer = _interopRequireWildcard(_peerJs);

var _utilJs = require("./util.js");

var util = _interopRequireWildcard(_utilJs);

console.log("Start module dom.js");

var sessionStatus = {
    STOPPED: 'stopped',
    STARTING: 'starting',
    STARTED: 'started'
};

window.onunload = quitApplication;

checkUniqueWindow();

restoreLists();

// Initialize main button handlers
document.getElementById("pub-collapse").onclick = clickPubCollapse;
document.getElementById("pub-button").onclick = clickPublish;
document.getElementById("pub-add-row").onclick = clickAddPubDescRow;

document.getElementById("subs-collapse").onclick = clickSubsCollapse;
document.getElementById("subs-add-row").onclick = clickAddSubsDescRow;
document.getElementById("subs-button").onclick = clickSubscribe;

document.getElementById("recep-collapse").onclick = clickRecepCollapse;

document.getElementById("hamburger").onclick = clickHamburger;
document.getElementById("mi-clear-pub-list").onclick = clearPubList;
document.getElementById("mi-clear-subs-list").onclick = clearSubsList;
document.getElementById("mi-clear-recep-list").onclick = clearRecepList;
document.getElementById("mi-clear-all").onclick = clearAllAndQuit;

document.getElementById("connect-status").onclick = function () {
    window.location.reload();
};
document.getElementById("connect-address").onkeydown = function (event) {
    if (event.keyCode === 13) clickConnect();
};

// Clear Inputs
clearPubInputs();
clearSubsInputs();

// Listeners for peers and session events

peer.service.addListener(new peer.PeerListener(addPeer, removePeer, clearPeers));

session.service.addListener(new session.SessionListener(onConnectionOpened, onConnectionClosed, onConnectionError, onSessionStarting, onSessionStarted, onSessionStopped));

// Status handlers

function onSessionStarted() {
    setSessionStatus(sessionStatus.STARTED);

    peer.service.getPeers(addPeers);
    peer.service.getMyPeerName(setMyPeer);

    message.service.askForMissedMessages(addRecepListElt);

    activatePubsubButtons(true);
}

function onSessionStopped() {
    setSessionStatus(sessionStatus.STOPPED);
    activatePubsubButtons(false);
}

function onSessionStarting() {
    setSessionStatus(sessionStatus.STARTING);
    activatePubsubButtons(false);
}

function onConnectionError() {}
function onConnectionOpened() {}
function onConnectionClosed() {}

// Be ready to receive

var serverHost = window.localStorage.getItem("server_host");
var serverPort = window.localStorage.getItem("server_port");

if (serverHost === null) {
    serverHost = window.location.hostname;
    window.localStorage.setItem("server_host", serverHost);
}

if (serverPort === null) {
    serverPort = "8025";
    window.localStorage.setItem("server_port", serverPort);
}

configuration.service.config.server_host = serverHost;
configuration.service.config.server_port = serverPort;
document.getElementById("connect-address").value = serverHost + ":" + serverPort;

session.service.start();

// Indicates that this window is the first that has launched the app
// (and has created the lock)
var IAmTheFirst;

/*
 * Checks if this is the sole application window
 * This is done with a lock item in the local storage.
 * As the lock may not have been released properly, 
 * we ask the user to confirm that the lock should be overcome or not.
 */
function checkUniqueWindow() {
    //console.log("IAmTheFirst=" + IAmTheFirst);
    var lock = window.localStorage.getItem("appLock");
    //console.log("lock=" + lock);
    if (lock === "lock") {
        IAmTheFirst = false;
        var result = window.confirm("The Webopp application seems to be already running in another window or tab." + " Click OK if you want to continue anyway with this tab");
        if (!result) {
            //console.log("I give up. Clear the window.");
            document.body.innerHTML = "";
        } else {
            //console.log("I insist.");
        }
    } else {
            IAmTheFirst = true;
            addLock();
        }
}

function addLock() {
    console.log("add lock");
    window.localStorage.setItem("appLock", "lock");
}

function removeLock() {
    console.log("remove lock");
    window.localStorage.removeItem("appLock");
}

function quitApplication() {
    console.log("Quit application  [lock=" + window.localStorage.getItem("AppLock") + "]");
    //console.log("Quitting Webopp [IAmTheFirst = "+IAmTheFirst+"]");
    if (IAmTheFirst) {
        removeLock();
    }
}

// =========================== Displaying peers ================================

function setMyPeer(peer) {
    console.log("setMyPeerId " + peer);
    var myIdContent = document.getElementById("my-id-content");
    myIdContent.innerHTML = peer;
}

function addPeers(peers) {
    for (var i in peers) {
        console.log("PubsSub adding initial peer " + peers[i]);
        addPeer(peers[i]);
    }
}

function addPeer(peer) {
    var peerContent = document.getElementById("peer-content");
    var newElt = document.createElement("SPAN");
    newElt.setAttribute("id", "peerid_" + peer);
    newElt.setAttribute("class", "peer");
    newElt.innerHTML = peer;
    peerContent.appendChild(newElt);
}

function removePeer(peer) {
    var elt = document.getElementById("peerid_" + peer);
    if (elt === null) {
        console.log("Peer " + peer + " cannot be removed");
        return;
    }
    elt.remove();
}

function clearPeers() {
    var peerContent = document.getElementById("peer-content");
    peerContent.innerHTML = "";
}

// ================ Current descriptor for publishing and subscribing ==========

var pubDesc = new pubsub.Descriptor();
var subsDesc = new pubsub.Descriptor();

/*
* Clears the current publish descriptor
*/
function clearPubDesc() {
    pubDesc = new pubsub.Descriptor();
}

/*
 * Clears the current subscription descriptor
 */
function clearSubsDesc() {
    subsDesc = new pubsub.Descriptor();
}

// ================ Restoration from Local Storage ======================
/*
 * Rebuild from the storage and display the lists of 
 * published messages, set subscriptions and received messages
 */
function restoreLists() {
    message.service.restorePersistedLists(addPubListElt, addSubsListElt, addRecepListElt);
}

function clearPubList() {
    message.service.clearPersistedPubList;
    document.getElementById("pub-list").innerHTML = "";
}

function clearSubsList() {
    message.service.clearPersistedSubsList();
    document.getElementById("subs-list").innerHTML = "";
}

function clearRecepList() {
    message.service.clearPersistedRecepList();
    document.getElementById("recep-list").innerHTML = "";
}

function clearAllAndQuit() {
    session.service.bye();
    window.localStorage.clear();
    window.onclick = function () {};
    document.body.innerHTML = "Bye bye...";
}

// ========================= Status Bar ==================================

function setSessionStatus(status) {
    console.log("setSessionStatus : " + status);
    var elt = document.getElementById("connect-status");
    switch (status) {
        case sessionStatus.STARTED:
            elt.style.color = "limegreen";break;
        case sessionStatus.STARTING:
            elt.style.color = "orange";break;
        case sessionStatus.STOPPED:
            elt.style.color = "firebrick";break;
    }
}

// ================ Main  Button Clicks ==================================

function clickPublish() {

    // Invalidate the button
    activatePubButton(false);

    // Checks if the descriptor is unfinished
    if (document.getElementById("pub-input-key").value !== "") {
        addPubDescRow();
        //console.log("Publish descriptor completed with the input line");
    }

    // Build the out message
    var typedText = document.getElementById("pub-textpayload-input").value.trim();
    var files = document.getElementById("pub-file").files;
    var chosenFile = files.length === 0 ? null : files[0];

    // Checks if the message is empty (desc and payload empty)
    if (Object.keys(pubDesc).length === 0 && typedText === "" && chosenFile === null) {
        alert("Cannot publish an empty message");
        activatePubButton(true);
        return;
    }

    var msg = new message.Message();
    msg.inout = "out";
    msg.mid = util.uniqueId();
    msg.desc = pubDesc;
    // The text type is privileged, i.e., if both a chosen file and a typed text
    // are provided, the message will be a text message.
    // The message payload may be eventually empty (descriptor-only message)
    msg.ptype = typedText !== "" || chosenFile === null ? "text" : "file";

    // Complete the message wih its payload and publish it.
    if (msg.ptype === "text") {
        msg.desc._webopp_ptype = "text";
        //console.log("Publish data from text: size=" + typedText);
        msg.payload = util.stringToB64(typedText);
        message.service.publish(msg, afterPublish);
    } else {
        (function () {
            msg.desc._webopp_ptype = "file";
            msg.desc._webopp_pmime = chosenFile.type;
            msg.desc._webopp_file = chosenFile.name;

            var reader = new FileReader();

            reader.onload = function () {
                //console.log("reader.result= " + reader.result);
                //console.log("Loaded " + chosenFile.name);
                console.log("Publish data from file: size=" + reader.result.byteLength);
                msg.payload = util.uint8ArrayToB64(new Uint8Array(reader.result));
                message.service.publish(msg, afterPublish);
            };

            // Read in the image file as byte buffer.
            reader.readAsArrayBuffer(chosenFile);
        })();
    }
}

/*
 * Postlude to a publish
 */
function afterPublish(msg) {
    //console.log("AfterPublish "+ JSON.stringify(msg));

    // Empty the payload and desc
    clearPubInputs();
    clearPubDesc();
    removeDescAllRows(document.getElementById("pub-desc-tab"));

    // Add the msg to the HTML publish list
    addPubListElt(msg);

    // Revalidate the button
    activatePubButton(true);
}

function clickAddPubDescRow() {
    addPubDescRow();
    document.getElementById("pub-input-key").focus();
}

function clickAddSubsDescRow() {
    addSubsDescRow();
    document.getElementById("subs-input-key").focus();
}

/*
 * Action associated with the Subscribe button
 */
function clickSubscribe() {

    // Checks if the descriptor is unfinished
    if (document.getElementById("subs-input-key").value !== "") {
        clickAddSubsDescRow();
        //console.log("Descriptor completed with the input line");
    }

    // The current subscription descriptor should not be empty
    if (Object.keys(subsDesc).length === 0) {
        alert("Cannot make a subscription from an empty descriptor");
        return;
    }

    // Make the subscription, and get the created subscription key
    message.service.subscribe(subsDesc, addRecepListElt, afterSubscribe);
}

/*
 * Postlude to a subscribe
 */
function afterSubscribe(skey) {
    console.log("New subscription : " + skey);

    // Add the subscription to the displayed list
    addSubsListElt(skey, subsDesc);

    // Remove the rows of the descriptor table
    removeDescAllRows(document.getElementById("subs-desc-tab"));

    // Clear the current subscription descriptor
    clearSubsDesc();

    clearSubsInputs();
}

function clickHamburger() {
    if (menuVisible()) hideMenu();else showMenu();
}

// A click outside the hamburger hides the menu
window.onclick = function (event) {
    if (!event.target.matches(".hamburger")) {
        hideMenu();
    }
};

function menuVisible() {
    return document.getElementById("main-menu").style.display === "block";
}

function showMenu() {
    document.getElementById("main-menu").style.display = "block";
}

function hideMenu() {
    document.getElementById("main-menu").style.display = "none";
}

function clickConnect() {
    var server = document.getElementById("connect-address").value.trim();
    var indexSemiColon = server.indexOf(":");
    if (indexSemiColon === -1) return;
    window.localStorage.setItem("server_host", server.substring(0, indexSemiColon));
    window.localStorage.setItem("server_port", server.substring(indexSemiColon + 1));
    window.location.reload();
}

function clickPubCollapse() {
    toggleButtonText(this);
    toggleCollapse([document.getElementById("pub-content"), document.getElementById("pub-button")]);
}

function clickSubsCollapse() {
    toggleButtonText(this);
    toggleCollapse([document.getElementById("subs-content"), document.getElementById("subs-button")]);
}

function clickRecepCollapse() {
    toggleButtonText(this);
    toggleCollapse(document.getElementById("recep-content"));
}

function toggleButtonText(btn) {
    if (btn.firstChild.data === "▲") {
        btn.firstChild.data = "▼";
    } else {
        btn.firstChild.data = "▲";
    }
}

// Used to memorize the states of the CSS display property of collapsible sections
var savedDisplays = {};

function toggleCollapse(elements) {
    elements = elements.length ? elements : [elements];

    for (var i = 0; i < elements.length; i++) {
        var elt = elements[i];
        var computedDisplay = window.getComputedStyle(elt, null).getPropertyValue('display');
        if (computedDisplay === "none") {
            elt.style.display = savedDisplays[elt.id];
        } else {
            savedDisplays[elt.id] = computedDisplay;
            elt.style.display = "none";
        }
    }
}

/*
 * Activate or deactivate buttons for publishing and subscribing
 */
function activatePubsubButtons(active) {
    activatePubButton(active);
    activateSubButton(active);
}

function activatePubButton(active) {
    document.getElementById("pub-button").disabled = !active;
}

function activateSubButton(active) {
    document.getElementById("subs-button").disabled = !active;
}

// =======================================================================

function addPubDescRow() {
    addDescRow(document.getElementById("pub-input-key"), document.getElementById("pub-input-value"), pubDesc, document.getElementById("pub-desc-tab"));
}

function addSubsDescRow() {
    addDescRow(document.getElementById("subs-input-key"), document.getElementById("subs-input-value"), subsDesc, document.getElementById("subs-desc-tab"));
}

/* 
 * Adds a row both in a descriptor object and in an HTML div.
 * The source of the key, value are two HTML inputs.
 */
function addDescRow(keyInput, valueInput, desc, div) {
    //console.log('addDescRow ' + keyInput.value + "  " + valueInput.value + "  " + JSON.stringify(desc) + "  "+ div);

    var newKey = keyInput.value.trim();
    var newValue = valueInput.value.trim();

    if (newKey === "") {
        alert("Please type in a key before adding a new one");
        return;
    }

    // Update the descriptor
    var ok = desc.setAttribute(newKey, newValue);
    if (!ok) {
        alert("Key already present");
        return;
    }

    // Append the couple to the pub desc element
    var row = document.createElement("div");
    row.setAttribute("class", "desc-row");

    var col1 = document.createElement("div");
    col1.innerHTML += newKey;
    col1.setAttribute("class", "keycell");
    row.appendChild(col1);

    var col2 = document.createElement("div");
    col2.innerHTML += newValue;
    col2.setAttribute("class", "valuecell");
    row.appendChild(col2);

    var buttonNode = document.createElement("button");
    buttonNode.setAttribute("class", "buttoncell");
    var textNode = document.createTextNode("–");
    buttonNode.appendChild(textNode);
    buttonNode.onclick = function () {
        removeDescRow(newKey, desc, row);
    };
    buttonNode.appendChild(textNode);
    row.appendChild(buttonNode);

    div.appendChild(row);

    // Clear the input elements
    keyInput.value = "";
    valueInput.value = "";
}

/*
 * Remove a couple key/value in a descriptor 
 * and the corresponding elt
*  @param {string} key  
*  @param {Descriptor} desc
 * @param {Element} elt 
 */
function removeDescRow(key, desc, elt) {
    //console.log("removeDescRow for key:" + key);
    elt.remove();
    delete desc[key];
}

/*
 * Removes all the rows of a desc element
 */
function removeDescAllRows(elt) {
    elt.innerHTML = "";
}

function clearPubInputs() {
    document.getElementById("pub-input-key").value = "";
    document.getElementById("pub-input-value").value = "";
    document.getElementById("pub-textpayload-input").value = "";
    document.getElementById("pub-file").value = "";
}

function clearSubsInputs() {
    document.getElementById("subs-input-key").value = "";
    document.getElementById("subs-input-value").value = "";
}

/*
 * Adds a div (a list-elt) at the beginning of the subs-list. This div contains 
 *  - a pseudo-table filled with the couples of a descriptor
 *  - an unsubscribe button 
 * @param {String} skey
 * @param {Descriptor} desc
 */
function addSubsListElt(skey, desc) {
    //console.log("addSubsListElt "+ skey + " "+ desc.toJson());

    var eltNode = document.createElement("div");
    eltNode.setAttribute("class", "list-elt");

    // Fill the table and add it to the div
    var eltTabNode = document.createElement("div");
    eltTabNode.setAttribute("class", "list-elt-tab");

    for (var _iterator = Object.entries(desc), _isArray = Array.isArray(_iterator), _i = 0, _iterator = _isArray ? _iterator : _iterator[Symbol.iterator]();;) {
        var _ref;

        if (_isArray) {
            if (_i >= _iterator.length) break;
            _ref = _iterator[_i++];
        } else {
            _i = _iterator.next();
            if (_i.done) break;
            _ref = _i.value;
        }

        var k = _ref[0];
        var v = _ref[1];

        // Append the couple to the pub desc element
        var row = document.createElement("div");
        row.setAttribute("class", "desc-row");

        var col1 = document.createElement("div");
        col1.innerHTML += k;
        col1.setAttribute("class", "keycell");
        row.appendChild(col1);

        var col2 = document.createElement("div");
        col2.innerHTML += desc[k];
        col2.setAttribute("class", "valuecell");
        row.appendChild(col2);

        eltTabNode.appendChild(row);
    }
    eltNode.appendChild(eltTabNode);

    // Add to the div the unsusbcribe button associated with this table
    var buttonNode = document.createElement("button");
    buttonNode.setAttribute("class", "list-elt-button");
    var textNode = document.createTextNode("–");
    buttonNode.appendChild(textNode);
    buttonNode.onclick = function () {
        clickUnsubscribe(skey, eltNode);
    };
    eltNode.appendChild(buttonNode);

    // Insert the div into the list
    insertFirst(eltNode, document.getElementById("subs-list"));
}

/*
 * Action associated with the Unsubscribe button
 *  
 * @return {undefined}
 */
function clickUnsubscribe(skey, elt) {
    //console.log("clickUnsubscribe  key="+skey + "  elt="+elt);
    message.service.unsubscribe(skey, function () {
        removeListElt(elt);
    });
}

/*
 * Adds a message to the HTML reception list
 * @param {Message} msg
 */
function addRecepListElt(msg) {
    addMessageListElt(msg, document.getElementById("recep-list"));
}

/*
 * Adds a message to the HTML publication list
 * @param {Message} msg
 */
function addPubListElt(msg) {
    addMessageListElt(msg, document.getElementById("pub-list"));
}

/*
 * Adds a div (a list-elt) at the beginning of a message list. This div contains 
 * information taken from a Message object:
 *  - a div that contains a date
 *  - a table filled with the couples of a descriptor
 *  - a div that contains the text of the message, or a button to open a file
 * @param {Message} msg
 * @param {Element} listElt  the message list as an HTML Element
 */
function addMessageListElt(msg, listElt) {
    //console.log("addRecepListElt desc="+ JSON.stringify(msg.desc));
    //console.log("addRecepListElt payload="+ msg.payload);

    var eltNode = document.createElement("div");
    eltNode.setAttribute("class", "recep-list-elt");

    var dateNode = document.createElement("div");
    var textNode = document.createTextNode(msg.date);
    dateNode.setAttribute("class", "date");
    dateNode.appendChild(textNode);
    eltNode.appendChild(dateNode);

    // Fill the table and add it to the div
    var eltTabNode = document.createElement("div");
    eltTabNode.setAttribute("class", "recep-list-tab");
    for (var _iterator2 = Object.entries(msg.desc), _isArray2 = Array.isArray(_iterator2), _i2 = 0, _iterator2 = _isArray2 ? _iterator2 : _iterator2[Symbol.iterator]();;) {
        var _ref2;

        if (_isArray2) {
            if (_i2 >= _iterator2.length) break;
            _ref2 = _iterator2[_i2++];
        } else {
            _i2 = _iterator2.next();
            if (_i2.done) break;
            _ref2 = _i2.value;
        }

        var k = _ref2[0];
        var v = _ref2[1];

        if (!k.startsWith("_")) {
            // Append the couple to the pub desc element
            var row = document.createElement("div");
            row.setAttribute("class", "desc-row");

            var col1 = document.createElement("div");
            col1.innerHTML += k;
            col1.setAttribute("class", "keycell");
            row.appendChild(col1);

            var col2 = document.createElement("div");
            col2.innerHTML += v;
            col2.setAttribute("class", "valuecell");
            row.appendChild(col2);

            eltTabNode.appendChild(row);
        }
    }
    eltNode.appendChild(eltTabNode);

    var payloadNode = document.createElement("div");

    if (msg.ptype === "text") {
        payloadNode.setAttribute("class", "recep-payload-text");
        payloadNode.innerHTML = util.b64ToString(msg.payload);
    } else if (msg.ptype === "file") {
        var linkNode = document.createElement("a");

        var b64 = util.b64ToUint8Array(msg.payload);
        var blob = new Blob([b64], { type: msg.desc._webopp_pmime });

        linkNode.href = window.URL.createObjectURL(blob);
        linkNode.download = msg.desc._webopp_file;
        linkNode.text = ellipside(msg.desc._webopp_file, 20);

        var buttonSaveNode = document.createElement("button");
        buttonSaveNode.setAttribute("type", "button");
        buttonSaveNode.setAttribute("class", "file-button");
        buttonSaveNode.appendChild(linkNode);

        payloadNode.setAttribute("class", "recep-payload-file");
        payloadNode.appendChild(buttonSaveNode);
    }

    eltNode.appendChild(payloadNode);

    insertFirst(eltNode, listElt);
}

/*
 * Shortens too long strings by replacing the middle of the string by ellipsis.
 * Short-enough strings are returned unchanged.
 * @param {type} str
 * @param {type} maxlen the number of characters preserved from the original string
 *               (hence the return string has at most maxlen +1 characaters)
 * @return {Function}
 */
function ellipside(str, maxlen) {
    var strlen = str.length;
    if (strlen - maxlen < 0) return str;
    var firstpartlen = maxlen / 2;
    var lastpartlen = firstpartlen;
    if (strlen % 2 !== 0) firstpartlen++;
    return str.substring(0, firstpartlen) + "…" + str.substring(strlen - lastpartlen);
}

/*
 * Inserts an element as the first child of a parent
 * @param {Element} elt
 * @param {Element} parent
 * @return {undefined}
 */
function insertFirst(elt, parent) {
    if (parent.hasChildNodes()) {
        parent.insertBefore(elt, parent.firstElementChild);
    } else {
        parent.appendChild(elt);
    }
}

/*
 *  * Removes an element from a HTML list
 * @param {Element} elt
 * @return {undefined}
 */
function removeListElt(elt) {
    elt.remove();
}

},{"./configuration.js":1,"./message.js":8,"./peer.js":3,"./pubsub.js":4,"./session.js":5,"./util.js":6}],8:[function(require,module,exports){
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

"use strict";

exports.__esModule = true;

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj["default"] = obj; return newObj; } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var _utilJs = require("./util.js");

var util = _interopRequireWildcard(_utilJs);

var _pubsubJs = require("./pubsub.js");

var pubsub = _interopRequireWildcard(_pubsubJs);

var _persistenceJs = require("./persistence.js");

var persist = _interopRequireWildcard(_persistenceJs);

/*
 * Class Message ---------------------------------------------
 *   inout {String} either "in" or "out"
 *   date {String} "MM/DD hh:mm:ss.sss"
 *   mid {String} 
 *   ptype {String} either "file" or "text" 
 *   desc  {Descriptor}
 *   payload {String} b64-encoded string (of utf8 for ptype text, and of byte array for ptype file) 
 */
console.log("Start module message.js");

var Message = (function () {
    function Message() {
        _classCallCheck(this, Message);

        this.inout = null;
        this.date = null;
        this.mid = null;
        this.ptype = null;
        this.desc = null;
        this.payload = null;
    }

    Message.prototype.setDateFromCurrentTime = function setDateFromCurrentTime() {
        var d = new Date(Date.now());
        this.date = pad(d.getMonth() + 1) + "/" + pad(d.getDate()) + " " + pad(d.getHours()) + ":" + pad(d.getMinutes()) + ":" + pad(d.getSeconds());
    };

    Message.prototype.toJson = function toJson() {
        return JSON.stringify(this);
    };

    Message.fromJson = function fromJson(str) {
        return Object.assign(new Message(), JSON.parse(str));
    };

    return Message;
})();

exports.Message = Message;

function pad(n) {
    return (n < 10 ? "0" : "") + n;
}

/*
 * Class MessageService ---------------------------------------------
 */

var MessageService = (function () {
    function MessageService() {
        _classCallCheck(this, MessageService);

        // List of published messages (key=msg id, value=Message)
        this.pubList = new Map();

        // List of set subscriptions (key=subscription key, value=Descriptor)
        this.subsList = new Map();

        // List of received messages (key=msg id, value=Message)
        this.recepList = new Map();

        // Global error
        this.onError = function () {
            console.log("Empty onError()");
        };
        // Notification of a the recpetion of a message
        this.onReceived = function () {
            console.log("Empty onReceived()");
        };
    }

    // Singleton

    /*
     * Setters for callbacks
     */

    MessageService.prototype.setOnError = function setOnError(f) {
        this.onError = f;
    };

    MessageService.prototype.setOnReceived = function setOnReceived(f) {
        this.onReceived = f;
    };

    /*
     * Publishes a message
     * @param {Message} msg the message to be published
     * @param {function(Message)} after - the callback passing the message, to execute after completion
     */

    MessageService.prototype.publish = function publish(msg, after) {
        var _this = this;

        pubsub.service.publishBuffer(msg.desc, msg.payload, function () {
            msg.setDateFromCurrentTime();
            _this.pubList.set(msg.mid, msg);
            persist.addPubListStorage(msg);
            after(msg);
        }, function (error) {
            alert("Publication error: " + error);
        });
    };

    /*
     * Makes a subscription
     * @param {Descriptor} pattern - the descriptor that contains the subscription pattern
     * @param {function(Message) afterReception - callback passing the received message after the processor work
     * @param {function(string)} afterSubscribe - callback passing the subscription key after completion of the subscription
     */

    MessageService.prototype.subscribe = function subscribe(pattern, afterReception, afterSubscribe) {
        var _this2 = this;

        var newSubsKey = "s" + util.uniqueId();
        console.log("Subscribe " + newSubsKey + " : " + JSON.stringify(pattern));

        pubsub.service.addSubscription(newSubsKey, pattern,
        //processor
        function (rdesc) {
            _this2.messageProcessor(rdesc, afterReception);
        },

        // ok
        function () {
            _this2.subsList.set(newSubsKey, pattern);
            persist.addSubsListStorage(newSubsKey, pattern);
            afterSubscribe(newSubsKey);
        },

        //error
        function (error) {
            alert("Subscription error: " + error);
        });
    };

    /*
     * Processes the arrival of a message (recv_desc): obtain the payload to build a message
     * @private
     * @param {mid} the received message id 
     * @param {desc} the received descriptor 
     * @param {function(string)} after - callback passing the message after processor completion
     */

    MessageService.prototype.messageProcessor = function messageProcessor(desc, after) {
        var _this3 = this;

        console.log("Message processor called with desc=" + JSON.stringify(desc));
        var mid = desc._docid;

        if (this.recepList.has(mid)) return;

        // Register the message (without its payload)
        var msg = new Message();
        msg.mid = mid;
        msg.inout = "in";
        msg.desc = desc;
        //msg.desc = Object.assign(new pubsub.Descriptor(), desc);
        msg.ptype = desc._webopp_ptype;

        // Get the payload
        pubsub.service.getAsBuffer(mid, function (payload) {
            msg.payload = payload;
            msg.setDateFromCurrentTime();
            _this3.recepList.set(mid, msg);
            persist.addRecepListStorage(msg);
            after(msg);
        }, function () {
            console.log("Processor error");
        });
    };

    /*
     * Removes a subscription
     * @param {function()} [after] - callback after completion
     */

    MessageService.prototype.unsubscribe = function unsubscribe(skey, after) {
        var _this4 = this;

        console.log("Unsubscribe : " + skey);

        pubsub.service.removeSubscription(skey,
        //ok
        function () {
            _this4.subsList["delete"](skey);
            persist.removeSubsListStorage(skey);
            if (after !== undefined) after();
        },

        //error
        function (error) {
            alert("Unsubscription error: " + error);
        });
    };

    /*
     *  Restores the publish, subscribe, and reception lists from the persistance storage
     *  For each element restored, a treatment is applied. 
     *  Previous contents of the lists are cleared.
     * @param {function(msg)} pubFunc the treatment applied to a restored published message
     * @param {function(skey, desc)} subsFunc the treatment applied to a restored subscription (subsciption key, descriptor)
     * @param {function(msg)} recepFunc the treatment applied to a restored received message
     */

    MessageService.prototype.restorePersistedLists = function restorePersistedLists(pubFunc, subsFunc, recepFunc) {
        persist.loadListsStorage(this.pubList, this.subsList, this.recepList);
        for (var _iterator = this.pubList, _isArray = Array.isArray(_iterator), _i = 0, _iterator = _isArray ? _iterator : _iterator[Symbol.iterator]();;) {
            var _ref;

            if (_isArray) {
                if (_i >= _iterator.length) break;
                _ref = _iterator[_i++];
            } else {
                _i = _iterator.next();
                if (_i.done) break;
                _ref = _i.value;
            }

            var msgId = _ref[0];
            var msg = _ref[1];

            pubFunc(msg);
        }
        for (var _iterator2 = this.subsList, _isArray2 = Array.isArray(_iterator2), _i2 = 0, _iterator2 = _isArray2 ? _iterator2 : _iterator2[Symbol.iterator]();;) {
            var _ref2;

            if (_isArray2) {
                if (_i2 >= _iterator2.length) break;
                _ref2 = _iterator2[_i2++];
            } else {
                _i2 = _iterator2.next();
                if (_i2.done) break;
                _ref2 = _i2.value;
            }

            var skey = _ref2[0];
            var desc = _ref2[1];

            subsFunc(skey, desc);
        }
        for (var _iterator3 = this.recepList, _isArray3 = Array.isArray(_iterator3), _i3 = 0, _iterator3 = _isArray3 ? _iterator3 : _iterator3[Symbol.iterator]();;) {
            var _ref3;

            if (_isArray3) {
                if (_i3 >= _iterator3.length) break;
                _ref3 = _iterator3[_i3++];
            } else {
                _i3 = _iterator3.next();
                if (_i3.done) break;
                _ref3 = _i3.value;
            }

            var msgId = _ref3[0];
            var msg = _ref3[1];

            recepFunc(msg);
        }
    };

    /*
     * Clears the list of published messages.
     * Both the in-memory list and the list in persistant storage are cleared.
     */

    MessageService.prototype.clearPersistedPubList = function clearPersistedPubList() {
        this.pubList.clear();
        persist.removeAllPubListStorage();
    };

    /*
     * Clears the list of subscriptions.
     * Unsubscribes also all the registered subscriptions.
     * Both the in-memory list and the list in persistant storage are cleared.
     */

    MessageService.prototype.clearPersistedSubsList = function clearPersistedSubsList() {
        for (var _iterator4 = persist.subsList, _isArray4 = Array.isArray(_iterator4), _i4 = 0, _iterator4 = _isArray4 ? _iterator4 : _iterator4[Symbol.iterator]();;) {
            var _ref4;

            if (_isArray4) {
                if (_i4 >= _iterator4.length) break;
                _ref4 = _iterator4[_i4++];
            } else {
                _i4 = _iterator4.next();
                if (_i4.done) break;
                _ref4 = _i4.value;
            }

            var k = _ref4[0];
            var v = _ref4[1];

            service.unsubscribe(k);
        }
        persist.removeAllSubsListStorage();
    };

    /*
     * Clears the list of received messages.
     * Both the in-memory list and the list in persistant storage are cleared.
     */

    MessageService.prototype.clearPersistedRecepList = function clearPersistedRecepList() {
        this.servicerecepList.clear();
        persist.removeAllRecepListStorage();
    };

    /* Tells DoDWAN to send the messages that are in the cache and that match our subscriptions   
     * but that we have not received (because the session was not active) 
     * @param {function(Message)} afterEach - callback passing the message triggred after the reception of each message 
     */

    MessageService.prototype.askForMissedMessages = function askForMissedMessages(afterEach) {
        var _this5 = this;

        pubsub.service.getMatching(Array.from(this.subsList.keys()),
        //ok
        function (mids) {
            var _loop = function () {
                if (_isArray5) {
                    if (_i5 >= _iterator5.length) return "break";
                    _ref5 = _iterator5[_i5++];
                } else {
                    _i5 = _iterator5.next();
                    if (_i5.done) return "break";
                    _ref5 = _i5.value;
                }

                var mid = _ref5;

                if (!_this5.recepList.has(mid)) {
                    // Issue a get_desc command
                    pubsub.service.get_desc(mid, function (desc) {
                        messageProcessor(desc, afterEach);
                    }, function (error) {
                        console.log("Missed message " + mid + " could not be retrieved: " + error);
                    });
                }
            };

            for (var _iterator5 = mids, _isArray5 = Array.isArray(_iterator5), _i5 = 0, _iterator5 = _isArray5 ? _iterator5 : _iterator5[Symbol.iterator]();;) {
                var _ref5;

                var _ret = _loop();

                if (_ret === "break") break;
            }
        });
    };

    return MessageService;
})();

var service = new MessageService();
exports.service = service;

},{"./persistence.js":9,"./pubsub.js":4,"./util.js":6}],9:[function(require,module,exports){
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

"use strict";

exports.__esModule = true;
exports.addPubListStorage = addPubListStorage;
exports.addRecepListStorage = addRecepListStorage;
exports.addSubsListStorage = addSubsListStorage;
exports.removePubListStorage = removePubListStorage;
exports.removeRecepListStorage = removeRecepListStorage;
exports.removeSubsListStorage = removeSubsListStorage;
exports.removeAllListStorage = removeAllListStorage;
exports.removeAllPubListStorage = removeAllPubListStorage;
exports.removeAllSubsListStorage = removeAllSubsListStorage;
exports.removeAllRecepListStorage = removeAllRecepListStorage;
exports.loadListsStorage = loadListsStorage;

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj["default"] = obj; return newObj; } }

var _pubsubJs = require("./pubsub.js");

var pubsub = _interopRequireWildcard(_pubsubJs);

var _messageJs = require("./message.js");

var message = _interopRequireWildcard(_messageJs);

/*
 * Update the local storage for the lists of published messages, set subscriptions and 
 * received messages
 */

console.log("Start module persistence.js");

function addPubListStorage(msg) {
    //console.log("addPubListStorage: " + JSON.stringify(msg));
    try {
        window.localStorage.setItem("_p" + msg.mid, msg.toJson());
    } catch (exception) {
        window.alert("Can't save published message : local storage full");
    }
}

function addRecepListStorage(msg) {
    //console.log("addRecepListStorage: " + JSON.stringify(msg));
    try {
        window.localStorage.setItem("_r" + msg.mid, msg.toJson());
    } catch (exception) {
        window.alert("Can't save received message : local storage full");
    }
}

function addSubsListStorage(key, desc) {
    //console.log("addSubsListStorage: " + JSON.stringify(desc));
    try {
        window.localStorage.setItem("_s" + key, desc.toJson());
    } catch (exception) {
        window.alert("Can't save subscription : local storage full");
    }
}

function removePubListStorage(msgid) {
    window.localStorage.removeItem("_p" + msgid);
}

function removeRecepListStorage(msgid) {
    window.localStorage.removeItem("_r" + msgid);
}

function removeSubsListStorage(key) {
    window.localStorage.removeItem("_s" + key);
}

function removeAllListStorage(prefix) {
    var list = [];
    var k = 0;
    for (var i = 0; i < window.localStorage.length; i++) {
        var key = window.localStorage.key(i);
        if (key.startsWith(prefix)) list[k++] = key;
    }

    for (var i = 0; i < list.length; i++) {
        window.localStorage.removeItem(list[i]);
    }
}

function removeAllPubListStorage() {
    removeAllListStorage("_p");
}

function removeAllSubsListStorage() {
    removeAllListStorage("_s");
}

function removeAllRecepListStorage() {
    removeAllListStorage("_r");
}

function loadListsStorage(pubList, subsList, recepList) {
    for (var i = 0; i < window.localStorage.length; i++) {
        var key = window.localStorage.key(i);

        if (key.startsWith("_p")) {
            var msg = message.Message.fromJson(window.localStorage.getItem(key));
            pubList.set(msg.mid, msg);
        } else if (key.startsWith("_r")) {
            var msg = message.Message.fromJson(window.localStorage.getItem(key));
            recepList.set(msg.mid, msg);
        } else if (key.startsWith("_s")) {
            var desc = pubsub.Descriptor.fromJson(window.localStorage.getItem(key));
            var subsKey = key.substring(2);
            //console.log("retreive subs " + subsKey + "  desc=" + desc.toJson());
            subsList.set(subsKey, desc);
        }
    }
}

},{"./message.js":8,"./pubsub.js":4}]},{},[7]);
