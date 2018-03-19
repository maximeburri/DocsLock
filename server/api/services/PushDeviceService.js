var Notification = require('../services/NotificationService');

// Send "update" message of device with id `id`
var pushDevice = function(id){
    let q = new Promise(
        (resolve, reject) => { 
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
                resolve();
            }).catch(function(err){
                reject();
            });
        }
    );

    return q;
}

module.exports = {
    pushDevice: pushDevice
}