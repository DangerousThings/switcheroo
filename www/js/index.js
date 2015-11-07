/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.bleScan();
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
    },
    // Scan for Switcheroos
    bleScan: function(){
        ble.scan(["00000015-9d7a-4919-b570-3bb24a4bf68e"], 5, function(switcheroo){
            console.log("connecting to ", switcheroo.id)
            app.switcherooConnect(switcheroo.id);
        }, function(e){
            handleError("Unable to scan for Switcheroos")
        });
    },
    // Connect to a Switcheroo
    switcherooConnect: function(deviceID){
        app.deviceID = deviceID;
        ble.connect(app.deviceID, app.switcherooConnected, function(e){
            alert("Unable to connect to Switcheroo")
        });
    },
    switcherooConnected: function(){
        console.log("Connected to a switcheroo", app.deviceID);
    },
    readSwitcherooID: function(deviceID){
        /*
        ble.read(deviceID, "1800", "2a00", function(buffer){
            console.log("Read ID", String.fromCharCode.apply(null, new Uint8Array(buffer)));
        }, function(e){
            handleError("Unable to read Device ID from" +deviceID)
        });
        */
    },
    switchPort: function(state, port, duration){
        data = new Uint8Array(4);
        if(state === "on"){
            data[0] = 1;
        }
        if(state === "off"){
            data[0] = 0;
        }
        if(duration){
            data[1] = duration;
        }
        ble.writeWithoutResponse(app.deviceID, 
            switcherooServicesAndCharacteristics[port].serviceID, 
            switcherooServicesAndCharacteristics[port].characteristicID, 
            data.buffer, 
            function(s){
                console.log("done writing data to port");
            }, 
            function(e){
                handleError("error writing");
            }
        );
    }
};

app.initialize();

function stringToBytes(string) {
   var array = new Uint8Array(string.length);
   for (var i = 0, l = string.length; i < l; i++) {
       array[i] = string.charCodeAt(i);
    }
    return array.buffer;
}

function handleError(e){
    alert("Error" + e)
}

$("body").on("click", ".switches > .switch", function(){
    var type = $(this).data("type"); // on or off
    var port = $(this).data("port"); // 1-->4
    if(type === "on"){
        app.switchPort(type, port);
    }
    if(type === "off"){
        app.switchPort(type, port);
    }
})

var switcherooServicesAndCharacteristics = {
  1: {serviceID: "00000015-9d7a-4919-b570-3bb24a4bf68e", characteristicID: "00000021-9d7a-4919-b570-3bb24a4bf68e"},
  2: {serviceID: "00000015-9d7a-4919-b570-3bb24a4bf68e", characteristicID: "00000022-9d7a-4919-b570-3bb24a4bf68e"},
  3: {serviceID: "00000015-9d7a-4919-b570-3bb24a4bf68e", characteristicID: "00000023-9d7a-4919-b570-3bb24a4bf68e"},
  4: {serviceID: "00000015-9d7a-4919-b570-3bb24a4bf68e", characteristicID: "00000024-9d7a-4919-b570-3bb24a4bf68e"}
}