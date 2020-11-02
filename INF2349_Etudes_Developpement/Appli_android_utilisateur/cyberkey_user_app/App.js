import { StatusBar } from 'expo-status-bar';
import React, {Component} from 'react';
import { StyleSheet, View, Text, TextInput, Button, NativeModules, NativeEventEmitter, Platform, PermissionsAndroid } from 'react-native';
import BleManager from 'react-native-ble-manager';
import { stringToBytes, bytesToString } from "convert-string";

const BleManagerModule = NativeModules.BleManager;
const bleManagerEmitter = new NativeEventEmitter(BleManagerModule);

const CYBER_KEY_ESP32_MAC_ADDR = "24:0A:C4:58:4F:0A";
const CYBER_KEY_ESP32_SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
const CYBER_KEY_ESP32_CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";

export default class App extends Component {

  constructor() {
    super()

    this.state = {
      scanning: false,
      cyberKeyESP32Found: false,
      cyberKeyESP32_peripheral: null,
      connectedTo_cykerKeyESP32: false,
      textToSentTo_cykerKeyESP32: "",
      textReceiveFrom_cyberKeyESP32: "",
    }
  }

  handleScanningButton = () => {
    BleManager.scan([], 3, true).then((results) => {
      console.log("Scan started")
      this.setState({scanning: true})
    })
  }

  connectToCyberKeyESP32 = () => {
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

  handleWriteButton = () => {

    const data = stringToBytes(this.state.textToSentTo_cykerKeyESP32)

    BleManager.retrieveServices(this.state.cyberKeyESP32_peripheral.id)
    .then((peripheralInfo) => {

        console.log("Peripheral info:", peripheralInfo);

        BleManager.write(
          this.state.cyberKeyESP32_peripheral.id,
          CYBER_KEY_ESP32_SERVICE_UUID,
          CYBER_KEY_ESP32_CHARACTERISTIC_UUID,
          data
        )
        .then(() => {
          console.log("Write: " + data)

          BleManager.read(
            this.state.cyberKeyESP32_peripheral.id,
            CYBER_KEY_ESP32_SERVICE_UUID,
            CYBER_KEY_ESP32_CHARACTERISTIC_UUID
          )
          .then((readData) => {
            // Success code
            console.log("Read: " + readData);
        
            this.setState({textReceiveFrom_cyberKeyESP32: bytesToString(readData)})
          })
          .catch((error) => {
            // Failure code
            console.log(error);
          });
        })
        .catch((error) => {
          console.log(error);
        })
      }
    )
    
  }

  handleEndOfScanning = () => {
    console.log("Scan stopped")
    this.setState({scanning: false})
  }

  handleDiscoverPeripheral = (peripheral) => {
    
    if(peripheral.id === CYBER_KEY_ESP32_MAC_ADDR) {
      BleManager.stopScan().then(() => console.log("Scan stopped"))
      console.log("ID du ESP32 Cyker key = ", peripheral.id)
      console.log(peripheral)
      this.setState({
        cyberKeyESP32Found: true,
        cyberKeyESP32_peripheral: peripheral,
      })

      this.connectToCyberKeyESP32(); // se connecte automatiquement à l'ESP32 CyberKey
    }
  }

  componentDidMount() {

    this.handlerStopScan = bleManagerEmitter.addListener('BleManagerStopScan', this.handleEndOfScanning);
    this.handlerDiscoverPeripheral = bleManagerEmitter.addListener('BleManagerDiscoverPeripheral', this.handleDiscoverPeripheral)

    if (Platform.OS === 'android' && Platform.Version >= 23) {
      PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION).then((result) => {
        if (result) {
          console.log("Permission is OK");
        } else {
          PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION).then((result) => {
            if (result) {
              console.log("User accept");
            } else {
              console.log("User refuse");
            }
          });
        }
      });
    }

    BleManager.enableBluetooth()
    .then(() => console.log("The bluetooth is already enabled or the user confirm"))
    .catch((error) => console.log("The user refuse to enable bluetooth"))

    BleManager.start({ showAlert: false }).then(() => console.log("Module initialized"))
  }

  componentWillUnmount() {
    this.handlerStopScan.remove()
    this.handlerDiscoverPeripheral.remove()
    
    if(this.state.connectedTo_cykerKeyESP32) {
      BleManager.disconnect(this.state.cyberKeyESP32_peripheral.id)
      .then(() => console.log("Disconnected from CyberKey ESP32"))
      .catch((error) => console.log(error))
    }
  }

  render() {
    return (
      <View style={styles.container}>
        <Text>ESP32 Trouvé = {this.state.cyberKeyESP32Found ? "true" : "false"}</Text>
        <Button
          onPress={this.handleScanningButton}
          disabled={this.state.scanning}
          title="Scan"
          color="#841584"
        />
        <Button
          onPress={this.handleWriteButton}
          disabled={!this.state.connectedTo_cykerKeyESP32}
          title="Write"
          color="#841584"
        />
        <TextInput
          style={{ height: 40, borderColor: 'gray', borderWidth: 1 }}
          onChangeText={text => this.setState({textToSentTo_cykerKeyESP32: text})}
          placeholder="Text à envoyer à l'ESP32"
          editable={this.state.connectedTo_cykerKeyESP32}
        />
        <Text>Réponse de l'ESP32 : {this.state.textReceiveFrom_cyberKeyESP32}</Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
