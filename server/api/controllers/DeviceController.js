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
                documents = Document.find().populate("groups").then(
                    function (documents){
                        documents = 
                            documents.filter(d => 
                                d.groups && 
                                d.groups.map(g => g.id)
                                    .includes(device.group.id)
                            ).map(d => {
                                // Remove groups
                                // !? donst work ?
                                delete d.groups;
                                return d;
                            });
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

            Notification.sendTo(device.id, "update", message);
        }).catch(function(err){
          if(err) return res.serverError(err);  
        });
    }
};

