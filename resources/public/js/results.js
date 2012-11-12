jQuery(document).ready(function ($) {
	$('.icon-question-sign').popover({
		html: true,
		content: function() {
			return $(this).siblings('.pop-content').html();
		}
	});
});