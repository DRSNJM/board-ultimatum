jQuery(document).ready(function ($) {
  /**
   * Expert Compare
   */
  options = {
    labels: 8,
    tooltipSrc: "value",
    labelSrc: "text"
  };
  $(".rating-slider select").each(function (i, select) {
    $(select).selectToUISlider(options).hide();
  });
});
