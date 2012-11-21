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
    $(this).children('.icon-chevron-down, .icon-chevron-up').toggle();
    $(this).parents('.game').children('.recom').slideToggle('fast');
  });
});