import React, { Component } from 'react';
import { Dimensions, NativeModules, NativeEventEmitter, Platform, PermissionsAndroid } from "react-native";
import { Container, Header, Content, Button, Text, Spinner, Toast } from 'native-base';
import BleManager from 'react-native-ble-manager';
import { stringToBytes, bytesToString } from "convert-string";
import { RSAKeychain, RSA } from 'react-native-rsa-native';
import { sha512 } from "js-sha512";
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

// Etat possible du déverrouillage
const UNLOCK_STATE = {
    NOTHING_DONE: "nothing_done"
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
            unlockState: UNLOCK_STATE.NOTHING_DONE
        }
    }

    /**
     * Méthode exécutée après le rendu du composant.
     * Ajoute les handler nécessaires à la connexion à l'ESP32
     * Demande les permissions nécéssaires à l'utilisation du BLE sur l'appareil.
     */
    componentDidMount() {

        this.handlerStopScan = bleManagerEmitter.addListener('BleManagerStopScan', this.handleEndOfScanning);
        this.handlerDiscoverPeripheral = bleManagerEmitter.addListener('BleManagerDiscoverPeripheral', this.handleDiscoverPeripheral)
    
        if (Platform.OS === 'android' && Platform.Version >= 23) {
          PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION).then((result) => {
            if (result) console.log("Permission is OK")
            else {
                PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION).then((result) => {
                    if (result) console.log("User accept")
                    else console.log("User refuse")
                })
            }
          })
        }
    
        BleManager.enableBluetooth()
        .then(() => console.log("The bluetooth is already enabled or the user confirm"))
        .catch((error) => console.log("The user refuse to enable bluetooth"))
    
        BleManager.start({ showAlert: false }).then(() => console.log("Module initialized"))
    }
    
    /**
     * Méthode exécutée avant que le composant se démonte.
     * Retire les handlers.
     * Se déconnecte de l'ESP322 si l'on était connecté.
     */
    componentWillUnmount() {
        this.handlerStopScan.remove()
        this.handlerDiscoverPeripheral.remove()

        if(this.state.connectedTo_cykerKeyESP32) {
            BleManager.disconnect(this.state.cyberKeyESP32_peripheral.id)
            .then(() => console.log("Disconnected from CyberKey ESP32"))
            .catch((error) => console.log(error))
        }
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
     * Handler lorsque le scan BLE s'arrête
     */
    handler_StopBLEScanning = () => {
        console.log("BLE Scanning ended...")
        this.setState({scanning: false})
    }

    /**
     * Handler lorsqu'un nouveau périphérique BLE est trouvé.
     * @param {Object} peripheral objet représentant un périphérique
     */
    handler_discoverNewBLEPeripheral = (peripheral) => {
        if(peripheral.id === CYBER_KEY_ESP32_MAC_ADDR) {
            BleManager.stopScan() // Arrête le scanning lorsque l'on trouve l'ESP32 de la serrure
            console.log("ID du ESP32 Cyker key = ", peripheral.id)
            console.log(peripheral)
            this.setState({
              cyberKeyESP32Found: true,
              cyberKeyESP32_peripheral: peripheral,
            })
      
            this.connectToCyberKeyESP32(); // se connecte automatiquement à l'ESP32 CyberKey
        }
    }

    /**
     * Se connecte en BLE à l'ESP32 de la serrure.
     * Vérifie d'abord que l'ESP32 a été trouvé lors du scan.
     */
    connectToCyberKeyESP32 = () => {
        if(this.state.cyberKeyESP32Found === true) {
            BleManager.connect(this.state.cyberKeyESP32_peripheral.id)
            .then(() => {
                console.log("Connected to CykerKey ESP32")
                this.setState({connectedTo_cykerKeyESP32: true})
            })
            .catch((error) => {
                console.log(error)
                this.setState({connectedTo_cykerKeyESP32: false})
            })   
        }
    }

    /**
     * Ecrit du texte sur la caractéristique BLE de l'ESP32 concernant la serure
     * @param {String} text le texte à envoyer à l'ESP32
     * @param {Function} callback callback() lorsque l'on a écrit sur la caractéristique
     */
    writeToESP32 = (text, callback) => {

        const data = stringToBytes(text)
    
        BleManager.retrieveServices(this.state.cyberKeyESP32_peripheral.id)
        .then((peripheralInfo) => {

            console.log("Peripheral info:", peripheralInfo)
            
            BleManager.write(
                this.state.cyberKeyESP32_peripheral.id,
                CYBER_KEY_ESP32_SERVICE_UUID,
                CYBER_KEY_ESP32_CHARACTERISTIC_UUID,
                data
            )
            .then(() => callback())
            .catch((error) => console.log(error))
        })
    }

    /**
     * Lit la caractéristique BLE et retourne le résultat sous forme de chaine de caractères ou une erreur.
     */
    readFromESP32 = () => {
        let ret
        BleManager.read(
            this.state.cyberKeyESP32_peripheral.id,
            CYBER_KEY_ESP32_SERVICE_UUID,
            CYBER_KEY_ESP32_CHARACTERISTIC_UUID
        )
        .then((readData) => ret = bytesToString(readData))
        .catch((error) => ret = error)
        return error
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
        let text = this.state.loading ? "" : "Déverrouillage"
        return (
            <Button style={styles.unlockButton} onPress={this.unlock}>
                <Text>{text}</Text>
            </Button>
        )
    }

    /**
     * Retourne vrai si il y a à l'heure courant un créneau disponible.
     */
    isThereACurrentEvent = () => {
        let currentDate = new Date()
        return ((
            this.props.events.filter(e => {
                let startDate = new Date(e.start)
                let endDate = new Date(e.end)
                return (currentDate >= startDate && currentDate <= endDate)
            })
        ).length === 1 ? true : false)
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
     * - Regarde si un créneau est actuellement en cours (disponible)
     * - Scan les périphériques BLE alentours
     * - Si le scan précédent permet de retrouver la serrure grâce à son adresse MAC, s'y connecte
     * - Si la connexion précédente s'est bien établie, lance la procédure d'authentification qui est :
     *      - Envoie à la serrure le couple (créneau_courant, id_utilisateur_bdd) *id_utilisateur_bdd est l'adresse mail sans les points
     *      - Attend une chaine challenge de la serrure "chaine_chall"
     *      - Chiffre la chaine challenge avec la clef privée de l'utilisateur contenue dans le SecureKeyStore puis l'envoie à la serrure
     * - Si la procédure d'authentification aboutie alors la serrure est déverrouillée et on informe l'utilisateur.
     */
    unlock = () => {
        
        let privateKey = 
        "-----BEGIN RSA PRIVATE KEY-----\n" +
        "MIIEogIBAAKCAQEAkw7qWtHXgvdPTEjvy+B3be0wP+0ixtNZltJ6AFkoELhIeDUr\n" +
        "HtSjeKSc/J7kLisBIGZILEkRxZVT8n5yjazjIoHACDXPbjcfw7SNWLyjA+SpmcFz\n" +
        "enIzu9x3al53xujlePYoQWF3KFIbeSC0NydD7PBo2SjZkHLJr1JcNgOkAx1YHRMt\n" +
        "4BZy5OzvP3Cf1Jwx4S4RmcO3W8M/AC3iayiNOjZiTGivMA+Nmng20/TqoIKzvunt\n" +
        "2GFrWxtfa51mrKI0WsosR3voqlGaVNEfD2W0NRnSxt2sC/7UQ38ZWs6QaDsBkQsp\n" +
        "LHbUyfL8X7dT2dbeo9mxAy02Mu0UgShQHJOUzwIDAQABAoIBABqQerAOMOZ1AyuO\n" +
        "D1ceRKSkUG9zyDDvU8MlYSl38X9XsquYOyJWGfN8lOkjUKewWANAxq/NQTsWVvQ6\n" +
        "eDn0bZE5q/Uvtzk7Fu9UyaWqaa4V27ctFU2cnJDGagHab7uMLYuUt7/budkcP985\n" +
        "E6TU4HXIFcCImwh3W8xiTcrqDHINUFLDlGgHDtGKikI1ac+AC1YT9hmQdiNGy92r\n" +
        "r5/ceraQnWBk6Vyp/qs2eo9Gn+nW7t1y5mvnql5NSLPBnz0zsR18AFM+kRxTAfL1\n" +
        "B6ltctfYcTmppQu/EsdTh54aKv7br4mH33lzMgZ+lDUn2aVdcSsAn61gMm+NfQpj\n" +
        "J1O50q0CgYEAxwme7DTQ8bay/WKhm4mlmj03oKIyJGmLN7L6QMAw7TM1XczifJGs\n" +
        "deYWylTCfMcl6a4oDIyA8s/oCVJirhUJpDaM3hQATNRMEOGNGCEiDUyws2BVO/Ba\n" +
        "reD0rFIrzWeA3JW08tjHjt6naEA1Yr7N6VV7Mc+WWAAwqDYxkXJZBmsCgYEAvSUO\n" +
        "9t44Ih4kELRNcXRYOBRsre+3uc+RNxIMEDirYwYqEpubp4SzT9C6Fq+WnFTXQ+kU\n" +
        "66BRbj2OEnIkSS/w6dBgr+JDmtJws4r3a5JIXDjFepEJ2CqwbpoFrNgomoV/LVW9\n" +
        "RhhKKHv4g9tChZhuyWCf5Vgdl4C0TRHnXX+dXC0CgYBiQ1KtZKnfG4akZ+RHKJ6U\n" +
        "VjK3YDwHFABCQF8iRoAhKs3nADa0ryqIjk6kF1JpTdxe0Lc+8sLanV/98il7rjEz\n" +
        "XCcRoKhBRfJDx0bkKb9o93qpJB8QV2o9O7kvteWgI8BPpq1N0046L5xQcJqqpG5+\n" +
        "CtCmV8juqzFaal7wYpam/wKBgF7/geiT6/WW1d5d8JePK8/e0kb6I3j7u5/BPLXZ\n" +
        "AJuXuQHnjeIxUC5CU3SZdDfIlOXS52MA/mU0HWs+UONBZOrKcgVkyzezgk9q+KHa\n" +
        "suU+zlClECC2MC8cxMTvPFV/v0Rxz8Ko6HY07bisI1keGmaiVSzHePVi+yvKVj4r\n" +
        "nklxAoGAFAbuy5eQYU9bXNFiGvm1Yu3zQ95wlwGk6qRj41oO6q/gocmjcxReMCdt\n" +
        "yplL6YK+8o6oTxb7k7ZSPNFxgwcA8mLGOdBt3/ne5c5TnEACT0BahzPco9sSnFny\n" +
        "RgQmVlzUPF1jj46lnURxcgk1/X3Ozb1F/Mis+JI2WdfrteOC/gA=\n" +
        "-----END RSA PRIVATE KEY-----\n";

        let publicKey = 
        "-----BEGIN RSA PUBLIC KEY-----\n" +
        "MIIBCgKCAQEAkw7qWtHXgvdPTEjvy+B3be0wP+0ixtNZltJ6AFkoELhIeDUrHtSj\n" +
        "eKSc/J7kLisBIGZILEkRxZVT8n5yjazjIoHACDXPbjcfw7SNWLyjA+SpmcFzenIz\n" +
        "u9x3al53xujlePYoQWF3KFIbeSC0NydD7PBo2SjZkHLJr1JcNgOkAx1YHRMt4BZy\n" +
        "5OzvP3Cf1Jwx4S4RmcO3W8M/AC3iayiNOjZiTGivMA+Nmng20/TqoIKzvunt2GFr\n" +
        "Wxtfa51mrKI0WsosR3voqlGaVNEfD2W0NRnSxt2sC/7UQ38ZWs6QaDsBkQspLHbU\n" +
        "yfL8X7dT2dbeo9mxAy02Mu0UgShQHJOUzwIDAQAB\n" +
        "-----END RSA PUBLIC KEY-----\n";
        
        RSA.signWithAlgorithm("louis", privateKey, RSA.SHA512withRSA).then((sig) => console.log(sig))
        
        /*
        RSAKeychain.getPublicKey(`${STORAGE_NAMING.PRIVATE_KEY_NAME}-${this.props.user.id}`).then((pub_key) => console.log(pub_key))
        RSAKeychain.signWithAlgorithm("louis", `${STORAGE_NAMING.PRIVATE_KEY_NAME}-${this.props.user.id}`, RSAKeychain.SHA512withRSA)
            .then((signature) => console.log(signature))
            .catch((error) => console.log(error))
        */

        /*
        this.loading(true)
        
        // Vérifie qu'un créneau est actuellement disponible
        if(!this.isThereACurrentEvent()) {
            Toast.show({text: "Aucun créneau n'est actuellement disponible !"})
            return;
        }
        
        const message = "louis"
        const hash = sha512(message)
        console.log("HASH =\n", `[${hash}]`)
        let keyTag = `${STORAGE_NAMING.PRIVATE_KEY_NAME}-${this.props.user.id}`
        RSAKeychain.sign(message, keyTag)
            .then((signature) => {
                console.log("SIGNATURE B64=\n", `[${signature}]`)
                console.log("SIGNATURE HEX =\n", `[${base64ToHex(signature)}]`)
            })
            .catch((error) => console.log(error))
        this.loading(false)
        */
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