var cahier = document.getElementById("dessin").getContext("2d")
var cahierHeight = document.getElementById("dessin").height
var cahierWidth = document.getElementById("dessin").width

var points = []
var nbPoints = 0

var chatmarche = new Image()
chatmarche.src = 'chatmarche.png'

var chatdort = new Image()
chatdort.src = 'chatdort.png'

var miaulement = new Audio('miaou.mp3')

document.addEventListener("click", clicked)
reset()

function reset() {

    points = []
    nbPoints = 0
    drawCanvas()
}

function drawCanvas() {

    cahier.fillStyle = "gold"
    cahier.fillRect(0, 0, cahierWidth, cahierHeight)

    cahier.fillStyle = "red"
    cahier.fillRect(0, 0, 120, 40)

    cahier.fillStyle = "black"
    cahier.font = "30px Helvetica"
    cahier.fillText("Effacer", 10, 30)

    cahier.fillStyle = "red"
    cahier.fillRect(150, 0, 120, 40)

    cahier.fillStyle = "black"
    cahier.font = "30px Helvetica"
    cahier.fillText("Ligne", 170, 30)

    cahier.fillStyle = "red"
    cahier.fillRect(300, 0, 120, 40)

    cahier.fillStyle = "black"
    cahier.font = "30px Helvetica"
    cahier.fillText("Animer", 315, 30)
}

function drawChat(x, y, img) {
    cahier.drawImage(img, x - (img.width/2), y - (img.height/2))
}

function drawChatAtPointNumber(n, img) {
    drawChat(points[n][0], points[n][1], img)
}

function drawChatAnimation() {

    document.removeEventListener("click", clicked) // interdit les cliques

    drawChatAtPointNumber(0, chatmarche)
    miaulement.play()
    var positionChat = 1

    var id = setInterval(() => {
        drawCanvas()
        if(positionChat == points.length-1) {
            drawChatAtPointNumber(positionChat, chatdort)
        } 
        else drawChatAtPointNumber(positionChat, chatmarche)
        positionChat = positionChat + 1
        if(positionChat == points.length) { // chat arrivé au dernier point
            document.addEventListener("click", clicked)
            clearInterval(id)
        }
    }, 500)
}

function clicked(event) {
    
    var x = event.pageX - event.target.offsetLeft
    var y = event.pageY - event.target.offsetTop
    if(x <= 120 && y <= 40) { // effacer
        reset()
    } else if (x >= 150 && x <= 270 && y <= 40) { // ligne

        cahier.beginPath()
        cahier.fillStyle = "black"
        cahier.moveTo(points[0][0], points[0][1])
        for(iCoords in points) {
           cahier.lineTo(points[iCoords][0], points[iCoords][1]) 
        }
        cahier.stroke()
    } else if(x >= 300 && x <= 420 && y <= 40) { // animer

        drawChatAnimation()
    } else { // ajouter point

        points.push([x, y])
        nbPoints = nbPoints + 1
        console.log(`Nombre de points : ${nbPoints}\n${points}`)

        cahier.fillStyle = "black"
        cahier.beginPath();
        cahier.arc(x, y, 3, 0, 2 * Math.PI, true);
        cahier.fill();  
    }
}