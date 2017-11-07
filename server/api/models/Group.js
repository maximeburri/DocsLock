module.exports = {
  attributes: {
    name : {
      type : 'string',
      required : true
    },
    isLocked: {
      type : 'boolean',
      defaultTo : false
    },
    devices: {
      collection : 'device',
      via : 'group'
    },
    documents: {
      collection : 'document',
      via : 'groups'
    }
  }
};