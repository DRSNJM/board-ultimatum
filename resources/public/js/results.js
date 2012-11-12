jQuery(document).ready(function ($) {
	$('.pop-trigger').popover({
		html: true,
		content: function() {
			return $(this).siblings('.pop-content').html();
		}
	});
});