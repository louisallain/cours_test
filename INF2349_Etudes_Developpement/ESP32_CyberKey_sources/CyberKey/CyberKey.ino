// BLE
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

// Wifi
#include <WiFi.h>

// Firebase
#include <FirebaseESP32.h>

// mbedtls
#if !defined(MBEDTLS_CONFIG_FILE)
#include "mbedtls/config.h"
#else
#include MBEDTLS_CONFIG_FILE
#endif
#include "mbedtls/rsa.h"
#include "mbedtls/md.h"

// SSD1306 (écran oled)
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#include <string.h>

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

// Taille de l'écran OLED
#define SCREEN_WIDTH 128 // OLED display width, in pixels
#define SCREEN_HEIGHT 64 // OLED display height, in pixels

// Pin de la led intégrée sur la carte
#define ONBOARD_LED  2

// Durée du timeout en ms d'un client mettant trop de temps à répondre
#define TIEMOUT_PROCEDURE_MS 3000 // 3 secondes

// Déclaration de l'afficheur OLED SSD1306 (SCL sur le pin 22 et SDA sur le pin 21)
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);


//////////////////////////////////////////////////////////////////////////////////////////////////////////

// Variables utilisées pour la connexion et les transmissions BLE
BLEServer *cyberKeyServer = NULL;
BLECharacteristic *tx_chall = NULL;
String user_id = ""; // Id de l'utilisateur courant en BDD
String user_sig = ""; // Signature de challenge de l'utilisateur courant

// Challenge à envoyer
String challenge_str = "cyberkey";

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
 * Booléen servant à repérer le moment où un nouveau client s'est connecté en BLE.
 * Ce booléen permettra ensuite dans la boucle du programme de positionner un timout dans le cas où
 * le client mettrait trop de temps à poursuivre la procédure.
 */
bool newUserConnected = false;

/**
 * Booléan servant à repérer le moment où un nouveau client a envoyé son identifiant en BDD.
 * Ce booléeen permettra ensuite dans la boucle du programme, de positionner un timeout dans le cas où
 * le client mettrait trop de temps à poursuivre la procédure.
 */
bool newUserSentId = false;

/**
 * Variable où sera sauvegardé le moment auquel un nouvel utilisateur s'est connecté en BLE.
 * Sert à définir le timeout après lequel un client qui s'est connecté n'a pas envoyé sont identifiant en BDD.
 */
unsigned long timeWhenNewUserConnect = 0.0;

/**
 * Varible où sera sauvegardé le moment auquel un nouvel utilisateur a envoyé sont identifiant en BDD.
 * Sert à définir le timeout après lequel un client qui a envoyé sont identifiant en BDD n'a pas envoyé la signature du challenge.
 */
unsigned long timeWhenUserSentID = 0.0;


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Vérifie une signature SHA256WithRSA d'une chaine de caractère.
 * @param N_str le module de la clef publique en hexadécimal.
 * @param E_str l'exposant de la clef publique en hexadécimal.
 * @param sig_SHA256WithRsa la signature en hexadécimal du texte.
 * @param text le texte dont la signature doit être vérifiée.
 * @return 0 si la signature est vérifiée sinon autre.
 */
int verifySHA256WithRSA(char * N_str, char * E_str, char * sigHex_SHA256withRSA, char * text)
{
    int ret = 1;
    int n = 0;
    unsigned c;
    size_t i;
    unsigned char hash[32];
    unsigned char buf[MBEDTLS_MPI_MAX_SIZE];
    mbedtls_rsa_context rsa;
    
    mbedtls_rsa_init( &rsa, MBEDTLS_RSA_PKCS_V15, 0 );

    if( ( ret = mbedtls_mpi_read_string( &rsa.N, 16, N_str ) ) != 0 ||
        ( ret = mbedtls_mpi_read_string( &rsa.E, 16, E_str ) ) != 0 )
    {
        Serial.printf( " failed\n  ! mbedtls_mpi_read_string returned %d\n\n", ret );
        goto exit;
    }

    rsa.len = ( mbedtls_mpi_bitlen( &rsa.N ) + 7 ) >> 3;

    i = 0;
    while( ( n = sscanf( sigHex_SHA256withRSA, "%02X", (unsigned int*) &c ) > 0) && i < (int) sizeof( buf ) ) {
        buf[i++] = (unsigned char) c;
        sigHex_SHA256withRSA += n*2; // n*2 car on lit octet par octet
    }
    
    if( i != rsa.len )
    {
        Serial.printf( "\n  ! Invalid RSA signature format \n\n" );
        goto exit;
    }

    /*
     * Compute the SHA-256 hash of given text and
     * verify the signature
     */
    Serial.printf( "\n  . Verifying the RSA/SHA-256 signature" );

    if( ( ret = mbedtls_md(
                    mbedtls_md_info_from_type( MBEDTLS_MD_SHA256 ),
                    text, strlen(text), hash ) ) != 0 )
    {
        Serial.printf( " failed\n  ! mbedtls_md returned -0x%04X\n\n", ret );
        goto exit;
    }

    if( ( ret = mbedtls_rsa_pkcs1_verify( &rsa, NULL, NULL, MBEDTLS_RSA_PUBLIC,
                                  MBEDTLS_MD_SHA256, 20, hash, buf ) ) != 0 )
    {
        Serial.printf( " failed\n  ! mbedtls_rsa_pkcs1_verify returned -0x%0x\n\n", (unsigned int) -ret );
        goto exit;
    }

    Serial.printf( "\n  . OK (the signature is valid)\n\n" );
  exit:
  
      mbedtls_rsa_free( &rsa );
      return ret;
}

/**
 * Classe où sont définis les callbacks du serveur BLE.
 */
class CyberKeyServerCallback: public BLEServerCallbacks {

    /**
     * A la connexion d'un client, log + nettoyage de l'afficheur oled.
     */
    void onConnect(BLEServer* pServer) {
      Serial.printf("\n . New BLE connection");
      display.clearDisplay();
      timeWhenNewUserConnect = millis(); // définit le moment où un nouveau client s'est connecté en BLE.
      newUserConnected = true; // repère le moment où un nouveau client s'est connecté en BLE.
    };

    /**
     * A la déconnexion d'un client, log.
     */
    void onDisconnect(BLEServer* pServer) {
      Serial.printf("\n . BLE connection lost");
    }
};

/**
 * Classe où sont définis les callbacks de la char. rx_user_id
 */
class Rx_user_id_callback: public BLECharacteristicCallbacks {

  /**
   * Lorsqu'un client écrit sur la caractéritisque rx_user_id, lui renvoi une chaine challenge (via la caractéristique tx_chall) dont il devra signé le contenu.
   * Cette signature, il devra ensuite l'envoyer sur la caractéristique rx_user_sig.
   */
  void onWrite(BLECharacteristic *pCharacteristic) {
    std::string value = pCharacteristic->getValue();

    if(value.length() > 0) {
      
      Serial.printf("\n . New value on rx_user_id :");

      user_id = String(value.c_str());
      Serial.print("\n . user_id=");
      Serial.print(user_id);
      
      tx_chall->setValue(challenge_str.c_str());
      tx_chall->notify();

      timeWhenUserSentID = millis(); // définit le moment où un nouveau client a transmis son identifiant en BDD.
      newUserSentId = true; // repère le moment où un nouveau client a transmis son identifiant en BDD.
    }
  }
};

/**
 * Classe où sont définis les callbacks de la char. rx_user_sig
 */
class Rx_user_sig_callback: public BLECharacteristicCallbacks {

  /**
   * Lorsqu'un client écrit sur la caractéritisque rx_user_sig, continue la procédure en allant chercher en BDD sa clef publique pour ensuite vérifier la signature reçue.
   */
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

  // Initialisation de la led de test
  pinMode(ONBOARD_LED,OUTPUT);

  // Initialisation du booléen permettant de savoir si l'utilisateur courant a envoyé toutes les données nécessaires à l'ouverture de la porte
  newUserSentAllData = false;

  // Initialisation BLE
  initBLEServer();

  // Initialisation de l'afficheur OLED SD1306
  if(!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) { // Address 0x3D for 128x64
    Serial.println(F("SSD1306 allocation failed"));
  }
  delay(100);
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(0, 10);
  display.println("Serrure CyberKey !");
  display.display();
}

void loop() {

  // Routine permettant de vérifier lorsqu'un client qui s'est connecté met trop de temps à poursuivre la procédure.
  if(newUserConnected && !newUserSentId && !newUserSentAllData) {
    // Timeout
    if(millis() > (timeWhenNewUserConnect + TIEMOUT_PROCEDURE_MS)) {
      Serial.print("\n . Délais après connexion expiré.");
      Serial.print("\n . Redémarrage\n\n\n");
      ESP.restart();
    }
  }

  // Routine permettant de vérifier lorsqu'un client qui a transmis son identifiant en BDD met trop de temps à poursuivre la procédure
  if(newUserSentId && !newUserSentAllData) {
    // Timeout
    if(millis() > (timeWhenUserSentID + TIEMOUT_PROCEDURE_MS)) {
      Serial.print("\n . Délais après envoie de l'identifiant expiré.");
      Serial.print("\n . Redémarrage\n\n\n");
      ESP.restart();
    }
  }
  
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

    // TODO : récupérer le créneau courant ET vérifier si l'utilisateur a effectivement accès à ce créneau
    
    String pathToPublicKey = "/public_keys/" + user_id + "/public_key";
    display.clearDisplay();
    display.setTextSize(1);
    display.setTextColor(WHITE);
    display.setCursor(0, 10);
    display.println("Recuperation");
    display.println("des donnees ...");
    display.display();
    if (Firebase.get(firebaseData, pathToPublicKey.c_str()))
    {
      Serial.println("\n . Récupération de clef publique OK");
      Serial.println(" . Chemin de la clef publique: " + firebaseData.dataPath());
      Serial.println(" . Type de données de la clef publique: " + firebaseData.dataType());
      display.clearDisplay();
      display.setTextSize(1);
      display.setTextColor(WHITE);
      display.setCursor(0, 10);
      display.println("Donnes");
      display.println("Recuperees...");
      display.display();
      
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
        int isVerify = -1;
        if ((isVerify = verifySHA256WithRSA(modulus.c_str(), exponent.c_str(), user_sig.c_str(), challenge_str.c_str())) != 0) {
          Serial.printf("\n  . La signature n'est pas vérifiée !");
          display.clearDisplay();
          display.setTextSize(1);
          display.setTextColor(WHITE);
          display.setCursor(0, 10);
          display.println("Signature RSA");
          display.println("Invalide");
          display.println("Porte verrouillée");
          display.display();
        }
        else {
          Serial.printf("\n . Signature vérifiée !");
          Serial.printf("\n . TODO : ouvrir la porte !!!");
          
          display.clearDisplay();
          display.setTextSize(1);
          display.setTextColor(WHITE);
          display.setCursor(0, 10);
          display.println("Signature RSA");
          display.println("Verifiee");
          display.println("Bienvenue !");
          display.display();
          for(int k = 0; k < 30; k++) {
            digitalWrite(ONBOARD_LED, HIGH); delay(50); digitalWrite(ONBOARD_LED, LOW); delay(50);
          }
        }  
      }
    }
    else
    {
      Serial.println(" . Récupération de clef publique ECHOUE");
      Serial.println(" . Raison: " + firebaseData.errorReason());
      display.clearDisplay();
      display.setTextSize(1);
      display.setTextColor(WHITE);
      display.setCursor(0, 10);
      display.println("Erreur");
      display.println("d acces BDD");
      display.display();
    }
        
    Serial.printf("\n . Redémarrage\n\n\n");
    newUserSentAllData = false;

    // Meilleur moyen (le plus rapide) que j'ai trouver pour tout réinitialiser sans problème de mémoire
    ESP.restart();
  }
}
