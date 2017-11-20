/**
 * DocumentController
 *
 * @description :: Server-side logic for managing Documents
 * @help        :: See http://sailsjs.org/#!/documentation/concepts/Controllers
 */

var SkipperDisk = require('skipper-disk');
var fs = require('fs');

module.exports = {
    create: function (req, res) {

        let file = req.file('file');
        if (!file && file._files[0])
            return res.badRequest("No file");

        let filename;
        try {
            filename = file._files[0].stream.filename;
        } catch (e) {
            return res.badRequest("Error");
            return;
        }

        Document.create({ filename: filename })
            .exec(function (err, document) {
                if (err) return res.negotiate(err);
                
                //Document.subscribe(req, [document]);

                req.file('file').upload({
                    // don't allow the total upload size to exceed ~10MB
                    maxBytes: 10000000
                }, function whenDone(err, uploadedFiles) {
                    if (err) {
                        return res.negotiate(err);
                    }

                    // If no files were uploaded, respond with an error.
                    if (uploadedFiles.length === 0) {
                        return res.badRequest('No file was uploaded');
                    }

                    console.log({
                        // Grab the first file and use it's `fd` (file descriptor)
                        filepath: uploadedFiles[0].fd
                    });
                    Document.update(document.id, {
                    //Document.create({ filename: filename,
                        // Grab the first file and use it's `fd` (file descriptor)
                        filepath: uploadedFiles[0].fd
                    })
                        .exec(function (err,document) {
                            console.log(document);
                            if (err) return res.negotiate(err);
                            return res.json(document);
                        });
                });
            });
    },

    download: function (req, res){
          req.validate({
            id: 'string'
          });
        
          Document.findOne(req.param('id')).exec(function (err, document){
            if (err) return res.negotiate(err);
            if (!document) return res.notFound();
        
            if (!document.filepath) {
              return res.notFound();
            }
        
            var fileAdapter = SkipperDisk();
        
            // set the filename to the same file as the user uploaded
            res.set("Content-disposition", "attachment; filename='" + encodeURIComponent(document.filename) + "'");
            
            // Stream the file down
            fileAdapter.read(document.filepath)
            .on('error', function (err){
              return res.serverError(err);
            })
            .pipe(res);
          });
        },

    destroy: function (req, res){
        req.validate({
            id: 'string'
        });
        Document.destroy({id: req.param('id')}).exec(function (err, document){
            document = document[0];
            console.log(document);
            if (err) {
              return res.negotiate(err);
            }
            fs.unlink(document.filepath, function(err) {
                if (err) return res.negotiate(err); 
                //document.destroy();
                return res.ok({result:"Destroyed"});
              });
          });
    }
};

