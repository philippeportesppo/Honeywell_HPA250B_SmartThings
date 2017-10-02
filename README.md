# Honeywell_HPA250B_SmartThings
Integration of Honeywell HPA250B into smartthings using Raspberry Pi and BluePi

<p><b>Hardware needed:</b></p>
<p>
<li> Honeywell HPA250B (Bluetooth enabled)</li>
<li> Raspberry Pi 3 rev 2 </li>
</p>
<p></p>
<p>
<b>Raspberry configuration:</b></p>
<p>
<li>Bluez (http://www.elinux.org/RPi_Bluetooth_LE)</li>
<li>BluePy (https://github.com/IanHarvey/bluepy)</li>
<li>requests (http://raspberrypi-aa.github.io/session4/requests.html)</li>
<li>Assign static IP address and use a dedicated port (12345 works fine) between Raspberry and SmartThings Hub.</li>
</p>

<p>Command line for py script on Raspberry Pi: <b>sudo python hpa250b_agent.py [mac address] [port] "`ifconfig wlan0 | grep "inet " | awk -F'[: ]+' '{ print $4 }'`" &</b>
</p>

<p><b>Configuration SmartThings:</b></p>
<p>Configure the Raspberry IP address with the static IP you defined</p>
<p>Configure the Raspberry Port with the port (12345) you defined</p>
<p></p>
<p><b> Configure Pollster to frequently poll the HPA250B notifications in case you plan to use the device panel. I will work later on pushing the notification to SmartThings Handler. For now, the changes done on the device are either refreshed when a command is sent from the SmartThings app or when pollster polls the refresh. I use 1 minute pollster refresh on my side for now".

<p> General architecture </p>
<img src=https://github.com/philippeportesppo/Honeywell_HPA250B_SmartThings/blob/master/HPA250B.png>
