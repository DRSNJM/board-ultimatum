jQuery(document).ready(function ($) {
  $('.btn-input').find('input[type=hidden]').val('off');

  // make game selections toggle hidden input
  $('.btn-input').on('click', function(event){
    event.preventDefault();
    var input = $(this).find('input[type=hidden]');
    if ($(this).hasClass("active")) {
      input.val('on');
    } else {
      input.val('off');
    }
  });
});