#!/usr/bin/env python
#
#   Philippe Portes February 2018
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
import sys
import requests
import math
import struct
import binascii
import socket
import re
import netifaces as ni
from bluepy.btle import Scanner, DefaultDelegate, Peripheral, UUID, BTLEException

def speedfan(x):
    return {
        '\x41' : 'turbo',
        '\x21' : 'allergen',
        '\x11' : 'general_on',
        '\x09' : 'germ',
        '\x10' : 'attached',
        '\x00' : 'off'
    }[x]


def light(x):
    return {
        0 : 'on',
        1 : 'medium',
        2 : 'off',
    }[x]

def iaq(x):
    return {
    0 : 'green',
    4 : 'orange',
    8 : 'red'
    }[x]

class HFPStatus():
    def __init__(self):
        self.fanSpeed='off'
        self.voc='off'
        self.light='off'
        self.timer=0
        self.vociaq="green"

    def setVoc(self, voc):
        self.voc=voc

    def setVoCIAQ(self, vociaq):
        self.vociaq=vociaq

    def setFanSpeed(self, fanSpeed):
        self.fanSpeed=fanSpeed

    def getFanSpeed(self):
        return self.fanSpeed

    def setLight(self, light):
        self.light=light

    def setTimer(self, timer):
        self.timer=timer

    def status(self):
        return '{"hpa250b": {"fanSpeed":"'+self.fanSpeed+'","voc":"'+self.voc+'","vociaq":"'+self.vociaq+'","light":"'+self.light+'","timer":"'+str(self.timer)+'"}}'

class HFPProDelegate(DefaultDelegate):
    def __init__(self, bluetooth_adr,state,status):
        self.adr = bluetooth_adr
        self.status=status
        self.state=state
        self.fanSpeed='off'
        self.voc='off'
        self.light='off'
        self.timer=0

        print "Delegate initialized"
        DefaultDelegate.__init__(self)

    def handleDiscovery(self, dev, isNewDev, isNewdata):
        print "Advertiszing received #2 from:", dev.addr, " ours is:",self.adr
        if dev.addr == self.adr:
            print dev.getScanData()

    def handleNotification(self, cHandle, data):
        print "Notification received  from handle:", cHandle, " Data:",data
        if cHandle==0x37:
            self.status.setFanSpeed(speedfan(data[1]))
        elif cHandle==0x2e:
            print "Valid notification:"+binascii.hexlify(data)
            print self.status.status()
            try:
                if data[1] == '\x03':
                    self.status.setVoc('on')
                    self.status.setFanSpeed('na')
                    self.status.setVoCIAQ(iaq(int(binascii.hexlify(data[2]),16)>>2<<2))
                else:
                    self.status.setFanSpeed(speedfan(data[1]))
                    self.status.setVoc('off')
                # print "light value: ", int(binascii.hexlify(data[2]),16) & 0x03
                self.status.setLight(light(int(binascii.hexlify(data[2]),16)&0x03))

                self.status.setTimer(int(binascii.hexlify(data[4]),16))
            except:
                print "Unkown notification parameter"
            print self.status.status()

def sendBtCmd(perif, statushandler, string):

    print "Sending command:"+string
    perif.writeCharacteristic(0x25,string, withResponse=True)
    while True:
        if perif.waitForNotifications(1.0):
            break
    print "Received notification after command"

def get_network_interface_ip_address(interface='wlan0'):

    """

    Get the first IP address of a network interface.

    :param interface: The name of the interface.

    :return: The IP address.

    """


    while interface not in ni.interfaces():

        print('Could not find interface %s.' % (interface,))

        sleep(10)



    while True:
        interfacestring = ni.ifaddresses(interface)

        if (2 not in interfacestring) or (len(interfacestring[2]) == 0):

            print('Could not find IP of interface %s. Sleeping.' % (interface,))

            sleep(10)

            continue
        else:
            break
    return interfacestring[2][0]['addr']


def main():

    bluetooth_adr = sys.argv[1].lower()
    port = int(sys.argv[2])

    host = get_network_interface_ip_address(sys.argv[3])


    #BT HCI 0 or 1
    iface = int(sys.argv[4])

    print  "Will follow broadcasts from: ",bluetooth_adr, ". SmartThings DeviceHandler will have to be configured with IP:",host," and port: ",port
    print  "hci used: ",iface

    mac1,mac2,mac3,mac4,mac5,mac6=bluetooth_adr.split(':')
    status = HFPStatus()
    perif = Peripheral()
    perif.setDelegate(HFPProDelegate(bluetooth_adr,'init',status))

    #wait for commands
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)       # Create a socket object
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.settimeout(5.0)

    print "Connecting",
    while True:
        try:
            perif.connect(bluetooth_adr,iface=iface)
            perif.writeCharacteristic(0x25,"\x4d\x41\x43\x2b"+binascii.unhexlify(mac1)+binascii.unhexlify(mac2)+binascii.unhexlify(mac3)+binascii.unhexlify(mac4)+binascii.unhexlify(mac5)+binascii.unhexlify(mac6))
            perif.readCharacteristic(0x2e)
            perif.writeCharacteristic(0x2F,b"\x01\x00", withResponse=True)

            break
        except BTLEException:
            print ".",
    print ""
    print "Connected to ",bluetooth_adr

    try:
        s.bind((host, port))            # Bind to the port
    except:
        print "socket already bond"
        s.close()
        s = None
        s.bind((host, port))

    if s!=None:

        while True:
            try: # reactivate BT link by writing the Notification Characatestic every 5 sec
                perif.writeCharacteristic(0x2F,b"\x01\x00", withResponse=True)
                conn = None
                command= None
                s.listen(5)

                try:
                    conn, addr = s.accept()     # Establish connection with client.
                    #print 'Got connection from', addr
                    data = conn.recv(1024)
                    print('Server received', repr(data))
                    #extract the command from 'GET /api/commmand/value
                    command=re.findall('GET /api/(\S*)/',repr(data))

                    if command!=None:
                        print command[0]
                        # Parse value
                        value=re.findall('GET /api/'+command[0]+'/(\S*)', repr(data))
                        if value!=None:
                            #Commands
                            if command[0] == 'fanspeed':
                                # Fan Speed
                                # /api/fanspeed/4 turbo
                                # /api/fanspeed/3 allergen
                                # /api/fanspeed/2 general_on
                                # /api/fanspeed/1 germ
                                # /api/fanspeed/0 off
                                if status.getFanSpeed()!=value[0]:

                                    if value[0] == 'turbo': #'turbo':
                                        sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a510009316000000000000000000000000000000'))

                                    elif value[0] == 'allergen': #'allergen:
                                        sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a508009316000000000000000000000000000000'))

                                    elif value[0] == 'general_on': #'general':
                                        sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a504009316000000000000000000000000000000'))
                                    elif value[0] == 'germ': #'germ':
                                        if status.getFanSpeed()=='off':
                                            sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a501009316000000000000000000000000000000'))
                                        else:
                                            sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a502009316000000000000000000000000000000'))
                                    elif value[0] == 'off': #'off':
                                        sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a501009316000000000000000000000000000000'))

                                    else:
                                        print "Malformed/unknown value"
                            elif command[0] ==  'light':
                                        # /api/ligth/on
                                        # /api/light/medium
                                        # /api/light/off
                                if value[0] =='on':
                                    sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a500049316000000000000000000000000000000'))

                                elif value[0] == 'medium':
                                    sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a500049316000000000000000000000000000000'))

                                elif value[0] == 'off':
                                    sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a500049316000000000000000000000000000000'))

                                else:
                                    print "Malformed/unknown value"
                            elif command[0] == 'timer':
                                # /api/timer/plus
                                # /api/timer/minus
                                if value[0] == 'plus':
                                    sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a500019316000000000000000000000000000000'))

                                elif value[0] == 'minus':
                                    sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a500029316000000000000000000000000000000'))

                                else:
                                    print "Malformed/unknown value"
                            elif command[0] == 'voc':
                                        # /api/voc/on
                                        # /api/voc/off
                                if value[0] == 'on':
                                    sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a520009316000000000000000000000000000000'))

                                elif value[0] == 'off':
                                    sendBtCmd(perif, HFPProDelegate, binascii.unhexlify('a520009316000000000000000000000000000000'))

                                else:
                                    print "Malformed/unknown value"
                            elif command[0] == 'refresh':
                                 # do nothing and just resent the last status. Maybe notification(s) were received in-between to status got update despite the Smartthings side didn't get refreshed yet.
                                 print "Refresh will be sent"
                            else:
                                print "Unkown command"
                        else:
                            print "Malformed value"
                    #else:
                        #    print "Malformed command"
                    if conn != None:
                        conn.send('HTTP/1.1 200 OK\nContent-Type: application/json\n\n'+status.status())
                        conn.close()
                except socket.timeout:
                    #not received anything.
                    perif.waitForNotifications(1.0)
                    pass
            except BTLEException as e:
                print 'Error on line {}'.format(sys.exc_info()[-1].tb_lineno)
                print e
                while True:
                    try:
                        print "Trying to reach again ",bluetooth_adr
                        perif.connect(bluetooth_adr,iface=iface)
                        perif.writeCharacteristic(0x25,"\x4d\x41\x43\x2b"+binascii.unhexlify(mac1)+binascii.unhexlify(mac2)+binascii.unhexlify(mac3)+binascii.unhexlify(mac4)+binascii.unhexlify(mac5)+binascii.unhexlify(mac6))
                        perif.readCharacteristic(0x2e)
                        perif.writeCharacteristic(0x2F,b"\x01\x00", withResponse=True)
                        print "Re-connected BT-LE target"
                        break
                    except:
                        pass


if __name__ == "__main__":
    main()