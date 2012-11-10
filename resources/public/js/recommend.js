function updateActiveDisplay(listElem, speed, input) {
  if (arguments.length == 2) {
    input = $('input[name=' + listElem.attr('id') + '-active]');
  }
  if (input.val() == 'true') {
    listElem.addClass('active');
    $('#input-' + listElem.attr('id')).show(speed);
  } else {
    listElem.removeClass('active');
    $('#input-' + listElem.attr('id')).hide(speed);
  }
}

function updateTriStateDisplay(button, formInput) {
  if (arguments.length == 1) {
    formInput = button.parent().siblings('input[type=hidden]');
  }
  // If the sent button is not the option (center) button then find it
  var $option = button.hasClass('option') ? button : button.siblings('.option');
  // State is not 0 so set the appropriate class on the option button.
  if (formInput.val() != '0') {
    $option.addClass(formInput.val() == 1 ? 'btn-success' : 'btn-danger');
  }
}

jQuery(document).ready(function ($) {
  // If any inputs are set (e.g. from back button), alter interface to show
  $('#select li').each(function() {
    updateActiveDisplay($(this), 0);
  });
  $('.option').each(function() {
    updateTriStateDisplay($(this));
  });
  $('input:checked').each(function() {
    $(this).siblings('.icon').addClass('active');
  });
  $('.radio-buttons').each(function() {
    var radioValue = $(this).siblings(':input').val();
    if (radioValue != '') {
      $(this).children('button[value|="' + radioValue + '"]').addClass('active');
    }
  });


  // Toggle what form elements are available
  $('#select li').on('click', function() {
    var input = $('input[name=' + $(this).attr('id') + '-active]');
    input.val(input.val() == 'false' ? 'true' : 'false');
    updateActiveDisplay($(this), 'medium', input);
  });

  // Alter tri-state buttons' hidden input based on which button was clicked
  // and the current state
  $('.tri-state').on('click', '.tri-state button', function(event) {
    var $btn = $(this);
    // If the clicked button is not the option (center) button then find it
    var $option = $btn.hasClass('option') ? $btn : $btn.siblings('.option');

    var formInput = $btn.parent().siblings('input[type=hidden]');
    // Current state is stored on the input field.
    var state = formInput.val();

    $option.removeClass('btn-danger btn-success');

    // Determine what newState should be.
    var newState = 0;
    if ($btn != $option) {
      // It is not the center button
      if ($btn.hasClass('btn-success')) {
        newState = state == 1 ? 0 : 1;
      } else if ($btn.hasClass('btn-danger')) {
        newState = state == -1 ? 0 : -1;
      }
    }

    // Change the input field.
    formInput.val(newState);

    updateTriStateDisplay($(this), formInput)
  });

  $('.selection').on('change', 'input[type="checkbox"]', function(e) {
    $(e.target).closest('.selection').find('.icon').toggleClass('active');
  });

  // Alter radio buttons' hidden input based on which button was clicked
  $('.radio-buttons').on('click', '.btn', function(event) {
    $(this).parent().siblings('input[type=hidden]').val($(this).val());
  });
});
