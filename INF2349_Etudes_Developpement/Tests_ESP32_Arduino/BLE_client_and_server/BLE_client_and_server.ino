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
#define CHAR_UUID_TX_IS_AUTH  "c15079b0-2a84-11eb-adc1-0242ac120002" // uuid de la caractéristique sur laquelle on écrit la réponse à la procédure d'authentification

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
BLECharacteristic *tx_isAuth = NULL;
bool userConnected = false;
bool oldUserConnected = false;
char user_id[512] = {'\0'}; // Id de l'utilisateur courant en BDD
char user_sig[512] = {'\0'}; // Signature de challenge de l'utilisateur courant

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
      userConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      Serial.printf("\n . BLE connection lost");
      userConnected = false;
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

      // Sauvegarde globallement l'id de l'utilisateur courant en BDD
      user_id[0] = 0;
      strcpy(user_id, value.c_str());
      Serial.printf("\n . %s\n", user_id);
      
      if(userConnected) {
        tx_chall->setValue("cyberkey");
        tx_chall->notify();
      }
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
      Serial.printf("\n . user_id=%s", user_id);
      
      if(userConnected) {

        // Ici, on sauvegarde en globale la nouvelle signature pour ensuite la vérifier dans la boucle loop()
        user_sig[0] = 0;
        strcpy(user_sig, value.c_str());
        Serial.printf("\n . sig=%s\n", user_sig);
        
        newUserSentAllData = true; // toutes les infos nécessaires au déverrouillage ont été envoyées.
        
        /*
        tx_isAuth->setValue("V");
        tx_isAuth->notify();
        */
      }
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

void setup() {
  Serial.begin(115200);

  // Initialisation BLE
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

  // Char. tx_isAuth
  tx_isAuth = cyberKeyService->createCharacteristic(CHAR_UUID_TX_IS_AUTH, 
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

void loop() {

  // Routine permettant de renouveler les connexions BLE
  // Déconnexion
  if(!userConnected && oldUserConnected) {
    delay(500); // give the bluetooth stack the chance to get things ready
    cyberKeyServer->startAdvertising(); // restart adverstising
    Serial.printf("\n . Advertising again");
    oldUserConnected = userConnected;
  }

  // Connexion
  if(userConnected && !oldUserConnected) {
    oldUserConnected = userConnected;
  }

  // Routine permettant de vérifier si un nouvel utilisateur a envoyé toutes les infos nécessaires au déverrouillage
  if(newUserSentAllData) {

    Serial.printf("\n A new user sent all data");
  
    // Arrêt du BLE
    BLEDevice::deinit(false);
    delay(300);
    
    // Initialisation Wifi
    connectToWifi();

    // Initialisation Firebase
    Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
    Firebase.reconnectWiFi(true);
    Firebase.setReadTimeout(firebaseData, 1000 * 60); // Timeout de lecture des données depuis la base = 60 secondes

    if (Firebase.getInt(firebaseData, "/test/int"))
    {
      Serial.println("PASSED");
      Serial.println("PATH: " + firebaseData.dataPath());
      Serial.println("TYPE: " + firebaseData.dataType());
      Serial.println("ETag: " + firebaseData.ETag());
      Serial.print("VALUE: ");
      Serial.print(firebaseData.intData());
      Serial.println("------------------------------------");
      Serial.println();
    }
    else
    {
      Serial.println("FAILED");
      Serial.println("REASON: " + firebaseData.errorReason());
      Serial.println("------------------------------------");
      Serial.println();
    }
    newUserSentAllData = false;
  }
}
