import time
import random
print("IOT Gateway")
import sys
from Adafruit_IO import MQTTClient
from simple_ai import image_detector
from uart import *

AIO_FEED_IDs = ["nutnhan1", "nutnhan2","ai"]
AIO_USERNAME = "nguyentrongtri"
AIO_KEY = "aio_GPRg164RbJg4jjrlOJ81dC7VAi8D"

def connected(client):
    print("Ket noi thanh cong ...")
    for topic in AIO_FEED_IDs:
        client.subscribe(topic)

def subscribe(client , userdata , mid , granted_qos):
    print("Subscribe thanh cong ...")

def disconnected(client):
    print("Ngat ket noi ...")
    sys.exit (1)

def message(client , feed_id , payload):
    print("Nhan du lieu: " + feed_id + " : " + payload)
    if feed_id == "nutnhan1":
        if payload == "1":
            writeData("1")
        else:
            writeData("2")
    elif feed_id == "nutnhan2":
        if payload == "1":
            writeData("3")
        else:
            writeData("4")
    elif feed_id == "ai":
        if payload.strip() == "UnMask":
            writeData("5")
        elif payload.strip() == "Mask":
            writeData("6")
        else:
            writeData("7")

client = MQTTClient(AIO_USERNAME , AIO_KEY)
client.on_connect = connected
client.on_disconnect = disconnected
client.on_message = message
client.on_subscribe = subscribe
client.connect()
client.loop_background()

counter = 0
while True:
    counter += 1
    if counter == 5:
        readSerial(client)
        result, imageBase64 = image_detector()
        client.publish("ai", result)
        client.publish("image", imageBase64)
        counter = 0
    time.sleep(1)