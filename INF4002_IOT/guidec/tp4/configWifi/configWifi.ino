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

#define AP_SSID "esp32_"
#define AP_PASS "bonjour56"

#define WEB_SERVER_PORT       80

ESP32WebServer web_server(WEB_SERVER_PORT);
WiFiClient client;
String mac;         // MAC address of the local device

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

// ==========================================================================
void setup() {

  // Initialization of the serial console
  Serial.begin(115200);

  setup_EEPROM();

  setup_AP();

  setup_Web_server();

  Serial.println(); Serial.println();
}

// ==========================================================================
void loop() {

  handle_Web_client();
}
