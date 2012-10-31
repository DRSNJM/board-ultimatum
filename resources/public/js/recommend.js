jQuery(document).ready(function ($) {
  $('.param').hide();

  // Toggle what form elements are available
  $('#select li').on('click', function() {
    $(this).toggleClass('active');
    $('#input-' + $(this).attr('id')).toggle('medium');
    var active = $('input[name=' + $(this).attr('id') + '-active]');
    active.attr('value', active.attr('value') == 'false' ? 'true' : 'false');
  });

  // Alter tri-state buttons' hidden input based on which button was clicked
  // and the current state
  $('.tri-state').on('click', '.tri-state button', function(event) {
    var $btn = $(this);
    // If the clicked button is not the option (center) button then find it.
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

    // New state is not 0 so set the appropriate class on the option button.
    if (newState !== 0) {
      $option.addClass(newState == 1 ? 'btn-success' : 'btn-danger');
    }

    // Change the input field.
    formInput.val(newState);
  });

  $('.selection').on('change', 'input[type="checkbox"]', function(e) {
    $(e.target).closest('.selection').find('.icon').toggleClass('active');
  });

  // Alter radio buttons' hidden input based on which button was clicked
  $('.radio-buttons').on('click', '.btn', function(event) {
    $(this).parent().siblings('input[type=hidden]').val($(this).val());
  });
});
