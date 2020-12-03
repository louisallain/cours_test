#include <Arduino.h>
#include <WiFi.h>

#define LED_WiFi  2

// ==========================================================================

WiFiClient client;
String mac;


#define NETWORK_SSID    "Bbox-80CDC60A"
#define NETWORK_PASSWD  "7y2uDWWK92utNXdCpq"
#define SERVER_ADDR     "10.0.0.1"
#define SERVER_PORT     6000

// ==========================================================================

wl_status_t WiFi_status = WL_NO_SHIELD;

unsigned long WiFi_last_attempt               = 99999;
static unsigned long WiFi_attempt_interval    = 10000;

unsigned long last_sampling                   = 99999;
static unsigned long sampling_interval        = 10000;

// ==========================================================================
// WI-FI MANAGEMENT
// ==========================================================================

// ==========================================================================
// Set up the Wi_Fi client
//
// - Enable the LED that will display the connectivity status
// - Enable mode WIFI_STA and display the MAC addresses
// - Set the client's timeout to 5 seconds
void setup_WiFi_client() {

  // utilisés pour convertir l'adresse mac en String arduino
  byte macBytes[6];
  char macCharArray[18];
  
  pinMode(LED_WiFi, OUTPUT); // led du statut de la connexion wifi
  
  WiFi.mode(WIFI_STA); // wifi en mode station
  WiFi.macAddress(macBytes); // récupération de l'adresse MAC de la carte

  // conversion du tableau d'octets en String
  sprintf(macCharArray, "%2X:%2X:%2X:%2X:%2X:%2X", macBytes[0], macBytes[1], macBytes[2], macBytes[3], macBytes[4], macBytes[5]);
  mac = String(macCharArray);
  Serial.println("MAC="); Serial.print(mac); Serial.println();

  // timeout du client fixé à 5 secondes
  client.setTimeout(5);
}

// ==========================================================================
// Return 'true' if 'ssid' has been detected during the last scan.
// Return 'false' otherwise.
bool checkSSID(String ssid) {

  Serial.println("TO BE COMPLETED");
}

// ========================================================================
// Return the channel number used by network 'ssid', as detected during
// the last scan.  Return 0 if 'ssid' was not detected.
int getChannel(String ssid) {

  Serial.println("TO BE COMPLETED");
}

// ==========================================================================
// Check the current WiFi status (i.e., connected or not connected).
// When a connection or disconnection is detected, display information in
// the serial console, and switch the LED on or off accordingly.
// When a disconnection is detected, stop the WiFiClient if it was connected.
void check_WiFi_status() {

  Serial.println("TO BE COMPLETED");
}

// ==========================================================================
// — on scanne les réseaux environnants;
// — on vérifie avec checkSSID() que le SSID du réseau visé a été détecté (sinon on abandonne);
// — on extrait avec getChannel() le n° de canal sur lequel se trouve l’AP ayant ce SSID;
// — on tente une connexion avec cet AP, en spécifiant le SSID, le mot de passe, et le canal utilisé
void start_WiFi_connection() {

  int numSsid = WiFi.scanNetworks();
  if (numSsid == -1) {
    Serial.println("Couldn't get a wifi connection");
    return;
  }

  Serial.println("Number of available networks=" + numSsid);
}

// ==========================================================================
// DATA ACQUISITION MANAGEMENT
// ==========================================================================

// ==========================================================================
// Try to get new samples, and return these samples as a C string (or NULL
// if no samples were obtained)
char *get_samples() {

  // Dummy implementation: should be replaced by real data sampling
  static char data[256];

  unsigned long now = millis();
  float temp = 18.0 + (now % 100) / 10.0;
  strcpy(data, "id=");
  strcat(data, mac.c_str());
  strcat(data, " temp=");
  sprintf(data+strlen(data), "%.1f", temp);

  return data;
}

// ==========================================================================
// Send data to the remote server (via a TCP session)
void send(char *data) {

  Serial.print("Connecting to server: ");
  if (client.connect(SERVER_ADDR, SERVER_PORT)) {

    Serial.println("OK");
    Serial.print("Sending data: ");
    Serial.println(data);

    client.println(data);

    Serial.println("Closing connection");
    client.stop();
  }
  else {
    Serial.println("failed");
  }
  Serial.println();
}

// ==========================================================================
// MAIN FUNCTIONS
// ==========================================================================


void setup() {

  Serial.begin(115200);

  setup_WiFi_client();

  Serial.println(); Serial.println();
}

// ==========================================================================
void loop() {

  unsigned long now = millis();

  // Check if the Wi-Fi status has changed
  check_WiFi_status();

  // If the Wi-Fi is currently "not connected"
  if (WiFi_status != WL_CONNECTED) {
    // If the last connection attempt was long ago, let us make another attempt
    if (now - WiFi_last_attempt > WiFi_attempt_interval) {
      WiFi_last_attempt = now;
      start_WiFi_connection();
    }
    // There is no need to go further until the Wi-Fi connection is established
    return;
  }

  // If the last data acquisition was long ago, let us start another one
  if (now - last_sampling > sampling_interval) {
    last_sampling = now;

    // Try to get new samples
    char *data = get_samples();

    // If new samples have been obtained, send these samples away
    if (data != NULL)
      send(data);
  }
}
