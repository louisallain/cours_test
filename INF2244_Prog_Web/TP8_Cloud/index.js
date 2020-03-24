
// Gère l'affichage du nombre de requêtes total
$("#nb-rq").load("http://localhost:3000/visiteurs")
setInterval(() => {
    $("#nb-rq").load("http://localhost:3000/visiteurs")
}, (30*1000))

// Affichage du calcul de la distance
$("#contact").submit((e) => {

    e.preventDefault()

    let user_key = $("#user-key").val()
    let A = $("#ADN-A").val()
    let B = $("#ADN-B").val()
    $("#res-json").load(`http://localhost:3000/${user_key}/distance/?A=${A}&B=${B}`)
})