function getAjaxOptions(url,data){
	return {
			url: url,
			headers : {
				"Accept" : "text/html;type=ajax"
			},
			method: "POST",
			data:data
		};
	
}

function ajaxFail( jqxhr, settings, exception ) {
	console.log("Ajax call failed", exception);
}

var isLoading=false;
var isLoadingProgramado=false;
var startTimer = null;

$(document).ajaxStart(function() {
		var startTime = Date.now();
		var duration = 0.8;
		startTimer = setInterval(function () {
		    var diff = (Date.now() - startTime) / 1000;
		    if (diff > duration) {
				if(!isLoading){
			    	$("body").showLoading();
	    			isLoading=true;
				}
		        clearInterval(startTimer);
		    }
		}, 100);
		isLoadingProgramado = true;
	});

$(document).ajaxComplete(function( event, xhr, settings ) {
		if(isLoadingProgramado){
			clearInterval(startTimer);
			isLoadingProgramado=false;
		}
		$("body").hideLoading();
		isLoading=false;
	});

function onCompleteTask(idAplicacion){
	jQuery.ajax(getAjaxOptions(showFragment+"content&idAplicacion="+idAplicacion+"&isCompleteTask=true", $("#gunixMainForm").serialize()))
	.done(
		function(newContent) {
			$("#content").html(newContent);
		})
	.fail(ajaxFail);	
}