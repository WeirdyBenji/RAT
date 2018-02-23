#!/usr/bin/python2

import nfc
import csv
import requests
import datetime
import time

clf = nfc.ContactlessFrontend()
clf.open('usb')

sorties = {}

while True:
    tag = clf.connect(rdwr={'on-connect': lambda tag: False})
    tag = str(tag).replace("Type2Tag 'NXP NTAG213' ID=", "")
    print(tag)
    r = requests.get('http://whatsupdoc.epitech.eu/check_rfid/epitech_check_rfid.php?uid={}'.format(tag))
    print (r.json())
    time.sleep(10)
