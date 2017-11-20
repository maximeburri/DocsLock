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
            var documents = Document.find({"group":device.group.id}).then(
                function (documemts){
                    return documemts;
                }
            )
            return [device, documents]; 
        }).spread(function(device, documemts){
            device = device.toObject();
            device.group.documemts = documemts;
            
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

