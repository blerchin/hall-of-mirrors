const express = require('express');
const fs = require('fs');
const app = express();
const fileUpload = require('express-fileupload');

//app.use(fileUpload());


app.post('/upload', function(req, res) {
    const filePath = (__dirname + '/uploads/' + Date.now() + '.jpg');
    fs.open(filePath, 'w', function(err, fd) {
        if (err) return res.status(500).send(err)
        req.on('data', function(data) {
            fs.write(fd, data, function(err) { if(err) res.status(500).send(err)});
        });
        req.on('end', function() {
            fs.close(fd, function(err) {
                if (err) { return res.status(500).send(err) }
                res.send("File uploaded");
            });
        });
    });
});

app.listen(8000);

