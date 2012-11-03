jQuery(document).ready(function ($) {
  /**
   * Expert Compare
   */
  var sliderIncrementCount = 1000;
  $(".rating-slider").slider({
    orientation: "horizontal",
    range: "min",
    min: 0,
    max: sliderIncrementCount,
    value: sliderIncrementCount / 2,
    slide: function (event, ui) {
      var val = ui.value;
      var id = parseInt(this.id.slice(13), 10);
      $('#rating-input' + id).val(val);
    }
  });
});
