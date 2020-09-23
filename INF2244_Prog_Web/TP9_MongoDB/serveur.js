const MongoClient = require('mongodb').MongoClient;
const uri = "mongodb+srv://Louis:louisdu56@ecole-v3pdn.mongodb.net/test?retryWrites=true&w=majority";
const client = new MongoClient(uri, { useNewUrlParser: true });
const express = require('express')
const app = express()

// Utilise pug pour render la vue
app.set('view engine', 'pug');

// /noms/spécialités
app.get('/*/*', function (req, res) {

    let pathArray = req.path.split("/")
    let nom = pathArray[1].replace("-", " ").toUpperCase();
    let spec = pathArray[2].replace("-", " ").toUpperCase()
    
    client.connect(err => {
        
        if(err) {
            res.render('index', { title: 'Erreur', message: `Erreur : ${err}` })
        } 
        else {
            const collection = client.db("test").collection("tp9")
            
            collection.countDocuments({borough: {$regex: "^manhattan", $options:"i"}, cuisine: {$regex: "^piZzA", options: "i"} }, (err, items) => {
                
                res.render('index', { title: 'Bonjour', message: `Nombre de restaurants : ${items}` })
            })
        }
        client.close()
    });
})

// Nombre de restaurants
app.get('/', function (req, res) {

    client.connect(err => {
        
        if(err) {
            res.render('index', { title: 'Erreur', message: `Erreur : ${err}` })
        } 
        else {
            const collection = client.db("test").collection("tp9")
            collection.countDocuments((err, count) => {
                res.render('index', { title: 'Bonjour', message: `Nombre de restaurants : ${count}` })
            })
        }
        client.close()
    });
})

// Erreur 404 page inconnue
app.get('/*', function (req, res) {
    res.render('index', { title: 'Page inconnue', message: 'Erreur 404 : Page non trouvée'});
})

app.listen(3000, function () {
  console.log('Example app listening on port 3000!')
})