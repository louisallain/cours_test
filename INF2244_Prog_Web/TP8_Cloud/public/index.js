const protocole = "https"
const domaine_name = "htp8cloud.herokuapp.com"

// Gère l'affichage du nombre de requêtes total
$("#nb-rq").load(`${protocole}://${domaine_name}/visiteurs`)
setInterval(() => {
    $("#nb-rq").load(`${protocole}://${domaine_name}/visiteurs`)
}, (30*1000))

// Affichage du calcul de la distance
$("#contact").submit((e) => {

    // évite le rechargement de la page au "submit"
    e.preventDefault()

    let user_key = $("#user-key").val()
    let A = $("#ADN-A").val()
    let B = $("#ADN-B").val()
    $("#res-json").load(`${protocole}://${domaine_name}/${user_key}/distance/?A=${A}&B=${B}`)
})