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
    $(this).parents('.game').children('.recom').slideToggle('fast');
    var opts = {
      lines: 9, // The number of lines to draw
      length: 10, // The length of each line
      width: 6, // The line thickness
      radius: 13, // The radius of the inner circle
      trail: 50, // Afterglow percentage
    };
    var spinner = new Spinner(opts).spin();
    $(this).parents('.game').find('.spin').html(spinner.el);
  });
});