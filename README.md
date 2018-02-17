# Honeywell_HPA250B_SmartThings
Integration of Honeywell HPA250B into smartthings using Raspberry Pi and BluePi

<h2><b>Hardware needed:</p>
<p>
<li> Honeywell HPA250B (Bluetooth enabled)</li>
<li> Raspberry Pi 3 rev 2 </li>
</p>
<p></p>
<p></p>
<h2>Raspberry configuration:<p></p>
<p>
<li>Bluez (http://www.elinux.org/RPi_Bluetooth_LE)</li>
<li>BluePy (https://github.com/IanHarvey/bluepy)</li>
<li>requests (http://raspberrypi-aa.github.io/session4/requests.html)</li>
<li>netifaces (python -m pip install netifaces)  
<li>Assign static IP address. Port 12345 will be used between Raspberry and SmartThings Hub.</li>
</p>

<p>Command line for py script on Raspberry Pi: <b>sudo python hpa250b_agent.py [mac address] [port] [inet] [hci port #] &</b>
</p>
<p> Example: sudo python hpa250b_agent.py fe:ed:fa:ce:da:ad 12345 wlan0 1 &
</p>
<li>SSDP UPnP Server: python ssdp_server_hpa250b.py &</li>
<h2>Configuration SmartThings:<p></p>
<p>Install the Honeywell HPA250B UPnP Service Manager from the app Smartapps -> myApps after you published it for yourself</p>
<p>Search for the Raspberry, select it, save. The Device will be created automatically.</p>
<p></p>
<h2> Pollster<p></p>
<p>Configure Pollster to frequently poll the HPA250B notifications in case you plan to use the device panel. I will work later on pushing the notification to SmartThings Handler. For now, the changes done on the device are either refreshed when a command is sent from the SmartThings app or when pollster polls the refresh. I use 1 minute pollster refresh on my side for now".

<h2> General architecture <p></p>
<img src=https://github.com/philippeportesppo/Honeywell_HPA250B_SmartThings/blob/master/HPA250B.png>
