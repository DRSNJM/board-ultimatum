jQuery(document).ready(function ($) {

  $('#game-name').typeahead({source: taValues});

  $('.radio-buttons').each(function() {
    var radioValue = $(this).siblings(':input').val();
    if (radioValue != '') {
      $(this).children('button[value="' + radioValue + '"]').addClass('active');
    }
  });

  // set first to active
  $('.radio-buttons button').first().addClass('active');
  $('input[name=method]').val($('.radio-buttons button').first().val());


  // Alter radio buttons' hidden input based on which button was clicked
  $('.radio-buttons').on('click', '.btn', function(event) {
    $(this).parent().siblings('input[type=hidden]').val($(this).val());
  });

});
