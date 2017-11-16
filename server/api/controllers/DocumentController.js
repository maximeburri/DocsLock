/**
 * DocumentController
 *
 * @description :: Server-side logic for managing Documents
 * @help        :: See http://sailsjs.org/#!/documentation/concepts/Controllers
 */

module.exports = {
    create: function (req, res) {
        let file = req.file('file');
        if(!file && file._files[0])
            res.badRequest("No file");
        
        let filename;  
        try{
            filename = file._files[0].stream.filename;
        }catch(e){
            res.badRequest("Error");
            return;
        }

        Document.create({filename:filename})
            .exec(function (err, document) {
                if (err) return res.negotiate(err);

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

                    Document.update(document, {
                        // Grab the first file and use it's `fd` (file descriptor)
                        filepath: uploadedFiles[0].fd
                    })
                    .exec(function (err) {
                        if (err) return res.negotiate(err);
                        return res.json({result:"OK"});
                    });
                });
            });


    }
};

