#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

#include <WiFi.h>
#include "FirebaseESP32.h"

#include <ArduinoJson.h>

#define FIREBASE_HOST "https://gestionnairesallesparempreinte.firebaseio.com"
#define FIREBASE_AUTH "HZ1pVMKT8OF0AKqnkIqx42eP4HISpECCwngS78Bl"

#define WIFI_SSID "Bbox-80CDC60A"
#define WIFI_PASSWORD "7y2uDWWK92utNXdCpq"

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define MAX_JSON            200

const char challenge[] = "cyberkey";
char * user_id = NULL;
char buf[512] = "";
char path[256] = "/public_keys/";
int i = 0;

StaticJsonBuffer<MAX_JSON> jsonBuffer;

FirebaseData firebaseData;

/**
 * Initialise la procédure d'authentification et de déverrouillage.
 */
void init_procedure() {
  
}

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      Serial.printf("\nNew connection");
    };

    void onDisconnect(BLEServer* pServer) {
      Serial.printf("\nConnection lost");
    }
};

class MyCharacteristicrCallbacks: public BLECharacteristicCallbacks {
  
  void onWrite(BLECharacteristic *pCharacteristic) {
    std::string value = pCharacteristic->getValue();

    // TODO : CORRIGER : parser tout value puis le traiter
    if (value.length() > 0) {
      
      strcpy(buf+i, value.c_str()); // car lit 20 octets max par lecture la caractéristique
      i = i + value.length();
    
      // le # permet à l'ESP32 de repérer si c'est les infos relatives à l'ouverture de la porte + fin de l'envoi
      if(buf[i-1] == '#') { // fin de l'envoi
        buf[i-1] = '\0'; // fin de la chaine donc
        // Parse le json reçu
        JsonObject& root = jsonBuffer.parseObject(buf);
        if (!root.success()) {
          Serial.printf("parseObject() failed");
          return;
        }
        user_id = root["user_id"];
        strcat(path, user_id);
        i = 0;

        // écriture de la chaine challenge
        pCharacteristic->setValue("cyberkey");
      }
      else if(buf[i-1] == '!') { // le ! permet à l'ESP32 de repérer si c'est la signature + fin de l'envoi
        buf[i-1] = '\0'; // fin de la chaine donc
        
        char pathToExponent[256] = "";
        char pathToModulus[256] = "";
        char exponent[20] = "";
        char modulus[600] = "";
        
        Serial.printf("\n . sig = %s", buf);
        Serial.printf("\n . path = %s", path);
        
        // Va cherche la clef publique de l'utilisateur et vérifie la signature
        strcpy(pathToExponent, path);
        strcat(pathToExponent, "/public_key/e");
        
        strcpy(pathToModulus, path);
        strcat(pathToModulus, "/public_key/n");
        
        Serial.printf("\n . exponent path = %s", pathToExponent);
        Serial.printf("\n . modulus path = %s", pathToModulus);
        
        Serial.printf("\n . Searching in database for the public key ...");
        
        // Récupère l'exposant
        if (Firebase.getString(firebaseData, pathToExponent)) {
          strcpy(exponent, firebaseData.stringData().c_str());
          Serial.printf("\n . Exponent = %s", exponent);    
        } 
        else {
          Serial.println("\n . " + firebaseData.errorReason());
          return;
        }
        // Récupère le module
        if (Firebase.getString(firebaseData, pathToModulus)) {
          
          strcpy(modulus, firebaseData.stringData().c_str());  
          Serial.printf("\n . Modulus = %s", modulus);

          // TODO : procédure de vérification de la signature

          int isSigVerify = 1; // entier servant de booléan, 1 = signature envoyé par l'utilisteur vérifié, 0 = signature non vérifiée donc utilisateur non authentifié
          if(isSigVerify == 1) {
            // TODO : Déverrouiller la porte

            // Réinitialisation
            i = 0;
            char * user_id = NULL;
            char buf[512] = "";
            char path[256] = "/public_keys/";
            
            Serial.printf("\n . Porte déverrouilée !!!");
            pCharacteristic->setValue("V"); // indique à l'utilisateur si il est authentifié ou non (si déverrouillage ou pas)
          }
          else {
            memset(user_id, 0, sizeof(user_id));
            memset(buf, 0, sizeof(buf));
            memset(path, 0, sizeof(path));
            pCharacteristic->setValue("F");
          }
        } 
        else {
          Serial.println(firebaseData.errorReason());
          return;
        }
      }
    }
  }
};

void setup() {
  Serial.begin(115200);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }

  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();
  
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
  Firebase.setMaxRetry(firebaseData, 3);
  Firebase.setReadTimeout(firebaseData, 1000 * 60);
  Firebase.setwriteSizeLimit(firebaseData, "tiny");
  
  Serial.println("Starting BLE work!");

  BLEDevice::init("Long name works now");
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  BLEService *pService = pServer->createService(SERVICE_UUID);
  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_WRITE
                                       );
  pCharacteristic->setCallbacks(new MyCharacteristicrCallbacks());

  pCharacteristic->setValue("");
  pService->start();
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");
}

void loop() {
  // put your main code here, to run repeatedly:
}
