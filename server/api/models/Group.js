module.exports = {
  attributes: {
    name : {
      type : 'string',
      required : true
    },
    devices: {
      collection : 'device',
      via : 'group'
    }
  }
};

