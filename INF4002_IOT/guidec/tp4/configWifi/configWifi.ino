#include <Arduino.h>
#include <WiFi.h>
#include <EEPROM.h>
#include <ESP32WebServer.h>

// ==========================================================================
// SETTINGS MANAGEMENT
// ==========================================================================
#define EEPROM_ADDRESS        0
#define EEPROM_PARTITION_SIZE 128

#define COOKIE  3.14

struct Settings {
  float cookie;
  char ssid[32];
  char passwd[32];
  int channel;
  byte addr[4];
  int port;
};

Settings settings;

// ==========================================================================

// ==========================================================================
// ACCESS POINT
// ==========================================================================
#define AP_SSID "esp32_"
#define AP_PASS "bonjour56"

#define WEB_SERVER_PORT       80

ESP32WebServer web_server(WEB_SERVER_PORT);
WiFiClient client;
String mac;         // MAC address of the local device
// ==========================================================================

#define SWITCH_MODE_BUTTON 14
#define LED_WiFi  2

wl_status_t WiFi_status = WL_NO_SHIELD;

unsigned long WiFi_last_attempt               = 99999;
static unsigned long WiFi_attempt_interval    = 10000;

unsigned long last_sampling                   = 99999;
static unsigned long sampling_interval        = 5000;

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
// n : number of ssid find by scan
bool checkSSID(String ssid, int n) {

  for(int i=0; i<n; i++) if(ssid == WiFi.SSID(i)) return true;
  return false;
}

// ========================================================================
// Return the channel number used by network 'ssid', as detected during
// the last scan.  Return 0 if 'ssid' was not detected.
// n : number of ssid find by scan
int getChannel(String ssid, int n) {

  for(int i=0; i<n; i++) if(ssid == WiFi.SSID(i)) return WiFi.channel(i);
  return 0;
}

// ==========================================================================
// Check the current WiFi status (i.e., connected or not connected).
// When a connection or disconnection is detected, display information in
// the serial console, and switch the LED on or off accordingly.
// When a disconnection is detected, stop the WiFiClient if it was connected.
void check_WiFi_status() {

  WiFi_status = WiFi.status();
  if(WiFi_status == WL_CONNECTED) {
    digitalWrite(LED_WiFi, HIGH);
  }
  else {
    digitalWrite(LED_WiFi, HIGH);
  }
}

// ==========================================================================
// — on scanne les réseaux environnants;
// — on vérifie avec checkSSID() que le SSID du réseau visé a été détecté (sinon on abandonne);
// — on extrait avec getChannel() le n° de canal sur lequel se trouve l’AP ayant ce SSID;
// — on tente une connexion avec cet AP, en spécifiant le SSID, le mot de passe, et le canal utilisé
void start_WiFi_connection() {

  Serial.print("Tentative de connexion ");
  Serial.println(settings.ssid);
  Serial.println("Scan en cours...");
  int numSsid = WiFi.scanNetworks();
  if (numSsid == -1) {
    Serial.println("Connexion Wifi impossible");
    return;
  }

  bool ssidFound = checkSSID(settings.ssid, numSsid);
  // Vérifie qu'on trouve bien l'AP ayant ce SSID
  if(ssidFound) {
    Serial.print(settings.ssid);
    Serial.println(" Trouve");
    int numChannel = getChannel(settings.ssid, numSsid);
    if(numChannel != 0) {
      Serial.println("Tentative d'association...");
      WiFi.begin(settings.ssid, settings.passwd);
      check_WiFi_status();
    }
  }
  else {
    Serial.println("SSID non trouve");
  }
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
  if (client.connect(settings.addr, settings.port)) {

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
// SETTINGS MANAGEMENT
// ==========================================================================
void print_settings() {

  Serial.print("Cookie  : "); Serial.println(settings.cookie);
  Serial.print("SSID    : "); Serial.println(settings.ssid);
  Serial.print("Password: "); Serial.println(settings.passwd);
  Serial.print("Channel : "); Serial.println(settings.channel);
  Serial.print("Server  : ");
  Serial.print(settings.addr[0]); Serial.print(".");
  Serial.print(settings.addr[1]); Serial.print(".");
  Serial.print(settings.addr[2]); Serial.print(".");
  Serial.print(settings.addr[3]); Serial.print(":");
  Serial.println(settings.port);
}

// ==========================================================================
void save_settings(int addr) {

  Serial.println("Storing settings in EEPROM");
  EEPROM.put(addr, settings);
  if (! EEPROM.commit())
    Serial.println("ERROR: failed to write to EEPROM");
  // Serial.println("Done.");
}

// ==========================================================================
void read_settings(int addr) {

  Serial.print("Reading settings from EEPROM: ");
  EEPROM.get(addr, settings);

  if (settings.cookie == (float)COOKIE)
    Serial.println("OK");
  else {
    Serial.println("Failed (invalid cookie). Will use default values");
    settings.cookie = (float)COOKIE;
    strcpy(settings.ssid, "CHANGE_THIS");
    strcpy(settings.passwd, "********");
    settings.channel = 6;
    settings.addr[0] = 10;
    settings.addr[1] = 0;
    settings.addr[2] = 0;
    settings.addr[3] = 1;
    settings.port    = 80;
    save_settings(addr);
  }

  print_settings();
}

// ==========================================================================
// Enable the EEPROM to access a small partition, and read settings from
// this partition
void setup_EEPROM() {

  if (! EEPROM.begin(EEPROM_PARTITION_SIZE)) {
    Serial.println("ERROR: failed to initialize EEPROM");
    while(true);
  }

  read_settings(EEPROM_ADDRESS);
}

// ==========================================================================
// WEB SERVER
// ==========================================================================

void handleRoot() {

  String msg = "<!DOCTYPE HTML>\n";
  msg += "<html lang=\"en-US\">\n";
  msg += "<body>\n";
  msg += "<h1>ESP32 board</h1>";
  msg += "<h2>MAC=" + mac + "</h2>\n";
  msg += "<h2> <form action=\"/set\" method=\"get\">\n";
  msg += "<p>SSID:<br>\n";
  msg += "<input type=\"text\" name=\"ssid\" value=\"" + String(settings.ssid) + "\">\n";
  msg += "<p>Password:<br>\n";
  msg += "<input type=\"password\" name=\"passwd\" value=\"" + String(settings.passwd) + "\">\n";
  msg += "<p>Channel:<br>\n";
  msg += "<input type=\"number\" name=\"channel\" style=\"width: 3em\" min=\"0\" max=\"13\" value=\"" + String(settings.channel) + "\">\n";
  msg += "<p>Server (IP address):<br>\n";
  msg += "<input type=\"number\" name=\"ad1\" style=\"width: 3em\" min=\"0\" max=\"255\" value=\"" + String(settings.addr[0]) + "\">\n";
  msg += "<input type=\"number\" name=\"ad2\" style=\"width: 3em\" min=\"0\" max=\"255\" value=\"" + String(settings.addr[1]) + "\">\n";
  msg += "<input type=\"number\" name=\"ad3\" style=\"width: 3em\" min=\"0\" max=\"255\" value=\"" + String(settings.addr[2]) + "\">\n";
  msg += "<input type=\"number\" name=\"ad4\" style=\"width: 3em\" min=\"0\" max=\"255\" value=\"" + String(settings.addr[3]) + "\">\n";
  msg += "<p>Port:<br>\n";
  msg += "<input type=\"number\" name=\"port\" style=\"width: 5em\" min=\"0\" max=\"65535\" value=\"" + String(settings.port) + "\">\n";
  msg += "<p>\n";
  msg += "<button type=\"submit\">Submit</button>\n";
  msg += "</form></h2>\n";
  msg += "</body>\n</html>\n";

  web_server.send(200, "text/html", msg); 
}

// ==========================================================================
void handleSet() {

  settings.cookie = (float)COOKIE;
  strcpy(settings.ssid, web_server.arg(0).c_str()); // SSID
  strcpy(settings.passwd, web_server.arg(1).c_str()); // PASSWORD
  settings.channel = web_server.arg(2).toInt();
  settings.addr[0] = web_server.arg(3).toInt();
  settings.addr[1] = web_server.arg(4).toInt();
  settings.addr[2] = web_server.arg(5).toInt();
  settings.addr[3] = web_server.arg(6).toInt();
  settings.port    = web_server.arg(7).toInt();
  

  Serial.println("New configuration from client :");
  save_settings(EEPROM_ADDRESS);
  print_settings();
  
  web_server.send(200, "text/plain", "Configuration enregistree !");
}

// ==========================================================================
void handleNotFound() {

  String message = "Chemin inconnu\n\n";
  message += "URI: ";
  message += web_server.uri();
  message += "\nMethod: ";
  message += (web_server.method() == HTTP_GET)?"GET":"POST";
  message += "\nArguments: ";
  message += web_server.args();
  message += "\n";
  for (uint8_t i=0; i<web_server.args(); i++){
    message += " " + web_server.argName(i) + ": " + web_server.arg(i) + "\n";
  }
  web_server.send(404, "text/plain", message);
}

// ==========================================================================
// Initialize and start the Web server
void setup_Web_server() {

  Serial.println("Setting up the web server...");
  web_server.on("/", handleRoot); // racine du serveur web
  web_server.on("/set", handleSet); // lorsque le client soumet le formulaire
  web_server.onNotFound(handleNotFound); // chemin inconnu
  web_server.begin();
  Serial.println("Web server started !");
}

// ==========================================================================
// Wait for a Web client, process this client's request, then return
void handle_Web_client() {

  web_server.handleClient();
}


// ==========================================================================
// ACCESS POINT
// ==========================================================================

// ==========================================================================
// Enable the software access point mode on this device
//
// This AP will use address 10.0.0.1, and deliver addresses in range
// 10.0.0.0/24 to host stations.
//
// The SSID broadcast by the AP will be of the form 'ESP32_xxx', where 'xxx'
// is the MAC address of the device.
void setup_AP() {
  
  mac = WiFi.macAddress();
  String ssid = String(AP_SSID+mac);
  Serial.println("Starting AP...");
  WiFi.mode(WIFI_AP);
  WiFi.softAP(ssid.c_str(), AP_PASS);
  delay(100); // wait for AP_START..
  Serial.println("Set AP config...");
  IPAddress ip(10, 0, 0, 1);
  IPAddress netmask(255, 255, 255, 0);
  WiFi.softAPConfig(ip, ip, netmask);

  IPAddress tmpIP = WiFi.softAPIP();
  Serial.println("Config done, AP started");
  Serial.print("AP IP address: ");
  Serial.println(tmpIP);
}

void setup_configMode() {
  setup_EEPROM();

  setup_AP();

  setup_Web_server();

  Serial.println(); Serial.println();
}

void setup_modeData() {
  setup_WiFi_client();
}

// ==========================================================================
void setup() {

  // Button permettant de "switch" entre la mode configuration et le mode envoi de données
  pinMode(SWITCH_MODE_BUTTON, INPUT);
  digitalWrite(SWITCH_MODE_BUTTON, LOW);

  // Initialization of the serial console
  Serial.begin(115200);
}

// ==========================================================================
boolean modeConfigInitDone = false;
boolean modeDataInitDone = false;
void loop() {

  int buttonState = digitalRead(SWITCH_MODE_BUTTON);
   
  if(buttonState == HIGH) { // mode config
    
    modeDataInitDone = false; // réinitialise l'envoi de données
    if(modeConfigInitDone == false) {
       setup_configMode();
       modeConfigInitDone = true;
    }
    handle_Web_client();
  }
  else { // mode envoi de données
    
    modeConfigInitDone = false;
    if(modeDataInitDone == false) {
      setup_modeData();
      modeDataInitDone = true;
    }

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
      if (data != NULL) send(data);
    }
  }
}
