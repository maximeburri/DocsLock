module.exports = {
  attributes: {
    mac : {
      type : 'string', 
      required : true 
    },
    isActive : { // Application is in front of device
      type : 'boolean', 
      required : true, 
      defaultsTo : false
    },
    isLocked : { // Application is in front of and locked
      type : 'boolean', 
      required : true, 
      defaultsTo : false
    },
    isConnected : { // WebSocket of device is connected
      type: 'boolean',
      required: true,
      defaultsTo: false
    },
    group: {
      model : 'group'
    },
    label : {
      type : 'string'
    }
  }
};

