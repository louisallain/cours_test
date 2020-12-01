#include <Arduino.h>
#include <OneWire.h>

#define ONE_WIRE_PIN   4
#define MAX_DEVICES    32

// Creation of a OneWire object to access the 1-Wire bus,
// using the specified digital pin
OneWire ds(ONE_WIRE_PIN);

// Sensor identifiers will be stored in 'ids', each identifier occupying
// 8 consecutive bytes in this array (so the id of sensor #0 is stored
// in ids[0..7], the id of sensor #1 in ids[8..15], etc.)
uint8_t ids[8 * MAX_DEVICES];

// Number of ids contained in 'ids' (cannot be greater than MAX_DEVICES)
int nb_ids = 0;


// ---------------------------------------------------------------------------
// Display an array of bytes in hexadecimal representation
void show_bytes(uint8_t *addr, int nb_bytes) {

  for (int i=0; i < nb_bytes; i++) {
    if (addr[i] < 0x10) Serial.write('0');
    Serial.print(addr[i], HEX);
    Serial.print(' ');
  }
}

// ---------------------------------------------------------------------------
// Check the CRC in an array of bytes (assuming the last byte contains the CRC
// value for the rest of the array)
bool check_CRC(uint8_t *addr, int nb_bytes) {

  return (OneWire::crc8(addr, nb_bytes - 1) == *(addr + nb_bytes - 1));
}

// ---------------------------------------------------------------------------
// Display the ids of all known sensors
void show_all_ids() {

  for (int i=0; i < nb_ids; i++) {
    int offset=i * 8;
    Serial.print('#'); Serial.print(i); Serial.print(": ");
    show_bytes(ids + offset, 8);
    Serial.println();
  }
}

// ---------------------------------------------------------------------------
// Scan the 1-Wire bus and identify all DS18B20 sensors on this bus
int scan_bus() {

  byte addr[8]; // stock l'addresse courante du capteur trouvé
  int i = 0; // compte le nombre de capteur trouvé

  while(true) {
    
    if(!ds.search(addr)) {
      Serial.println("Fin du scan");
      ds.reset_search(); // réinitialise le compteur interne de la fonction "search".
      nb_ids = i; // fixe le nombre de capteurs de température trouvés.
      return i; // fin de la recherche
    }

    // Vérifie qu'il s'agit bien d'un capteur DS18B20 (addr[0] == 0x28) ou d'un capteur DS18S20 (addr[0] == 0x10)
    if ((addr[0] != 0x28) && (addr[0] != 0x10)) {
      // Mauvais type de capteur
      Serial.println("Mauvais capteur");
    }
    else {

      // Vérifie l'addresse du capteur
      if (!check_CRC(addr, 8)) {
        Serial.println("CRC non valide");
      }
      else {
        int offset = i*8; // offset permettant de sauvegarder l'id du capteur trouvé dans le tableau des id "ids".
        for(byte b = 0; b<8; b++) {
          ids[b+offset] = addr[b]; // sauvegarde l'id dans le tableau des ids
        }
        i++; // incrémente le nombre de capteurs de température trouvés sur le bus.
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Start measurement on all known sensors by issuing the same command to all
// sensors simultaneously
bool start_measurement() {

  ds.reset(); // Initialise le bus 1-wire avant communication.
  ds.skip(); // saute l'étape de la sélection du composant pour pouvoir envoyer la commande à tous les capteurs.
  ds.write(0x44); // commande de conversion vers les capteurs.
  Serial.println("Measurement started");
}

// ---------------------------------------------------------------------------
// Get the temperature from sensor #i, and set the value of 'temp' accordingly
// Return 'true' if the temperature was obtained, and 'false' otherwise
bool get_temperature(int i, float *temp) {

  int offset = i*8; // offset permettant de sauvegarder l'id du capteur trouvé dans le tableau des id "ids".

  byte addr[8]; // stock l'addresse du capteur courant.
  byte data[9]; // stock les données de température du capteur courant.

  for(byte k = 0; k<8; k++) addr[k] = ids[k+offset]; // récupère l'addresse du capteur courant.
  ds.reset(); // Initialise le bus 1-wire avant communication.
  ds.select(addr);
  ds.write(0xBE); 
  // lecture du "scratchpad" du capteur
  for(byte j = 0; j<9; j++) data[j] = ds.read(); // lit les 9 octets du scratchpad
  if(!check_CRC(data, 9)) { // vérifie la somme de contrôle pour la température
    Serial.println("CRC non valide");
    return false;
  }
  
  int16_t tempData = (data[1] << 8) | data[0]; // récupère les octets de données représentant la température, le bit le plus significatif à gauche
  float t;
  if(addr[0] == 0x10) { // cas du DS18S20
    tempData << 3; // la précision par défaut est 9 bits
    t = (float)(tempData*0.0625); // la précision par défaut est sur 12 bits ( 0.0625°C selon la doc).
    if(t == 85.0) return false; // cas d'erreur
    *temp = t;
    return true;
  }
  else {
    t = (float)(tempData*0.0625); // la précision par défaut est sur 12 bits ( 0.0625°C selon la doc).
    if(t == 85.0) return false; // cas d'erreur
    *temp = t;
    return true;
  }
}

// ---------------------------------------------------------------------------
void setup() {

  Serial.begin(9600); // Initalize the serial console

  while (! Serial) {}; // Wait for the serial console to open

  // Scan the 1-Wire until at least one DS18B20 sensor has been found
  scan_bus();
  while (nb_ids == 0) {
    delay(5000);
    scan_bus();
  }

  // Display the identifiers of all DS18B20 sensors
  show_all_ids();
  Serial.println();
}

// ---------------------------------------------------------------------------
void loop() {

  // Send a 'Start convert' command to all DS18B20 sensors so they start
  // measuring the temperature
  Serial.println(F("Starting measurement on all known sensors"));
  start_measurement();

  delay(800);  // Wait while temperature is being measured

  // For each known sensor, get the temperature that has just been measured,
  // and display this temperature
  Serial.println(F("Getting temperatures from all known sensors"));
  float temp;
  for (int i=0; i < nb_ids; i++) {
    Serial.print('#'); Serial.print(i); Serial.print(": ");
    if (get_temperature(i, &temp)) {
      Serial.print(temp, 1);
      Serial.println("°C");
    }
  }
  Serial.println();

  // Wait for a while, and then loop
  delay(5000);
}