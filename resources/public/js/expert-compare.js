jQuery(document).ready(function ($) {
  /**
   * Expert Compare
   */
  $(".rating-slider").slider({
    orientation: "horizontal",
    range: "min",
    min: 0,
    max: 1000,
    value: 500,
    slide: function (event, ui) {
      var val = ui.value;
      var id = parseInt(this.id.slice(13), 10);
      $('#rating-input' + id).val(val);
    }
  });
});
