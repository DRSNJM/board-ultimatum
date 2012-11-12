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
});