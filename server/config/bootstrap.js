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

module.exports.bootstrap = function(cb) {
  console.log("Bootstrap");

  sails.io.on('connection', function (socket, opts) {
    
    let deviceId = undefined;

    // If deviceId, store socket
    if(socket && socket.request && socket.request._query){
      deviceId = socket.request._query['deviceId'];

      if(deviceId !== undefined){
        console.log('Connect device ', deviceId);
        try{
          Notification.addDeviceSocket(deviceId, socket);
        } catch (err){
          console.log('Device already connected, disconnection');
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

    /*socket.on('helloFromClient', function (data) {
        console.log('helloFromClient', data);
        socket.emit('helloFromServer', {server: 'says hello'});
    });*/

    socket.on('disconnect', function (data) {
      if(deviceId !== undefined){
        console.log("Disconnect device ", deviceId)
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
