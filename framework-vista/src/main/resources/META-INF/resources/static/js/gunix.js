var urlExceptions = ['uploadFile','ajaxFragment'];

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
		var duration = 0.5;
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
	var errorMessage = $('<div/>').html(xhr.responseText).find("h1");
	if(errorMessage == null || typeof(errorMessage) == 'undefined'  || errorMessage.text() == ''){
		errorMessage = exception;
	} else {
		errorMessage = errorMessage.text();
	}
	alert("Se presentó un problema al procesar la información en el servidor: "+errorMessage);
});

$.ajaxPrefilter(function(options, originalOptions, jqXHR) {
	if(!(/^http/i.test(originalOptions.url))){
		options.type = "POST";
		if($.inArray(originalOptions.url.split('?')[0], urlExceptions) < 0){
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
			if(typeof(originalOptions.data) == 'string' && options.data.indexOf('&idAplicacion=') < 0){
				options.data = originalOptions.data + '&idAplicacion='+cIdAplicacion;
			}else{
				if(typeof(options.data.idAplicacion) == 'undefined')
				options.data = $.param($.extend(originalOptions.data, {
					idAplicacion : cIdAplicacion
				}));	
			}
		}

		if (originalOptions.data instanceof FormData) {
			options.data.append('cgCommandName', cgCommandName);
		} else {
			if(typeof(originalOptions.data) == 'string' && options.data.indexOf('&cgCommandName=') < 0){
				options.data = originalOptions.data + '&cgCommandName='+cgCommandName;
			}else{
				if(typeof(options.data.cgCommandName) == 'undefined')
				options.data = $.param($.extend(originalOptions.data, {
					cgCommandName : cgCommandName
				}));	
			}
		}
	}
});

function onCompleteTask(idAplicacion, preCompleteTask, boton) {
	if (typeof (preCompleteTask) == 'function') {
		if(preCompleteTask(boton) == false){
			return false;
		}
	}
	$.ajax(
			getAjaxOptions(showFragment + "content&idAplicacion=" + idAplicacion + "&isCompleteTask=true", $("#gunixMainForm").serialize()))
				.done(
				function(newContent) {
					$("#content").html(newContent);
				});
}

function buildHrefGet(uri, params) {
	return cGunixViewPath + "/" + uri + "?idAplicacion=" + cIdAplicacion + "&" + (params != null && params != undefined ? $.param(params):'');
}