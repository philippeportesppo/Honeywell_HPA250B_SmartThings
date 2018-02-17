/**
 *  Honeywell_HFP250B
 *
 *  Copyright 2017 Philippe PORTES
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
import groovy.json.JsonSlurper 
 
metadata {
definition (name: "Honeywell_HPA250B", namespace: "philippeportesppo", author: "Philippe PORTES") 
{
	capability "actuator"
    capability "Sensor"
    capability "polling"
    capability "switch"
    command "fan_germ"
    command "fan_general"
    command "fan_allergen"
    command "fan_turbo"
    command "fan_off"
    command "light_off"
    command "light_medium"
    command "light_on"
    command "voc_off"
    command "voc_on"
    command "timer_minus"
    command "timer_plus"
    attribute "argument", "enum", ["on", "auto", "medium", "off", "germ", "general_on", "allergen","turbo","updating","plus", "minus"]
    attribute "command", "enum", ["fan_speed", "light", "voc","timer"]
}
preferences {
        section {
            input "internal_ip", "text", title: "Raspberry static IP", required: true
            input "internal_port", "text", title: "Raspberry port (12345)", required: true
            }		
        }
    
tiles (scale: 2) {
      
standardTile("fan", "device.fan", width: 6, height: 4, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
    state ("fan_off", label:'Off' /*, action:"fan_germ"*/, icon:"https://raw.githubusercontent.com/philippeportesppo/Honeywell_HPA250B_SmartThings/master/images/HPA250.png", backgroundColor:"#ffffff")
    state ("fan_germ", label:'Germ', action:"fan_general",  icon:"https://raw.githubusercontent.com/philippeportesppo/Honeywell_HPA250B_SmartThings/master/images/HPA250.png", backgroundColor:"#00a0dc")
    state ("fan_general_on", label:'General Clean', action:"fan_allergen", icon:"https://raw.githubusercontent.com/philippeportesppo/Honeywell_HPA250B_SmartThings/master/images/HPA250.png", backgroundColor:"#00a0dc")
    state ("fan_allergen", label:'Allergen', action:"fan_turbo", icon:"https://raw.githubusercontent.com/philippeportesppo/Honeywell_HPA250B_SmartThings/master/images/HPA250.png", backgroundColor:"#00a0dc")
    state ("fan_turbo", label:'Turbo', action:"fan_germ",  icon:"https://raw.githubusercontent.com/philippeportesppo/Honeywell_HPA250B_SmartThings/master/images/HPA250.png", backgroundColor:"#00a0dc")
    state ("fan_updating", label:'Sending...', backgroundColor:"#00a0dc")
    state ("fan_auto", label:'Auto(VOC)',icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/TVOC-Icon.png", backgroundColor:"#00a0dc")
 
}

standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
    state ("off", label:'Off', action:"switch.on", icon:"https://raw.githubusercontent.com/philippeportesppo/Honeywell_HPA250B_SmartThings/master/images/HPA250.png", backgroundColor:"#ffffff")
    state ("on", label:'On', action:"switch.off", icon:"https://raw.githubusercontent.com/philippeportesppo/Honeywell_HPA250B_SmartThings/master/images/HPA250.png", backgroundColor:"#00a0dc")
    state ("fan_updating", label:'Sending...', backgroundColor:"#00a0dc")
}

standardTile("timer_minus", "device.timer_minus", width: 2, height: 2, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
    state ("default", label:'-', action:"timer_minus", backgroundColor:"#ffffff")
}
standardTile("timer", "device.timer", width: 2, height: 2, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
    state ("default", label: 'Timer: ${currentValue}h', backgroundColor:"#ffffff")
}
standardTile("timer_plus", "device.timer_plus", width: 2, height: 2, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
    state ("default", label:'+', action:"timer_plus", backgroundColor:"#ffffff")
}
standardTile("light", "device.light", width: 2, height: 2, canChangeIcon: false, canChangeBackground: false,decoration: "flat",) {
    state ("light_off", label:'Off', action:"light_on", icon:"st.Lighting.light13", backgroundColor:"#ffffff" )
    state ("light_medium", label:'Medium', action:"light_off",  icon:"st.Lighting.light11", backgroundColor:"#00a0dc")
    state ("light_on", label:'On', action:"light_medium", icon:"st.Lighting.light11", backgroundColor:"#00a0dc")
	state ("light_updating", label:'Sending...')
    state ("off", label:'')

}
standardTile("voc", "device.voc", width: 2, height: 2, canChangeIcon: false, canChangeBackground: false, decoration: "flat",) {
    state ("voc_off", label:'Off', action:"voc_off", icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/TVOC-Icon.png", backgroundColor:"#ffffff")
    state ("voc_on_green", label:'On', action:"voc_on", icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/TVOC-Icon.png", backgroundColor:"#44b621")
    state ("voc_on_orange", label:'On', action:"voc_on", icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/TVOC-Icon.png", backgroundColor:"#d04e00")
    state ("voc_on_red", label:'On', action:"voc_on", icon:"https://raw.githubusercontent.com/philippeportesppo/AirMentorPro2_SmartThings/master/images/TVOC-Icon.png", backgroundColor:"#bc2323")

    state ("voc_updating", label:'Sending...')
    state ("off", label:'')
}
    main("fan")
    details(["fan","switch","light","voc","timer_minus","timer","timer_plus"])
    
}

}

def on()
{
	log.debug "on"

	fan_germ()
}

def off()
{
	log.debug "off"

	fan_off()
}

def timer_minus()
{
	log.debug "timer_minus"
	sendEvent(name: "argument", value: 'minus', display: true)
    sendEvent(name: "command", value: 'timer', display: true)

    refreshCmd()
}  

def timer_plus()
{
	log.debug "timer_plus"
	sendEvent(name: "argument", value: 'plus', display: true)
    sendEvent(name: "command", value: 'timer', display: true)

    refreshCmd()
} 
  
def voc_off()
{
	log.debug "voc_off"

	sendEvent(name: "voc",      value: "voc_updating" )
	sendEvent(name: "argument", value: 'off', display: true)
    sendEvent(name: "command", 	value: 'voc', display: true)

    refreshCmd()
}

def voc_on()
{
	log.debug "voc_on"
	sendEvent(name: "fanonoff", value: "on")
	sendEvent(name: "voc",      value: "voc_updating" )
	sendEvent(name: "argument", value: 'on', display: true)
    sendEvent(name: "command", value: 'voc', display: true)

    refreshCmd()
}
  
  
def light_off()
{
	log.debug "light_off"
	sendEvent(name: "light",     	value: "light_updating" )
	sendEvent(name: "argument", value: 'off', display: true)
    sendEvent(name: "command", value: 'light', display: true)

    refreshCmd()
}
  
def light_medium()
{
	log.debug "light_medium"

	sendEvent(name: "light",    value: "light_updating" )
	sendEvent(name: "argument", value: 'medium', display: true)
    sendEvent(name: "command",  value: 'light', display: true)

    refreshCmd()
}

def light_on()
{
	log.debug "light_on"
	sendEvent(name: "light",    value: "light_updating" )
	sendEvent(name: "argument", value: 'on', display: true)
    sendEvent(name: "command",  value: 'light', display: true)

    refreshCmd()
}

def fan_germ()
{
	log.debug "fan_germ"

	sendEvent(name: "switch", value: "on")

	sendEvent(name: "fan",      value: "fan_updating" )
	sendEvent(name: "argument", value: 'germ', display: true)
    sendEvent(name: "command",  value: 'fanspeed', display: true)

    refreshCmd()
}

def fan_general()
{
	log.debug "fan_general"

	sendEvent(name: "switch", value: "on")

	sendEvent(name: "fan",      value: "fan_updating" )
	sendEvent(name: "argument", value: 'general_on', display: true)
    sendEvent(name: "command", value: 'fanspeed', display: true)
	  
    refreshCmd()
}

def fan_allergen()
{
	log.debug "fan_allergen"

	sendEvent(name: "switch", value: "on")

	sendEvent(name: "fan",      value: "fan_updating" )
	sendEvent(name: "argument", value: 'allergen', display: true)
    sendEvent(name: "command", value: 'fanspeed', display: true)
	    
    refreshCmd()
}

def fan_turbo()
{
	log.debug "fan_turbo"

	sendEvent(name: "switch", value: "on")

	sendEvent(name: "fan",      value: "fan_updating" )
	sendEvent(name: "argument", value: 'turbo', display: true)
    sendEvent(name: "command", value: 'fanspeed', display: true)
   
    refreshCmd()
}

def fan_off()
{
	log.debug "fan_off"

	sendEvent(name: "switch", value: "off")

	sendEvent(name: "fan",      value: "fan_updating" )
	sendEvent(name: "argument", value: 'off', display: true)
    sendEvent(name: "command", value: 'fanspeed', display: true)
   
    refreshCmd()
}

def refresh_status()
{
	log.debug "refresh_status"

	sendEvent(name: "argument", value: 'na', display: true)
    sendEvent(name: "command", value: 'refresh', display: true)
   
    refreshCmd()
}
 

def installed() {
	log.debug "Executing 'installed'"
    
}

def updated() {
	log.debug "Executing 'updated'"
}

def initialize() {
}


def poll(){
log.debug "Executing 'poll'"
    refresh_status()
}


def parse(description) {
    def events = []
    def descMap = parseDescriptionAsMap(description)
    log.debug descMap
    def body = new String(descMap["body"].decodeBase64())
    log.debug body
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    log.debug "fan speed: ${result.hpa250b.fanSpeed}"
    log.debug "voc: ${result.hpa250b.voc}"
    log.debug "light: ${result.hpa250b.light}"
	log.debug "timer: ${result.hpa250b.timer}"
    
    
    if (result.hpa250b.voc == "on")
        events << createEvent(name: "fan",     	value: "fan_auto", isStateChanged:true )
    else
    	events << createEvent(name: "fan",     	value: "fan_${result.hpa250b.fanSpeed}", isStateChanged:true )
    if (result.hpa250b.fanSpeed == "off") {
    	events << createEvent(name: "light",    value: "off", isStateChanged:true)  
        events << createEvent(name: "voc", 	    value: "off", isStateChanged:true)
        events << createEvent(name: "switch", 	value: "off", isStateChanged:true)
        
        }

    else
    	{
    	events << createEvent(name: "light",     	value: "light_${result.hpa250b.light}", isStateChanged:true)   
        events << createEvent(name: "voc",     		value: "voc_${result.hpa250b.voc}_${result.hpa250b.vociaq}", isStateChanged:true)
        }
    events << createEvent(name: "timer",     	value: "${result.hpa250b.timer}", isStateChanged:true)   

    return events
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
        
        if (nameAndValue.length == 2) map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
        else map += [(nameAndValue[0].trim()):""]
	}
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    log.debug hexport
    return hexport
}

def refreshCmd() {
	log.debug "Executing refreshCmd"
	
    def host = internal_ip 
    def port = internal_port
    def levelCommand = 0x00
    def hosthex = convertIPtoHex(host)
    def porthex = convertPortToHex(port)
    log.debug "The device id before update is: $device.deviceNetworkId"
    device.deviceNetworkId = "$hosthex:$porthex" 
    
    log.debug "The device id configured is: $device.deviceNetworkId"
    def command = device.currentValue("command")
    def argument = device.currentValue("argument")
    def path = "/api/$command/$argument"
    log.debug "path is: $path"
 
    def headers = [:] 
    headers.put("HOST", "$host:$port")
 
  try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: path,
    	headers: headers
        )
  		state.requestCounter=1
        return hubAction
    
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
    
}


            
