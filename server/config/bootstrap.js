/**
 * Bootstrap
 * (sails.config.bootstrap)
 *
 * An asynchronous bootstrap function that runs before your Sails app gets lifted.
 * This gives you an opportunity to set up your data model, run jobs, or perform some special logic.
 *
 * For more information on bootstrapping your app, check out:
 * http://sailsjs.org/#!/documentation/reference/sails.config/sails.config.bootstrap.html
 */

module.exports.bootstrap = function(cb) {
  console.log("Bootstrap");
  sails.io.on('connection', function (socket) {
    console.log('Connected');
    socket.emit("Yo");
    socket.on('helloFromClient', function (data) {
        console.log('helloFromClient', data);
        socket.emit('helloFromServer', {server: 'says hello'});
    });

    socket.on('disconnect', function (data) {
      console.log("Disconnect")
    });

  });
  // It's very important to trigger this callback method when you are finished
  // with the bootstrap!  (otherwise your server will never lift, since it's waiting on the bootstrap)
  cb();
};
