global.devicesSockets = {};

// Add a socket associated with deviceId
var addDeviceSocket = function (deviceId, socket) {
    if (deviceId in global.devicesSockets)
        throw "Device already connected";
    global.devicesSockets[deviceId] = socket;
    
    console.log("Connected devices", Object.keys(global.devicesSockets));
};

// Remove a socket associated with deviceId
var removeDeviceSocket = function (deviceId, socket) {
    delete global.devicesSockets[deviceId];

    console.log("Connected devices", Object.keys(global.devicesSockets));
};

// Send to a device id a message
var sendTo = function (deviceId, room, message) {
    var socket = global.devicesSockets[deviceId];
    console.log(global.devicesSockets);
    if(socket === undefined)
        throw "Device not conencted";
    socket.emit(room, message);
};

module.exports = {
    addDeviceSocket: addDeviceSocket,
    removeDeviceSocket: removeDeviceSocket,
    sendTo: sendTo
}