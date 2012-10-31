jQuery(document).ready(function ($) {
  var $expertSelect = $('#expert-select');
  // When navigating to the page by always ensure that inputs default to false.
  // This avoids odd behaviour when using back and forward buttons.
  $expertSelect.find('input[type=hidden]').val('false');

  // make game selections toggle hidden input
  var selectedCount = 0;
  var $mainButton = $('#main-button');
  var $mainButtonStrong = $mainButton.find('strong');
  var origButtonText = $mainButtonStrong.html();
  $('#expert-select').on('click', '.game', function (event) {
    event.preventDefault();
    var $this = $(this);
    var input = $this.find('input[type=hidden]');
    var selectedClass = 'selected';
    $this.toggleClass(selectedClass);
    if ($this.hasClass(selectedClass)) {
      input.val('true');
      selectedCount++;
    } else {
      input.val('false');
      selectedCount--;
    }

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
