/**
 * DeviceController
 *
 * @description :: Server-side logic for managing Devices
 * @help        :: See http://sailsjs.org/#!/documentation/concepts/Controllers
 */
var Notification = require('../services/NotificationService');

var SHOW_NOTIFICATION = true;

module.exports = {
    push: function (req, res) {
        let id = req.param('id');

        Device
        .findOne({id: id})
        .populate("group")
        .exec(function (err, device) {
            if (err) {
                return res.serverError(err);
            }
            if (!device) {
                return res.notFound('Not found');
            }
            console.log(device);
            var message = {
                data : {
                    device : device
                }
            };

            if(SHOW_NOTIFICATION)
                message.notification = {
                    title: 'Push device',
                    body: 'Push device'
                };

            Notification.sendTo(device.firebaseToken, message,
                function (err, response) {
                    // Error
                    if (err) {
                        return res.serverError(err);
                    }
                    // Ok
                    res.ok("Pushed");
                }
            );
        });
    }
};

