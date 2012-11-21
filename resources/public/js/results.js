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
  $('.open-recom').click(function() {
    $(this).find('.icon-chevron-down, .icon-chevron-up').toggle();
    var gameWell = $(this).parents('.game');
    gameWell.children('.recom').slideToggle('fast');
    var opts = {
      lines: 9, // The number of lines to draw
      length: 10, // The length of each line
      width: 6, // The line thickness
      radius: 13, // The radius of the inner circle
      trail: 50, // Afterglow percentage
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
          for (var i = 0; i < games.length; i++) {
            var game = games[i];
            var $clone = $('#recom-template').clone();
            $clone.removeAttr('id');
            $clone.find('img').attr('src', game['thumb']);
            $clone.find('.title').text(game['name']);
            gameWell.find('.similar').append($clone);
          }
          gameWell.find('.similar').children().fadeIn('slow');
        },
        error: function(jqXHR, textStatus, errorThrown) {
          gameWell.find('.similar').text('There was an error looking up similar games: ' + errorThrown)
        }
    });
  });
});