var FCM = require('fcm-node');
var serverKey = process.env.FCM_KEY;
if (!serverKey)
    throw new Error("FCM_KEY variable not specified");
var fcm = new FCM(serverKey);

var send = function (message, callback) {
    console.log(message);
    fcm.send(message, callback);
};

var sendTo = function (token, message, callback) {
    message.to = token;
    send(message, callback);
};

module.exports = {
    send: send,
    sendTo: sendTo
}