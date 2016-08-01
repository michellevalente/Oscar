#include <SoftwareSerial.h>
#include <string.h>
#include <math.h>
#include "Servo.h"

// =============
// ENV CONFIG
// =============

#define _DEBUG

#define MAX_CODE 500
#define MAX_REACTIONS 3
#define _offset  -50

#define SERVO_LEFT     13
#define SERVO_RIGHT    12
#define SENSOR_ONE_IN  10
#define SENSOR_ONE_OUT  9
#define LED_BLUE        8
#define LED_RED         7
#define SENSOR_TWO_IN   3
#define SENSOR_TWO_OUT  2
#define BUZZER          4

#define FREQ_RANGE_LOWER 38000
#define FREQ_RANGE_UPPER 42000

const int RX_PIN = 0;
const int TX_PIN = 1;
const int BLUETOOTH_BAUD_RATE = 9600;

typedef enum ReactionType {
  GENERIC     = 0,
  CAR_BLOCKED = 1,
  TIMER       = 2,
  TEMPERATURE = 3
} ReactionType;

typedef struct Reaction {
  ReactionType type;
  char code[MAX_CODE];
  int codeLen;
  int codePos;
  int loopPos;
  long wait;
  boolean isTurning;
} Reaction;

// Creates and returns a new reaction
struct Reaction REACTION_new() {
  Reaction r;
    r.type    =  GENERIC;
    r.codeLen =  0;
    r.codePos =  0;
    r.loopPos = -1;
    r.wait    =  0;
    r.isTurning = false;
  return r;
}
 
SoftwareSerial bluetooth(RX_PIN, TX_PIN);
 
// Transmission info
unsigned long last_show, initial;
boolean receivingCode = true;
boolean recvReact = false;
int blockCount = 0;

// Reaction info
Reaction reactions[MAX_REACTIONS];
int nReactions  = 1;
int curReaction = 0;

// ==============
// CAR
// ==============
Servo servoLeft;                             
Servo servoRight;

// Actuator
void setServo(int speedLeft, int speedRight)
{
  servoLeft.writeMicroseconds(1500 + speedLeft);  
  servoRight.writeMicroseconds(1500 - speedRight);                                   
}

void CAR_setup() {
  servoLeft.attach(SERVO_LEFT);                      
  servoRight.attach(SERVO_RIGHT); 
  setServo(0,0);
}

// Sensing
int sensorDetect(int sensorLedPin, int sensorReceiverPin, long frequency) {
  tone(sensorLedPin, frequency, 8);             
  delay(1);                                
  int sensorRead = digitalRead(sensorReceiverPin);       
  delay(1); 
  // Return 0 if detect something  
  return sensorRead;                                
} 

int sensorDistance(int sensorLedPin, int sensorReceivePin) {  
  int distance = 0;
  for(long f = FREQ_RANGE_LOWER; f <= FREQ_RANGE_UPPER; f += 1000) {
    distance += sensorDetect(sensorLedPin, sensorReceivePin, f);
  }
  return distance;
}

// ========
// REACTIONS
// ========
void REACTION_setWait(Reaction* r, unsigned long now, unsigned long interval) {
  r->wait = now + interval;
}

// Executes a reaction code and returns whether its alive
boolean REACTION_runCode(Reaction* r) {
  int i;
  
  // Check for an active `wait`. If one exists, check if it's over. If it is, then
  //   reset the wait state and run the code; otherwise, just return;
  long now = millis();
  if (r->wait > now)
    return true;
  else {
    r->wait = 0;
    
    if (r->isTurning) {
      setServo(0,0);
      r->isTurning = false;
    }  
  }

  if (r->codePos < r->codeLen) { 
      char command[5];
      for (i = 0; i < 4; i++)
        command[i] = r->code[(r->codePos)++];
       command[i] = '\0';
       
      if (strcmp(command, "loop") == 0) {
         r->loopPos = r->codePos;
      }
      else if (strcmp(command, "ledr") == 0) { // red led
        digitalWrite(LED_RED, r->code[(r->codePos)++] == '1' ? HIGH : LOW); 
      }
      else if (strcmp(command, "ledb") == 0) { // blue led
        digitalWrite(LED_BLUE, r->code[(r->codePos)++] == '1' ? HIGH : LOW); 
      }
      else if(strcmp(command,"buzz") == 0) { // buzzer
        int timer;
        sscanf(r->code + r->codePos, "%d", &timer);
        r->codePos += floor(log10(abs(timer))) + 1;
        tone(BUZZER, 1500, timer);     
      } 
      else if(strcmp(command,"wait") == 0) { // buzzer
        int timer;
        sscanf(r->code + r->codePos, "%d", &timer);
        r->codePos += floor(log10(abs(timer))) + 1;
        REACTION_setWait(r, now, timer);
      }
      else if(strcmp(command,"move") == 0) { // buzzer
        if(r->code[(r->codePos)++] == '0')
          setServo(-200, -200);
        else
          setServo(200, 200);
      }
      else if(strcmp(command,"turn") == 0) { // buzzer
        Serial.println("TURN!");
        if(r->code[(r->codePos)++] == '0') {
          setServo(-200, 200);
          REACTION_setWait(r, now, 800);
        }
        else {
          setServo(200, -200);
          REACTION_setWait(r, now, 800);
        }
        r->isTurning = true;
      }
      else if(strcmp(command,"stop") == 0) {
        setServo(0,0);
        REACTION_setWait(r, now, 20);
      }
      
      return true;
  }
  // Go to start of the `loop`
  else if (r->loopPos >= 0) {
    r->codePos = r->loopPos; 
    return true;
  }
  
  return false; // program ended
}

// =========
// MAIN
// =========

void setup() {
  Serial.begin(9600);
  bluetooth.begin(BLUETOOTH_BAUD_RATE);
  pinMode(SENSOR_ONE_IN, INPUT);  
  pinMode(SENSOR_ONE_OUT, OUTPUT);   
  pinMode(SENSOR_TWO_IN, INPUT);  
  pinMode(SENSOR_TWO_OUT, OUTPUT);    
  pinMode(LED_RED, OUTPUT);  
  pinMode(LED_BLUE, OUTPUT);
  CAR_setup();
  last_show = millis();

  resetState();
}

void resetState() {
  receivingCode = true;
  recvReact     = true;
  curReaction   = 0;
  nReactions    = 1; // by default, only the main "trail"
  reactions[0]  = REACTION_new();
  blockCount = 0;
}

int checkReactions() {
  int i, reactionIdx = 0;
  
  for (i = 1; i < nReactions; i++) {
    // if reaction is dead ignore it
    int sensorLeft  = sensorDistance(SENSOR_ONE_OUT, SENSOR_ONE_IN);            
    int sensorRight = sensorDistance(SENSOR_TWO_OUT, SENSOR_TWO_IN); 

    // Check if car is blocked
    ReactionType type = reactions[i].type;
    if (type == CAR_BLOCKED && blockCount > 20)
        reactionIdx = i;
    else if (type = TIMER) {
      // if wait is -1 this is the first time this trail is executing, so
      // execute it for its `wait` to be set.
      if (reactions[i].wait == -1 || reactions[i].wait != 0 && millis() > reactions[i].wait)
        reactionIdx = i;
    }

    // If a reaction was found, leave
    if (reactionIdx)
      break;
  }

  return reactionIdx;
}

void loop() {
  char codeChar;

  // Receive code from app, if any available
  if (bluetooth.available()) {
    if(!receivingCode)
      resetState();

    Reaction* recv = &(reactions[nReactions - 1]);
    codeChar = (char)bluetooth.read();
    if (codeChar == '/')
      receivingCode = false;  
    else if (codeChar == '|') {
      nReactions++;
      reactions[nReactions-1] = REACTION_new();
      recvReact = true;
    }
    else {
      // The first char should set the type of the reaction
      if (recvReact) {
        switch (codeChar) {
          case '1': recv->type = CAR_BLOCKED; break;
          case '2': {
            Serial.println("Received timer");
            recv->type = TIMER;
            recv->wait = -1;
            break;
          }
          case '3': recv->type = TEMPERATURE; break;
          default:  recv->type = GENERIC;
        }
        recvReact = false;
      } 
      else
        recv->code[(recv->codeLen)++] = codeChar;
    }
  }

  // Check for blocking
  int sensorLeft  = sensorDistance(SENSOR_ONE_OUT, SENSOR_ONE_IN);            
  int sensorRight = sensorDistance(SENSOR_TWO_OUT, SENSOR_TWO_IN); 
  blockCount += ((!sensorLeft ? 1 : 0) + (!sensorRight ? 1 : 0));
  
#ifdef _DEBUG
  //digitalWrite(8, !sensorLeft ? HIGH : LOW); 
  //digitalWrite(7, !sensorRight ? HIGH : LOW);
#endif

  // Reset block count every 1ms
  if (millis() > last_show + 1000) {
    blockCount = 0; 
    #ifdef _DEBUG
        for (int i = 0; i < nReactions; i++) {
           Serial.print("code ");
           Serial.print(i);
           Serial.print(": ");
           Serial.println(reactions[i].code);
           Serial.print("  codePos:");
           Serial.println(reactions[i].codePos);
        }
         Serial.print("curReact: ");
         Serial.println(curReaction);
         Serial.print("nReacts: ");
         Serial.println(nReactions);
         last_show = millis();
    #endif
  }

  
  boolean isAlive, timerFirstRun = false;
  if (!receivingCode) {
    // First, check if we need to execute any reaction
    if (curReaction == 0)
      curReaction = checkReactions();

    // If reaction is a "Timer" and it's running for the first time
    // set the flag so control is returned to main trail
    if (reactions[curReaction].type == TIMER && reactions[curReaction].codePos == 0)
      timerFirstRun = true;

    isAlive = REACTION_runCode(&(reactions[curReaction]));
  }
  
  if (timerFirstRun) {
    curReaction = 0;
  }
  // code has finished, so receive new code
  else if (!isAlive) {
    if (curReaction) {
      reactions[curReaction].codePos = 0;
      reactions[curReaction].wait = 0;
      reactions[curReaction].isTurning = false;
      curReaction = 0; // return to main trail 
    }
    else
      resetState();
  }
}
