
#include <WiFiManager.h>
#include "DHT.h"
#include<FirebaseESP32.h>
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

#define soilSensorPower 21
#define soilSensorIn 36
#define temperatureSensorIn 32
#define ventilationPIN 16
#define waterPumpPIN 23
#define API_KEY "AIzaSyCjjkBUNSBL9ScYEFGXXJzf2YYrYH8RwlY"
#define DATABASE_URL "https://projects-3rd-year-default-rtdb.asia-southeast1.firebasedatabase.app/"
#define USER_EMAIL "components@gmail.com"
#define USER_PASSWORD "123456789"

bool isAutoMode();
void controlWaterPump();
void controlVentilation();

DHT dht(temperatureSensorIn, DHT11);
bool ventilation = false, waterPump = false, autoMode = false;
int soilSensor, temperature, humidity;
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

void setup() {
  WiFi.mode(WIFI_STA);
  Serial.begin(9600);
  WiFiManager wm;
  bool res = wm.autoConnect("AutoConnectAP", "password"); // password protected ap
  if (!res) {
    Serial.println("Failed to connect\nRestarting...");
  }
  else {
    Serial.println("connected...yeey :)");
  }

  config.api_key = API_KEY;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;
  fbdo.setResponseSize(4096);
  config.token_status_callback = tokenStatusCallback;
  config.max_token_generation_retry = 5;
  config.database_url = DATABASE_URL;
  Firebase.begin(&config, &auth);

  Serial.println("Getting User UID");
  while ((auth.token.uid) == "") {
    Serial.print('.');
    delay(1000);
  }
  // Print user UID
  String uid = auth.token.uid.c_str();
  Serial.print("User UID: ");
  Serial.print(uid);

  pinMode(soilSensorPower, OUTPUT);
  digitalWrite(soilSensorPower, LOW);
  pinMode(ventilationPIN, OUTPUT);
  pinMode(waterPumpPIN, OUTPUT);
  dht.begin();

}
void loop() {

  //Soil Mositure Sensor Data
  digitalWrite(soilSensorPower, HIGH);
  delay(100);
  soilSensor = map(analogRead(soilSensorIn), 4096, 0, 0, 180);
  //soilSensor=analogRead(soilSensorIn);
  delay(100);
  Serial.print("Soil Mositure : ");
  Serial.println(soilSensor);
  digitalWrite(soilSensorPower, LOW);

  //    Temperature And Humidity Sensor Data
  temperature = dht.readTemperature();
  Serial.print("Temperature : ");
  Serial.println(temperature);
  humidity = dht.readHumidity();
  Serial.print("Humidity : ");
  Serial.println(humidity);

  Firebase.setInt(fbdo, F("/Data/Temperature"), temperature);
  Firebase.setInt(fbdo, F("/Data/SoilHumidity"), soilSensor);
  Firebase.setInt(fbdo, F("/Data/Humidity"), humidity);
  Firebase.getBool(fbdo, F("/Control/AutoMode"), &autoMode);

  if (isAutoMode()) {
    if (temperature > 25 && temperature < 60) {
      Firebase.setBool(fbdo, F("/Control/Ventilation"), true);
    } else {
      Firebase.setBool(fbdo, F("/Control/Ventilation"), false);
    }

    if (soilSensor > 85 && soilSensor <= 100) {
      Firebase.setBool(fbdo, F("/Control/WaterPump"), false);
    }
    else {
      Firebase.setBool(fbdo, F("/Control/WaterPump"), true);
    }
  }

  controlVentilation();
  controlWaterPump();
  delay(500);
}

void controlVentilation() {
  Firebase.getBool(fbdo, F("/Control/Ventilation"), &ventilation);
  Serial.print("Ventilation ");
  Serial.println(ventilation);
  if (!ventilation) {
    digitalWrite(ventilationPIN, HIGH);
  }
  else {
    digitalWrite(ventilationPIN, LOW);
  }
}
void controlWaterPump() {
  Firebase.getBool(fbdo, F("/Control/WaterPump"), &waterPump);
  Serial.print("WaterPump ");
  Serial.println(waterPump);
  if (waterPump) {
    digitalWrite(waterPumpPIN, HIGH);
  }
  else {
    digitalWrite(waterPumpPIN, LOW);
  }
}
bool isAutoMode() {
  Firebase.getBool(fbdo, F("/Control/AutoMode"), &autoMode);
  return autoMode == true ? true : false;
}
