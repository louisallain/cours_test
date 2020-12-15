#include <RH_ASK.h>   // Include the RH_ASK library
#include <SPI.h> 
#include <dht.h>      // DHT11 Library

// Vitesse = 2000 bit/seconde
// Pin 11 = réception (non utilisée dans ce cas)
// Pin 12 = émission
RH_ASK radio(2000, 11, 12);

// DHT11
dht DHT;

// Pin du DHT
#define DHT11_PIN 4

// Identité
#define MY_ID 1

// Structure du payload à envoyer
struct payload_t {
  uint8_t sdr; // identité de l'émetteur
  uint8_t rcvr; // identité du receveur
  float temperature;
  float humidity;
} my_payload;

byte tx_buf[sizeof(my_payload)] = {0}; // initialise le buffer d'envoi
 
void setup()
{
    Serial.begin(9600);
 
    if (!radio.init()) {
         Serial.println("Radio module failed to initialize");
    }
    else {
      Serial.println("Radio module initialized");
    }
}
 
void loop()
{
    // Lit le capteur
    DHT.read11(DHT11_PIN);

    // Rempli une structure
    my_payload.sdr = MY_ID;
    my_payload.rcvr = 2;
    my_payload.temperature = DHT.temperature;
    my_payload.humidity = DHT.humidity;
 
    // Envoi le payload sous forme d'un tableau d'octets
    memcpy(tx_buf, &my_payload, sizeof(my_payload) );
    byte size_payload = sizeof(my_payload);
    radio.send((uint8_t *)tx_buf, size_payload);
 
    // Attend que le paquet soit envoyé
    radio.waitPacketSent();

    Serial.println("Data Sent : ");
    Serial.println(payload.temperature);
    Serial.println(payload.humidity);
    Serial.println();
    
    // Send data every 3 s
    delay(3000);
}
