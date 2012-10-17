$(document).ready(function(){
	$('.param').hide();    
    $('#select li').click(function() {
      	$(this).toggleClass('active');
      	$('#input-' + $(this).attr('id')).toggle('medium', function() {});
  		var active = $('input[name=' + $(this).attr('id') + '-active]');
  		$(active[0]).attr('value', $(active[0]).attr('value') == 'false' ? 'true' : 'false');     
    });

    $('.tri-state').live('click', function(event) {
		event.preventDefault();
		var option = $(this).children('.option');
		var state = option.hasClass('btn-danger') ? '-1' : 
						option.hasClass('btn-success') ? '1' : '0';
		var stateClicked = $(event.target).hasClass('btn-danger') ? '-1' : 
						$(event.target).hasClass('btn-success') ? '1' : '0';
		var formInput = $(this).siblings('input[type=hidden]');

		if (stateClicked == state) {
			option.removeClass('btn-danger btn-success');
			formInput.val('0')
		} else if (stateClicked == '1') {
			option.removeClass('btn-danger');
			option.addClass('btn-success');
			formInput.val('1')
		} else if (stateClicked == '-1') {
			option.removeClass('btn-success');
			option.addClass('btn-danger')
			formInput.val('-1')
		}
	});
});
