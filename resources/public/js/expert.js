jQuery(document).ready(function ($) {
  var $expertSelect = $('#expert-select');
  // When navigating to the page by always ensure that inputs default to false.
  // This avoids odd behaviour when using back and forward buttons.
  $expertSelect.find('input[type=hidden]').val('false');

  // Store some jQuery objects for commonly used elements.
  var $mainButton = $('#main-button');
  var $mainButtonStrong = $mainButton.find('strong');
  var origButtonText = $mainButtonStrong.html();
  var selectedClass = 'selected';

  // Keep track of the number of selected games.
  var selectedCount = 0;
  $expertSelect.on('click', '.game', function (event) {
    event.preventDefault();
    // The game being clicked.
    var $this = $(this);
    // The hidden element used by the form.
    var input = $this.find('input[type=hidden]');

    $this.toggleClass(selectedClass);
    if ($this.hasClass(selectedClass)) {
      // This games has been selected!
      input.val('true');
      selectedCount++;
    } else {
      // This games has been unselected.
      input.val('false');
      selectedCount--;
    }

    // Change the submit button to reflect whether the user has made in
    // selections.
    if (selectedCount > 0) {
      if (!$mainButton.hasClass('btn-primary')) {
        $mainButtonStrong.html("I know these games!");
        $mainButton.addClass('btn-primary');
      }
    } else {
      $mainButton.removeClass('btn-primary');
      $mainButtonStrong.html(origButtonText);
    }
  });
});
