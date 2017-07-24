$(document).ready(function(){
	window.setTimeout("enable()",5000);
})

function enable(){
	$(".progress_top,.progress_bottom").html('Play now').css('pointer-events','auto');
}