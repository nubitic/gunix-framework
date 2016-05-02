var urlExceptions = ['uploadFile'];

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

$(document).ajaxError(function(event, xhr, settings, exception) {
	console.log("ajaxError");
	console.log("ajaxError.event", event);
	console.log("ajaxError.jqXHR", xhr);
	console.log("ajaxError.ajaxSettings", settings);
	console.log("ajaxError.thrownError", exception);
});

$.ajaxPrefilter(function(options, originalOptions, jqXHR) {
	options.type = "POST";
	
	
	if($.inArray(originalOptions.url, urlExceptions) < 0){
		if (originalOptions.url.indexOf(cGunixViewPath) < 0) {
			if (originalOptions.url.charAt(0) == '/') {
				options.url = cGunixViewPath + originalOptions.url;
			} else {
				options.url = cGunixViewPath + "/" + originalOptions.url;
			}
		}
	}
		
	if (originalOptions.data instanceof FormData) {
		options.data.append('idAplicacion', cIdAplicacion);
	} else {
		options.data = $.param($.extend(originalOptions.data, {
			idAplicacion : cIdAplicacion
		}));
	}
});

function onCompleteTask(idAplicacion){
	jQuery.ajax(getAjaxOptions(showFragment+"content&idAplicacion="+idAplicacion+"&isCompleteTask=true", $("#gunixMainForm").serialize()))
	.done(
		function(newContent) {
			$("#content").html(newContent);
		});	
}

function buildHrefGet(uri, params) {
	return cGunixViewPath + "/" + uri + "?idAplicacion=" + cIdAplicacion + "&" + (params != null && params != undefined ? $.param(params):'');
}