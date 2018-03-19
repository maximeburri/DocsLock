module.exports = {
  attributes: {
    mac : {
      type : 'string', 
      required : true 
    },
    isActive : {
      type : 'boolean', 
      required : true, 
      defaultsTo : false
    },
    isLocked : {
      type : 'boolean', 
      required : true, 
      defaultsTo : false
    },
    group: {
      model : 'group'
    }
  }
};

