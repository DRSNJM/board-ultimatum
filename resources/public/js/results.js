jQuery(document).ready(function ($) {
  $('.pop-trigger').popover({
    html: true,
    content: function() {
      return $(this).siblings('.pop-content').html();
    }
  });
  $('.game').hover(
    function() { $(this).find(".pop-trigger, .open-recom").fadeIn("fast"); },
    function() { $(this).find(".pop-trigger, .open-recom").fadeOut("fast"); }
  );

  $('.results').find('.tags, .desc').on('click', function(e) {
    $(this).closest('tr').find('.tags, .desc').toggle();
  });

  $('.open-recom').click(function() {
    $(this).find('.icon-chevron-down, .icon-chevron-up').toggle();
    var gameWell = $(this).parents('.game');
    gameWell.children('.recom').slideToggle('fast');
  });
  $('.open-recom').click(function(event) {
    $(this).unbind(event);
    var gameWell = $(this).parents('.game');
    var opts = {
      lines: 11, // The number of lines to draw
      length: 6, // The length of each line
      width: 4, // The line thickness
      radius: 9, // The radius of the inner circle
      trail: 60, // Afterglow percentage
    };
    var spinner = new Spinner(opts).spin();
    var serializedID = gameWell.children(':input').serialize();
    
    $.ajax({
        url: "/top-similar",
        type: "post",
        data: serializedID,
        beforeSend: function() {
          gameWell.find('.spin').html(spinner.el);
        },
        success: function(response, textStatus, jqXHR) {
          console.log("It worked! " + response);

          gameWell.find('.spin').remove();

          var games = jQuery.parseJSON(response)["games"];
          if (games.length > 0) {
            for (var i = 0; i < games.length; i++) {
              var game = games[i];
              var $clone = $('#recom-template').clone();
              $clone.removeAttr('id');
              $clone.find('img').attr('src', game['thumb']);
              $clone.find('.title').html("<b>" + game['name'] + "</b></br>" + game['rating'] + "% match");
              gameWell.find('.similar').append($clone);
            }
            gameWell.find('.similar').children().fadeIn('slow');
          } else {
            gameWell.find('.similar').text("No recommendations have been determined for this game.")
          }
        },
        error: function(jqXHR, textStatus, errorThrown) {
          gameWell.find('.similar').text('There was an error looking up similar games: ' + errorThrown)
        }
    });
  });
});
