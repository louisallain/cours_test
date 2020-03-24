var cors = require('cors')
const levenshtein = require('js-levenshtein')
const express = require('express')
const app = express()

getDate = () => {

    var today = new Date();
    var date = today.getFullYear()+'-'+(today.getMonth()+1)+'-'+today.getDate();
    var time = today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds();
    var dateTime = date+' '+time;
    return dateTime
}

const ADN_regex = /^[CAGT]+$/
var users_keys_nb_rq = {
    "alan1234" : 0,
    "bertrand4567" : 0,
    "cloclo1234" : 0,
    "elodie4567" : 0,
    "fab1234" : 0,
}
const users_keys_nb_rq_origin = JSON.parse(JSON.stringify(users_keys_nb_rq))
var nb_rq_total = 0

// Toutes les minutes on reset le nombre de requêtes des utilisateurs
setInterval(() => {
    users_keys_nb_rq = JSON.parse(JSON.stringify(users_keys_nb_rq_origin))
}, (60 * 1000))

// Routes Express

app.use(cors());

app.get('/visiteurs', function (req, res) {
    res.send(`Nombre de requêtes total : ${nb_rq_total}`)
})

app.get('/*/distance*', function (req, res) {
    
    let user_key = req.path.split("/")[1]

    if(Object.keys(users_keys_nb_rq).includes(user_key)) {
        
        if(req.query.A && req.query.B) {

            if(req.query.A.length > 50 || req.query.B.length > 50) {
                res.json({
                    "utilisateur" : user_key,
                    "erreur" : "une des deux chaînes est trop longue (gardez des chaînes inférieures à 50)"
                })
            }
            else {
                
                if(req.query.A.match(ADN_regex) && req.query.B.match(ADN_regex)) {

                    users_keys_nb_rq[user_key] = users_keys_nb_rq[user_key] + 1
                    if(users_keys_nb_rq[user_key] > 5) {
                        res.json({
                            "utilisateur" : user_key,
                            "erreur" : "nombre de requêtes dépassé, attendez une minute"
                        })
                    } else {
                        let A = req.query.A
                        let B = req.query.B

                        let t0 = process.hrtime()
                        let distance = levenshtein(A, B)
                        let t1 = process.hrtime(t0)

                        res.json({ 
                            "utilisateur" : user_key,
                            "date" : getDate(),
                            "A" : A, 
                            "B" : B,
                            "distance" : distance,
                            "temps de calcul (ms)" : (t1[0]  + (t1[1]/1000000)),
                            "interrogations minute" : users_keys_nb_rq[user_key],
                        })
                        nb_rq_total = nb_rq_total + 1
                    }
                } 
                else {
                    res.json({
                        "utilisateur" : user_key,
                        "erreur" : "une des chaînes ne code pas de l’ADN"
                    })
                }
            }
        } 
        else {
            res.json({
                "utilisateur" : user_key,
                "erreur" : "Requête mal formée."
            })
        }
    } 
    else {
        res.json({
            "utilisateur" : user_key,
            "erreur" : "Accès non autorisé."
        })
    }
})

app.get('/*', function (req, res) {
    res.json({
        "utilisateur" : "",
        "erreur" : "Requête mal formée."
    })
})

app.listen(3000, function () {
    console.log('ADN MASTER 3000 Listening on port 3000 !')
})