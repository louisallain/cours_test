var cahier = document.getElementById("dessin").getContext("2d")
var cahierTempsDeVol = document.getElementById("dessinTempsDeVol").getContext("2d")
var syracuseDemande = document.getElementById("computeSyracuseDemand");
var cahierHeight = document.getElementById("dessin").height
var cahierWidth = document.getElementById("dessin").width

cahier.font = "10px Helvetica"
 
syracuseDemande.addEventListener("submit", function (event) {

    event.preventDefault()

    cahier.clearRect(0, 0, cahierHeight, cahierWidth)
    
    var i
    var nb = document.getElementById("newValue").value;
    var nextNb = nb

    cahier.beginPath()

    cahier.moveTo(10, cahierHeight - nb)
    for(i = 0; i < 1000; i++) {

        nextNb = (nextNb % 2 == 0) ? (nextNb / 2) : (nextNb * 3 + 1)
        cahier.lineTo((i*10+20), cahierHeight - nextNb)
        cahier.fillText(nextNb, (i*10+20), cahierHeight - nextNb) 
    }

    cahier.stroke()
});


var i 
var cpt
cahierTempsDeVol.beginPath()
cahierTempsDeVol.moveTo(10, cahierHeight)
for(i = 0; i < 500; i++) {

    nextNb = i
    cpt = 0
    do {
        
        nextNb = (nextNb % 2 == 0) ? (nextNb / 2) : (nextNb * 3 + 1)
        cpt ++
    } while(nextNb != 4 && nextNb != 0)

    cahierTempsDeVol.lineTo((i*10+20), cahierHeight - cpt)
    cahierTempsDeVol.fillText(cpt, (i*10+20), cahierHeight - cpt) 
}
cahierTempsDeVol.stroke()