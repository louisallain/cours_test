
#define RELAY_PIN 4

int state = 0;

void setup() {
  Serial.begin(115200);
  pinMode(RELAY_PIN, OUTPUT);
  pinMode(5, INPUT);
  digitalWrite(RELAY_PIN, 1);
}

void loop() {

  
  state = digitalRead(5);
  if(state == 0) {
    digitalWrite(RELAY_PIN, 0);
    delay(3000);
    digitalWrite(RELAY_PIN, 1);
  }
}
