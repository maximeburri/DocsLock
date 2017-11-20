/**
 * DeviceController
 *
 * @description :: Server-side logic for managing Devices
 * @help        :: See http://sailsjs.org/#!/documentation/concepts/Controllers
 */
var Notification = require('../services/NotificationService');

module.exports = {
    push: function (req, res) {
        let id = req.param('id');

        Device
        .findOne({id: id})
        .populateAll()
        .then(function (device) {

            var documents = [];
            if(device.group){
                documents = Document.find({"group":device.group.id}).then(
                    function (documents){
                        return documents;
                    }
                )
            }
            
            return [device, documents]; 
        }).spread(function(device, documents){
            device = device.toObject();

            if(device.group){
                device.group.documents = documents;
            }
            
            var message = {
                data : {
                    device : device
                }
            };

            Notification.sendTo(device.firebaseToken, message,
                function (err, response) {
                    // Error
                    if (err) {
                        return res.serverError(err);
                    }
                    // Ok
                    res.json(device);
                }
            );
        }).catch(function(err){
          if(err) return res.serverError(err);  
        });
    }
};

