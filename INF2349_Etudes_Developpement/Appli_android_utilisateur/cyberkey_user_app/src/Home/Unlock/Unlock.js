import React, { Component } from 'react';
import { Dimensions, NativeModules, NativeEventEmitter, Platform, PermissionsAndroid } from "react-native";
import { Container, Header, Content, Button, Text, Spinner, Toast } from 'native-base';
import BleManager from 'react-native-ble-manager';
import { stringToBytes, bytesToString } from "convert-string";
import { RSAKeychain } from 'react-native-rsa-native';
import { base64ToHex } from "../../utils/functions";

import * as STORAGE_NAMING from '../../utils/storage_naming';

import styles from './UnlockCSS';

// Nécessaire au BLE
const BleManagerModule = NativeModules.BleManager;
const bleManagerEmitter = new NativeEventEmitter(BleManagerModule);

// Informations en dur pour l'instant mais dans la BDD au final
const CYBER_KEY_ESP32_MAC_ADDR = "24:0A:C4:58:4F:0A"; // @MAC de l'ESP32 serrure
const CYBER_KEY_ESP32_SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"; // Identifiant du service BLE
const CYBER_KEY_ESP32_CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"; // Identifiant de la caractéristique BLE

// Constante permettant d'identifier le type d'envoi que l'on fournit à l'ESP32
const SEND_TYPE = {
    USER_INFOS: '#', // Les infos relatives à l'identification de l'ouverte (id de l'utilisateur dans la bdd, id de l'event pour lequel on souhaite déverrouiller la porte)
    SIGNATURE: '!' // la signature du challenge envoyé par la serrure
}

const RCV_TYPE = {
    AUTH_OK: 'V'
}

/**
 * Classe représentant la page de déverrouillage de la porte.
 */
export default class Unlock extends Component {

    /**
     * Constructeur du composant.
     * @param {Object} props proprétés du composant
     */
    constructor(props) {
        super(props)
        this.state = {
            loading: false,
            scanning: false, // état du scan BLE
            cyberKeyESP32Found: false, // si l'ESP32 a été trouvé lors du scan
            cyberKeyESP32_peripheral: null, // l'objet représentant le périphérique ESP32
            connectedTo_cykerKeyESP32: false, // si on est connecté à l'ESP32
            allPermissionsAndBT_ok: false,
        }
    }

    /**
     * Méthode exécutée après le rendu du composant.
     * Ajoute les handler nécessaires à la connexion à l'ESP32
     * Demande les permissions nécéssaires à l'utilisation du BLE sur l'appareil.
     */
    componentDidMount() {

        this.handlerStopScan = bleManagerEmitter.addListener('BleManagerStopScan', this.handler_StopBLEScanning);
        this.handlerDiscoverPeripheral = bleManagerEmitter.addListener('BleManagerDiscoverPeripheral', this.handler_discoverNewBLEPeripheral)
        
        this.checkPermissionsAndBT()
    }
    
    /**
     * Méthode exécutée avant que le composant se démonte.
     * Retire les handlers.
     * Se déconnecte de l'ESP322 si l'on était connecté.
     */
    componentWillUnmount() {
        this.handlerStopScan.remove()
        this.handlerDiscoverPeripheral.remove()
        this.disconnectFromCyberKeyESP32()
    }

    /**
     * Demandes toutes les permissions nécessaires et demande d'activer le Bluetooth.
     */
    checkPermissionsAndBT = () => {
        let perm_BT_ok = true

        if(Platform.OS === 'android') {
            PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION).then((result) => {
                if (result === true) console.log("PERMISSIONS.ACCESS_BACKGROUND_LOCATION OK")
                else if(result === false) {
                    PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_BACKGROUND_LOCATION).then((result) => {
                        if (result) console.log("PERMISSIONS.ACCESS_BACKGROUND_LOCATION OK accepted")
                        else perm_BT_ok = false
                    })
                }
            })
        }
        if (Platform.OS === 'android' && Platform.Version >= 23 && Platform.Version < 29) {
            console.log("PAS OK ")
            PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION).then((result) => {
                if (result) console.log("PERMISSIONS.ACCESS_COARSE_LOCATION OK")
                else {
                    PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION).then((result) => {
                        if (result) console.log("PERMISSIONS.ACCESS_COARSE_LOCATION accepted")
                        else perm_BT_ok = false
                    })
                }
            })
        }
        if(Platform.OS === 'android' && Platform.Version >= 29) {
            PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION).then((result) => {
                if (result) console.log("PERMISSIONS.ACCESS_FINE_LOCATION OK")
                else {
                    PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION).then((result) => {
                        if (result) console.log("PERMISSIONS.ACCESS_FINE_LOCATION accepted")
                        else perm_BT_ok = false
                    })
                }
            })
        }
    
        BleManager.enableBluetooth()
        .then(() => BleManager.start({ showAlert: false }).then(() => console.log("Module initialized")))
        .catch((error) => perm_BT_ok = false)
    
        this.setState({allPermissionsAndBT_ok: perm_BT_ok})
    }

    /**
     * Débute le scan BLE.
     */
    beginBLEScanning = () => {
        BleManager
            .scan([], 3, true)
            .then((results) => {
                console.log("BLE Scanning started...")
                this.setState({scanning: true})
            })
    }

    /**
     * Se déconnecte de l'ESP32 CyberKey (si on était connecté).
     */
    disconnectFromCyberKeyESP32 = () => {
        if(this.state.connectedTo_cykerKeyESP32) {
            BleManager.disconnect(this.state.cyberKeyESP32_peripheral.id)
            .then(() => {
                console.log("Disconnected from CyberKey ESP32")
                this.setState({connectToCyberKeyESP32: false,})
            })
            .catch((error) => console.log(error))
        }
    }

    /**
     * Handler lorsque le scan BLE s'arrête
     */
    handler_StopBLEScanning = () => {
        console.log("BLE Scanning ended...")
        this.setState({scanning: false})
        if(this.state.cyberKeyESP32Found === false) { // le scan n'a pas permis de trouver l'ESP32 de la serrure
            this.resetForReason("Serrure non trouvée !")
        } 
    }

    /**
     * Handler lorsqu'un nouveau périphérique BLE est trouvé.
     * @param {Object} peripheral objet représentant un périphérique
     */
    handler_discoverNewBLEPeripheral = (peripheral) => {
        console.log(peripheral.id)
        if(peripheral.id === CYBER_KEY_ESP32_MAC_ADDR) {
            BleManager.stopScan() // Arrête le scanning lorsque l'on trouve l'ESP32 de la serrure
            console.log("ID du ESP32 Cyker key = ", peripheral.id)
            console.log(peripheral)
            this.setState({
              cyberKeyESP32Found: true,
              cyberKeyESP32_peripheral: peripheral,
            })
            
            // Procédure de déverrouillage
            this.connectToCyberKeyESP32( // se connecte automatiquement à l'ESP32 CyberKey
                () => {
                    this.launchUnlockProcedure(); 
                },
                () => {
                    this.resetForReason("Erreur de connexion à l'ESP32 !")
                }
            ); 
        }
    }

    /**
     * Se connecte en BLE à l'ESP32 de la serrure.
     * Vérifie d'abord que l'ESP32 a été trouvé lors du scan.
     * @param callback fonction exécutée lorsqu'on se connecte à l'ESP32.
     * @param errorCallback fonction exécutée si une erreur survient.
     */
    connectToCyberKeyESP32 = (callback, errorCallback) => {
        if(this.state.cyberKeyESP32Found === true) {
            BleManager.connect(this.state.cyberKeyESP32_peripheral.id)
            .then(() => {
                console.log("Connected to CykerKey ESP32")
                this.setState({connectedTo_cykerKeyESP32: true})
                if(callback) callback()
            })
            .catch((error) => {
                if(errorCallback) errorCallback()
            })   
        }
    }

    /**
     * Ecrit du texte sur la caractéristique BLE de l'ESP32 concernant la serure
     * @param {String} text le texte à envoyer à l'ESP32
     * @param {Function} callback callback() lorsque l'on a écrit sur la caractéristique
     */
    writeToESP32 = (text, callback, errorCallback) => {

        const data = stringToBytes(text)
    
        BleManager.retrieveServices(this.state.cyberKeyESP32_peripheral.id)
        .then((peripheralInfo) => {
            
            BleManager.write(
                this.state.cyberKeyESP32_peripheral.id,
                CYBER_KEY_ESP32_SERVICE_UUID,
                CYBER_KEY_ESP32_CHARACTERISTIC_UUID,
                data
            )
            .then(() => {
                if(callback) callback()
            })
            .catch((error) => {
                if(errorCallback) errorCallback()
            })
        })
    }

    /**
     * Lit la caractéristique BLE et retourne le résultat sous forme de chaine de caractères ou une erreur.
     * @param callback fonction appelée avec en paramètre le résultat de la lecture ou un erreur.
     */
    readFromESP32 = (callback, errorCallback) => {
        BleManager.read(
            this.state.cyberKeyESP32_peripheral.id,
            CYBER_KEY_ESP32_SERVICE_UUID,
            CYBER_KEY_ESP32_CHARACTERISTIC_UUID
        )
        .then((readData) => {
            if(callback) callback(bytesToString(readData))
        })
        .catch((error) => {
            if(errorCallback) errorCallback()
        })
    }

    /**
     * Méthode rendant un élément indiquant le chargement.
     */
    renderLoading = () => {
        if(this.state.loading) return <Spinner size={Dimensions.get("screen").width * 0.96} style={styles.loading} color="#FFBE00"/>
    }

    /**
     * Méthode rendant le bouton de déverouillage.
     */
    renderButton = () => {
        let text = "Déverrouillage"
        if(!this.state.allPermissionsAndBT_ok) text = "Veuillez accepter les permissions et activer le Bluetooth."
        if(this.state.allPermissionsAndBT_ok && this.state.loading) {
            if(this.state.scanning) {
                text = "Scan des BLE..."
            }
            if(this.state.cyberKeyESP32Found) {
                text = "Connexion à la serrure..."
            }
            if(this.state.connectedTo_cykerKeyESP32) {
                text = "Authentification..."
            }
        }
        let disabled = !this.state.allPermissionsAndBT_ok || this.state.loading
        return (
            <Button disabled={disabled} style={styles.unlockButton} onPress={this.unlock}>
                <Text>{text}</Text>
            </Button>
        )
    }

    /**
     * Retourne vrai si il y a à l'heure courant un créneau disponible.
     */
    isThereACurrentEvent = () => {
        let currentDate = new Date()
        currentDate.setHours(currentDate.getHours(), currentDate.getMinutes(), 0, 0)
        return (
            this.props.events.filter(e => {
                let startDate = new Date(e.start)
                let endDate = new Date(e.end)
                return (currentDate >= startDate && currentDate <= endDate)
            })
        )
    }

    /**
     * Modifie l'état graphique du composant pour montrer à l'utilisateur que l'application travaille.
     * @param {Boolean} isLoading actuellement en travail
     */
    loading = (isLoading) => {
        this.setState({loading: isLoading})
    }

    /**
     * Fonction enclanchant la procédure de déverrouillage de la porte, dans l'ordre :
     * - Si la connexion précédente s'est bien établie, lance la procédure d'authentification qui est :
     *      - Envoie à la serrure id_utilisateur_bdd est l'adresse mail sans les points
     *      - Attend une chaine challenge de la serrure
     *      - Chiffre la chaine challenge avec la clef privée de l'utilisateur contenue dans le SecureKeyStore puis l'envoie à la serrure
     *      - Attend la réponse de la serrure pour savoir si la signature a été validée ou non.
     * - Si la procédure d'authentification aboutie alors la serrure est déverrouillée et on informe l'utilisateur.
     */
    launchUnlockProcedure = () => {

        let infos = {
            user_id: this.props.user.id.replace(/[.]/g, '')
        }
        // le # permet à l'ESP32 de repérer la fin de la valeur de la caractéristique étant donnée que le MTU par défaut est de 23 octets (20 + 3 protocol wrapper).
        this.writeToESP32(JSON.stringify(infos)+SEND_TYPE.USER_INFOS, 
            () => { 
                this.readFromESP32(
                    (value) => {
                        let challenge = value
                        let keyTag = `${STORAGE_NAMING.PRIVATE_KEY_NAME}-${this.props.user.id}`        
                        RSAKeychain.signWithAlgorithm(challenge, keyTag, RSAKeychain.SHA256withRSA)
                            .then((signature) => {
                                this.writeToESP32(base64ToHex(signature)+SEND_TYPE.SIGNATURE, 
                                    () => {
                                        this.readFromESP32(
                                            (value) => {
                                                if(value == RCV_TYPE.AUTH_OK) { // signature valide donc je suis authentifié
                                                    this.resetForReason("Porte ouverte !")
                                                }
                                                else { // signature non validée
                                                    this.resetForReason("Erreur d'authenfication !")
                                                }
                                            },
                                            () => {
                                                this.resetForReason("Erreur de lecture de la réponse d'autorisation !")
                                            }
                                        )
                                    },
                                    () => {
                                        this.resetForReason("Erreur d'envoi de la signature RSA !")
                                    }
                                )
                            })
                            .catch((error) => console.log(error))
                    },
                    () => {
                        this.resetForReason("Erreur de lecture du challenge !")
                    }
                )
            },
            () => {
                this.resetForReason("Erreur d'envoi des infos utilisateur !")
            }
        )
    }

    /**
     * Réinitialise l'état et affiche la raison (peut être dû à une erreur ou non !).
     * Affiche la raison à l'utilisateur sous la forme d'un Toast.
     * @param {string} msg raison du reset de l'état
     */
    resetForReason = (msg) => {
        this.disconnectFromCyberKeyESP32()
        this.setState({
            loading: false,
            scanning: false, // état du scan BLE
            cyberKeyESP32Found: false, // si l'ESP32 a été trouvé lors du scan
            cyberKeyESP32_peripheral: null, // l'objet représentant le périphérique ESP32
            connectedTo_cykerKeyESP32: false, // si on est connecté à l'ESP32
            allPermissionsAndBT_ok: this.state.allPermissionsAndBT_ok,
        })
        if(msg) Toast.show({text: msg})
    }

    /**
     * Handler du bouton de déverrouillage.
     * - Regarde si un créneau est actuellement en cours.
     * - Regarde si l'utilisateur a accès à ce créneau ou non.
     * - Scan les périphériques BLE alentours afin de se connecter à la serrure.
     */
    unlock = () => {

        this.checkPermissionsAndBT()
        this.loading(true)
        // Vérifie l'accès pour l'utilisateur
        let currentEvent = this.isThereACurrentEvent()[0]
        if(currentEvent === undefined) {
            Toast.show({text: "Aucun créneau n'est actuellement disponible !"})
            this.loading(false)
            return;
        }
        if(this.props.user.requestForEvents === undefined & this.props.user.acceptedForEvents === undefined) {
            Toast.show({text: "Accès non demandé !"})
            this.loading(false)
            return;
        }
        if(this.props.user.requestForEvents !== undefined & this.props.user.acceptedForEvents === undefined) {
            if(this.props.user.requestForEvents.filter(id => id === currentEvent.id).length > 0) {
                Toast.show({text: "Accès à ce créneau en attente !"})
                this.loading(false)
                return;
            }
            else {
                Toast.show({text: "Accès non demandé pour ce créneau !"})
                this.loading(false)
                return;
            }
        }
        if(this.props.user.requestForEvents === undefined & this.props.user.acceptedForEvents !== undefined) {
            if(this.props.user.acceptedForEvents.filter(id => id === currentEvent.id).length === 0) {
                Toast.show({text: "Vous n'avez pas accès à ce créneau !"})
                this.loading(false)
                return;
            }
            else {
                this.beginBLEScanning() // démarre la procédure de déverrouillage (voir méthode beginBLEScanning)
            }
        }
        if(this.props.user.requestForEvents !== undefined & this.props.user.acceptedForEvents !== undefined) {
            if(this.props.user.requestForEvents.filter(id => id === currentEvent.id).length > 0) {
                Toast.show({text: "Accès à ce créneau en attente !"})
                this.loading(false)
                return;
            }
            else {
                if(this.props.user.acceptedForEvents.filter(id => id === currentEvent.id).length === 0) {
                    Toast.show({text: "Vous n'avez pas accès à ce créneau !"})
                    this.loading(false)
                    return;
                }
                else {
                    this.beginBLEScanning() // démarre la procédure de déverrouillage (voir méthode beginBLEScanning)
                }
            }
        }
    }
    
    /**
     * Méthode de rendu du composant.
     */
    render() {
        return (
        <Container style={styles.container}>
            {this.renderLoading()}
            {this.renderButton()}
        </Container>
        )
    }
}