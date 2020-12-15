#include <RH_ASK.h>
#include <SPI.h> // Not actualy used but needed to compile
#include <Wire.h>
#include <U8x8lib.h>

// Afficheur oled
U8X8_SSD1306_128X64_NONAME_HW_I2C u8x8(/* reset=*/ U8X8_PIN_NONE);

// pin de réception = 11
RH_ASK radio;

// Identité
#define MY_ID 2

// Structure du payload à envoyer
struct payload_t {
  uint8_t sdr; // identité de l'émetteur
  uint8_t rcvr; // identité du receveur
  float temperature;
  float humidity;
} my_payload;
 
void setup() {
  
    Serial.begin(9600);   // Use this for debugging

    // Initialisation de l'afficheur
    u8x8.begin();
  
    // Speed of 2000 bits per second
    // Use pin 11 for reception
    // Use pin 12 for transmission
    if (!radio.init())
    {
         Serial.println("Radio module failed to initialize");
    }
}

// Fonction utile avant d'écrire les résultats sur l'oled
void clearOled() {
  u8x8.setFont(u8x8_font_amstrad_cpc_extended_f);    
  u8x8.clear();
  u8x8.setCursor(0,1);
}
 
void loop() {
  
  uint8_t rx_buf[RH_ASK_MAX_MESSAGE_LEN]; // buffer de la taille max d'un message
  uint8_t rx_len = sizeof(rx_buf);
  
  if (radio.recv(rx_buf, &rx_len)) {

    radio.printBuffer("Receive=", rx_buf, rx_len);
    memcpy(&my_payload, rx_buf, sizeof(my_payload)); // recopie de la structure à partir du tableau d'octets

    // Vérifie si le message nous est destiné ou si broadcast
    if(my_payload.rcvr == MY_ID || my_payload.rcvr == 255) {
      // Affichage pour debugger
      Serial.println();
      Serial.print("sdr id: ");
      Serial.println(my_payload.sdr);
      Serial.print("rcvr id: ");
      Serial.println(my_payload.rcvr);
      Serial.print("temperature: ");
      Serial.println(my_payload.temperature);
      Serial.print("humidity: ");
      Serial.println(my_payload.humidity);
      Serial.println();

      // Affichage sur l'écran OLED
      clearOled();
      u8x8.print("Sender :");
      u8x8.println(my_payload.sdr);
      u8x8.print("Temp :");
      u8x8.println(my_payload.temperature);
      u8x8.print("Humi :");
      u8x8.println(my_payload.humidity);
    }
    else {
      // Juste pour debug
      Serial.println("Wrong destination");
    }
  }
}
