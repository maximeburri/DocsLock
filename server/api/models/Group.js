module.exports = {
  attributes: {
    name : {
      type : 'string',
      required : true
    },
    devices: {
      collection : 'device',
      via : 'group'
    },
    isLocked: {
      type : 'boolean',
      defaultTo : false
    }
  }
};