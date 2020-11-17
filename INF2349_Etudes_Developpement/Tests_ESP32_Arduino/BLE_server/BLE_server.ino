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
char * user_id;
char buf[512];
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
        Serial.printf("\n  . user_id = %s", user_id);
        i = 0;

        // écriture de la chaine challenge
        pCharacteristic->setValue("cyberkey");
      }
      else if(buf[i-1] == '!') { // le ! permet à l'ESP32 de repérer si c'est la signature + fin de l'envoi
        buf[i-1] = '\0'; // fin de la chaine donc
        Serial.printf("\nsig = %s", buf);
        Serial.printf("\n user_id = %s", user_id);
        /*
        // Va cherche la clef publique de l'utilisateur et vérifie la signature
        char path[512] = "/public_keys/";
        strcat(path, user_id);
        strcat(path, "/public_key/e");
        Serial.printf("exponent path = %s", path);
        */
        /*
        if (Firebase.getString(firebaseData, "/test/int")) {
    
          Serial.println(firebaseData.stringData());    
        } 
        else {
          Serial.println(firebaseData.errorReason());
        }
        */
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
  delay(2000);
}
