/**
 * Bootstrap
 * (sails.config.bootstrap)
 *
 * An asynchronous bootstrap function that runs before your Sails app gets lifted.
 * This gives you an opportunity to set up your data model, run jobs, or perform some special logic.
 *
 * For more information on bootstrapping your app, check out:
 * http://sailsjs.org/#!/documentation/reference/sails.config/sails.config.bootstrap.html
 */
var Notification = require('../api/services/NotificationService');
var Push = require('../api/services/PushDeviceService');

// Reset device isConnected websocket status
var initIsConnectedStatuses = function(){
  Device.update({/*all*/},{isConnected:false}).exec(
    function(err, updated){
        if (err) console.error("Error reset isConnected websocket status", isConnected);
  });
}

// Reset device isConnected websocket status
var setIsConnectedStatus = function(id, isConnected){
  Device.update({id:id},{isConnected:isConnected}).exec(
    function(err, updated){
        if (err) console.error("Set isConnected websocket status", "id", id, "error", err);
        else
          Device.publishUpdate(id, {isConnected:isConnected});
  });
}

module.exports.bootstrap = function(cb) {
  console.log("Bootstrap");

  initIsConnectedStatuses();

  sails.io.on('connection', function (socket, opts) {
    
    let deviceId = undefined;

    // If deviceId, store socket
    if(socket && socket.request && socket.request._query){
      deviceId = socket.request._query['deviceId'];

      if(deviceId !== undefined){
        console.log('Connect device ', deviceId);
        try{
          Notification.addDeviceSocket(deviceId, socket);
          setIsConnectedStatus(deviceId, true);
        } catch (err){
          console.log('Device already connected, disconnection');
          setIsConnectedStatus(deviceId, false);
          socket.disconnect();
        }
        Push.pushDevice(deviceId)
          .then(function(){
            console.debug("Device push message sended");
          })
          .catch(function(err){
              if(err) console.error("Can not send push device of ", deviceId);  
          });
      }else
        console.log('Connect user');
    }

    socket.on('disconnect', function (data) {
      if(deviceId !== undefined){
        console.log("Disconnect device ", deviceId)
        setIsConnectedStatus(deviceId, false);
        try{
          Notification.removeDeviceSocket(deviceId, socket);
        } catch (err){
          console.error(err, "deviceId:"+deviceId);
        }
      }else
        console.log("Disconnect user")
    });

  });

  // It's very important to trigger this callback method when you are finished
  // with the bootstrap!  (otherwise your server will never lift, since it's waiting on the bootstrap)
  cb();
};
