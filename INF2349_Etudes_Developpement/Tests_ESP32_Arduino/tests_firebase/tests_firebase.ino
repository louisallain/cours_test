#include <WiFi.h>
#include "FirebaseESP32.h"

FirebaseData firebaseData;
#define FIREBASE_HOST "https://gestionnairesallesparempreinte.firebaseio.com"
#define FIREBASE_AUTH "HZ1pVMKT8OF0AKqnkIqx42eP4HISpECCwngS78Bl"
#define WIFI_SSID "Bbox-80CDC60A"
#define WIFI_PASSWORD "7y2uDWWK92utNXdCpq"

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

}

void loop() {

  if (Firebase.getInt(firebaseData, "/test/int")) {

    //if (firebaseData.dataType() == "int")) {
      Serial.println(firebaseData.intData());
    //}

  } else {
    Serial.println(firebaseData.errorReason());
  }

  delay(3000);

}
