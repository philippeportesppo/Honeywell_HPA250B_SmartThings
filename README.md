# Honeywell_HPA250B_SmartThings
Integration of Honeywell HPA250B into smartthings using Raspberry Pi and BluePi


Command line for py script on Raspberry Pi: sudo python hpa250b_agent.py [mac address] [port] "`ifconfig wlan0 | grep "inet " | awk -F'[: ]+' '{ print $4 }'`"

