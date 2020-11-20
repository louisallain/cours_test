// BLE
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

// Wifi
#include <WiFi.h>

// Firebase
#include <FirebaseESP32.h>


// Définition des services et caractéristiques BLE utilisés
#define CYBERKEY_SERVICE_UUID "a4d6a5b6-2a84-11eb-adc1-0242ac120002" // uuid du service BLE CyberKey
#define CHAR_UUID_RX_USER_ID  "a4d6a7d2-2a84-11eb-adc1-0242ac120002" // uuid de la caractéristique sur laquelle on reçoit l'identifiant en BDD de l'utilisateur
#define CHAR_UUID_RX_USER_SIG "a4d6a8c2-2a84-11eb-adc1-0242ac120002" // uuid de la caractéristique sur laquelle on reçoit la signature de l'utilisateur en réponse au challenge
#define CHAR_UUID_TX_CHALL    "a4d6ac1e-2a84-11eb-adc1-0242ac120002" // uuid de la caractéristique sur laquelle on écrit le challenge

// Ordre de grandeur des transmissions BLE
#define MTU_SIZE 512

// Taille maximal des données que l'on va recevoir de la BDD
#define MAX_JSON 200

// SSID et mot de passe du point d'accès Wifi sur lequelle on se connecte
#define WIFI_SSID "Bbox-80CDC60A"
#define WIFI_PASSWORD "7y2uDWWK92utNXdCpq"

// Crédentials d'accès à la base de données
#define FIREBASE_HOST "https://gestionnairesallesparempreinte.firebaseio.com"
#define FIREBASE_AUTH "Oe0PX3eo7stDszodrN4x6s7fKsXMajDPtp2p1sPW"


//////////////////////////////////////////////////////////////////////////////////////////////////////////

// Variables utilisées pour la connexion et les transmissions BLE
BLEServer *cyberKeyServer = NULL;
BLECharacteristic *tx_chall = NULL;
String user_id; // Id de l'utilisateur courant en BDD
String user_sig; // Signature de challenge de l'utilisateur courant

// Variables utilisés pour récupérer les données depuis la base de données
FirebaseData firebaseData;

/**
 * Booléean servant à indiquer à la boucle du programme si un nouvel utilisateur qui souhaite déverrouiller la porte
 * à effectivement envoyé toutes les informations qui sont nécessaires à ce procédé (càd : sont id dans la BDD + la signature du challenge).
 * Moyen utilisé pour contourner le fait que le BLE et le Wifi ne peuvent être utilisé au même moment. En plus, cela permet d'éviter qu'une
 * connexion Wifi soit utilisée en permanence.
 */
bool newUserSentAllData = false;


/**
 * Classe où sont définis les callbacks du serveur BLE.
 */
class CyberKeyServerCallback: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      Serial.printf("\n . New BLE connection");
    };

    void onDisconnect(BLEServer* pServer) {
      Serial.printf("\n . BLE connection lost");
    }
};

/**
 * Classe où sont définis les callbacks de la char. rx_user_id
 */
class Rx_user_id_callback: public BLECharacteristicCallbacks {

  void onWrite(BLECharacteristic *pCharacteristic) {
    std::string value = pCharacteristic->getValue();

    if(value.length() > 0) {
      
      Serial.printf("\n . New value on rx_user_id :");

      user_id = String(value.c_str());
      Serial.print("\n . user_id=");
      Serial.print(user_id);
      
      tx_chall->setValue("cyberkey");
      tx_chall->notify();
    }
  }
};

/**
 * Classe où sont définis les callbacks de la char. rx_user_sig
 */
class Rx_user_sig_callback: public BLECharacteristicCallbacks {
  
  void onWrite(BLECharacteristic *pCharacteristic) {
    std::string value = pCharacteristic->getValue();

    if(value.length() > 0) {
      Serial.printf("\n . New value on rx_user_sig :");
      
      // Ici, on sauvegarde en globale la nouvelle signature pour ensuite la vérifier dans la boucle loop()
      user_sig = String(value.c_str());
      Serial.print("\n . user_sig=");
      Serial.print(user_sig);
      
      newUserSentAllData = true; // toutes les infos nécessaires au déverrouillage ont été envoyées.
    }
  }
};

void connectToWifi() {
  Serial.printf("\n . Connecting to %s ", WIFI_SSID);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while(WiFi.status() != WL_CONNECTED) {
    Serial.printf(".");
    delay(300);
  }
  Serial.print("\n . Connected to "); Serial.print(WIFI_SSID); Serial.print(" with IP="); Serial.print(WiFi.localIP());
}

void initBLEServer() {
  Serial.printf("\n . Starting BLE");

  BLEDevice::init("CyberKey BLE");
  uint16_t mtu = MTU_SIZE;
  BLEDevice::setMTU(mtu);
  cyberKeyServer = BLEDevice::createServer();
  cyberKeyServer->setCallbacks(new CyberKeyServerCallback());
  
  BLEService *cyberKeyService = cyberKeyServer->createService(CYBERKEY_SERVICE_UUID);

  // Char. rx_user_id
  BLECharacteristic *rx_user_id = cyberKeyService->createCharacteristic(CHAR_UUID_RX_USER_ID, BLECharacteristic::PROPERTY_WRITE);
  rx_user_id->setCallbacks(new Rx_user_id_callback());

  // Char. rx_user_sig
  BLECharacteristic *rx_user_sig = cyberKeyService->createCharacteristic(CHAR_UUID_RX_USER_SIG, BLECharacteristic::PROPERTY_WRITE);
  rx_user_sig->setCallbacks(new Rx_user_sig_callback());

  // Char. tx_chall
  tx_chall = cyberKeyService->createCharacteristic(
                                                    CHAR_UUID_TX_CHALL, 
                                                    BLECharacteristic::PROPERTY_READ | 
                                                    BLECharacteristic::PROPERTY_NOTIFY |
                                                    BLECharacteristic::PROPERTY_INDICATE
                                                  );


  cyberKeyService->start();
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(CYBERKEY_SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  Serial.printf("\n . CyberKey BLE Service operationnal !"); 
}

void setup() {
  Serial.begin(115200);

  // Initialisation BLE
  initBLEServer();
}

void loop() {
  
  // Routine permettant de vérifier si un nouvel utilisateur a envoyé toutes les infos nécessaires au déverrouillage
  if(newUserSentAllData) {
    
    Serial.printf("\n . A new user sent all data");
  
    // Arrêt du BLE
    BLEDevice::deinit(false);
    delay(200);
    
    // Initialisation Wifi
    connectToWifi();

    // Initialisation Firebase
    Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
    Firebase.reconnectWiFi(true);
    Firebase.setReadTimeout(firebaseData, 1000 * 60); // Timeout de lecture des données depuis la base = 60 secondes

    Serial.print("\n . user_id=");
    Serial.print(user_id);
    Serial.print("\n . user_sig=");
    Serial.print(user_sig);

    String pathToPublicKey = "/public_keys/" + user_id + "/public_key";
    if (Firebase.get(firebaseData, pathToPublicKey.c_str()))
    {
      Serial.println(" . Récupération de clef publique OK");
      Serial.println(" . Chemin de la clef publique: " + firebaseData.dataPath());
      Serial.println(" . Type de données de la clef publique: " + firebaseData.dataType());
      
      String modulus, exponent;

      if(firebaseData.dataType() == "json") {
          FirebaseJson json = firebaseData.jsonObject();
          FirebaseJsonData jsonObj;
          
          json.get(jsonObj, "/e");
          exponent = String(jsonObj.stringValue);

          json.get(jsonObj, "/n");
          modulus = String(jsonObj.stringValue);
          Serial.print("\n . Exponent = "); Serial.print(exponent);
          Serial.print("\n . Modulus = "); Serial.print(modulus);
      }
    }
    else
    {
      Serial.println(" . Récupération de clef publique ECHOUE");
      Serial.println(" . Raison: " + firebaseData.errorReason());
    }
    
    
    Serial.printf("\n . TODO : vérifier la signature reçue et en conséquence ouvrir ou ne pas ouvrir la pote");
    Serial.printf("\n . Redémarrage\n\n\n");
    newUserSentAllData = false;

    // Meilleur moyen (le plus rapide) que j'ai trouver pour tout réinitialiser sans problème de mémoire
    ESP.restart();
  }
}
