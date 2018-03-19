/**
 * DeviceController
 *
 * @description :: Server-side logic for managing Devices
 * @help        :: See http://sailsjs.org/#!/documentation/concepts/Controllers
 */
var Notification = require('../services/NotificationService');
var Push = require('../services/PushDeviceService');

module.exports = {
    push: function (req, res) {
        let id = req.param('id');

        Push.pushDevice(id)
        .then(function(){
            res.ok();
        })
        .catch(function(err){
            if(err) return res.serverError(err);  
        });
        
    }
};

