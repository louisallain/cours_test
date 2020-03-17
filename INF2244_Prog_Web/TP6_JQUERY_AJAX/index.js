$(document).ready(function () {
  var courbeTemperature = {
    x: [],
    y: [],
    mode: 'markers',
    name: 'T°C'
  };
  var courbePression = {
    x: [],
    y: [],
    mode: 'lines',
    name: 'Pa'
  };

  $("#graph").dialog({
    title: "Météo",
    width: 700,
    height: 450,
    resize: function (event, ui) {  Plotly.react("graph",[courbeTemperature, courbePression]); }
  });

  // Au chargement de la page
  $("#news").load("http://localhost:3000/news");
  $.when(
      $.get("http://localhost:3000/temp", function (temp) {
        courbeTemperature.x=JSON.parse(temp).heure;
        courbeTemperature.y=JSON.parse(temp).temp;
      }),
      $.get("http://localhost:3000/pression", function (pression) {
        courbePression.x=JSON.parse(pression).heure;
        courbePression.y=JSON.parse(pression).pression;
      }),
    ).then(function() {
      Plotly.newPlot("graph", [courbeTemperature, courbePression]);
    });

  // On met à jour les données toutes les 4 secondes
  setInterval(function () {
   
    $("#news").load("http://localhost:3000/news");
    
    $.when(
      $.get("http://localhost:3000/temp", function (temp) {
        heure=JSON.parse(temp).heure;
        courbeTemperature.y=JSON.parse(temp).temp;
        courbeTemperature.x=JSON.parse(temp).heure;
      }),
      $.get("http://localhost:3000/pression", function (pression) {
        courbePression.y=JSON.parse(pression).pression;
        courbePression.x=JSON.parse(pression).heure;
      }),
    ).then(function() {
      Plotly.react("graph",[courbeTemperature, courbePression]);
    });
  }, 4000);
});