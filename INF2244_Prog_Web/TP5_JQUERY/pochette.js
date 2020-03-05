$(document).ready(function () {

  $(".partie").click(function() {

      if ($(this).attr("id") === "img-a") {

        $("#img-b, #img-d, #img-e").animate({
            width: '100px'
        });

        $("#img-a").animate({
          width: '470px'
        });

      } else if ($(this).attr("id") === "img-b") {

        $("#img-a, #img-d, #img-e").animate({
          width: '100px'
        });

        $("#img-b").animate({
          width: '470px'
        });

      } else if ($(this).attr("id") === "img-c") {

        $("#img-a, #img-b, #img-d, #img-e").animate({
          width: '100px'
        });
      }

      else if ($(this).attr("id") === "img-d") {

        $("#img-a, #img-b, #img-e").animate({
          width: '100px'
        });

        $("#img-d").animate({
          width: '470px'
        });

      }

      else if ($(this).attr("id") === "img-e") {

        $("#img-a, #img-b, #img-d").animate({
          width: '100px'
        });

        $("#img-e").animate({
          width: '470px'
        });
      }
  });
});