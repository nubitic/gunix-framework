var cGunixContextPath="";
var urlExceptions = [];

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
			if (originalOptions.url.indexOf(cCurrentGunixViewPath) < 0) {
				if (originalOptions.url.charAt(0) == '/') {
					options.url = cCurrentGunixViewPath + originalOptions.url.substring(1);
				} else {
					options.url = cCurrentGunixViewPath + originalOptions.url;
				}
			}
		}
		
		addParamToData(originalOptions, options, 'idAplicacion', cIdAplicacion);
		addParamToData(originalOptions, options, 'cgCommandName', cgCommandName);
	}
});

function addParamToData(originalOptions, options, paramName, paramValue){
	if (options.data instanceof FormData) {
		options.data.append(paramName, paramValue);
	} else {
		if(typeof(options.data) == 'string' && options.data.indexOf('&'+paramName+'=') < 0 && options.data.indexOf(paramName+'=') < 0){
			options.data = options.data + '&'+paramName+'='+paramValue;
		}else{
			if(options.data==null || 
			   options.data=='' || 
			   typeof(options.data)=='undefined' || 
			   (typeof(options.data)=='object' && eval('typeof(options.data.'+paramName+')') == 'undefined'))
				options.data = $.param($.extend(
												(options.data==null||
												 options.data==''||
												 typeof(options.data)=='undefined')?{}:options.data, 
													JSON.parse('{"' + paramName + '":"' + paramValue + '"}')
												)
									  );	
		}
	}
}

function onCompleteTask(idAplicacion, preCompleteTask, boton) {
	if (typeof (preCompleteTask) == 'function') {
		if(preCompleteTask(boton) == false){
			return false;
		}
	}
	$.ajax(
			getAjaxOptions(cGunixContextPath + showFragment + fragmentToUpdate + "&idAplicacion=" + idAplicacion + "&isCompleteTask=true", $("#gunixMainForm").serialize()))
				.done(
				function(newContent) {
					$("#"+fragmentToUpdate).html(newContent);
				});
}

function startProcess(idAplicacion, idRol, idModulo, idFuncion) {
	$.ajax(
			getAjaxOptions(cGunixContextPath + "startProcess", {fragments:fragmentToUpdate,idAplicacion:idAplicacion, idRol:idRol, idModulo:idModulo, idFuncion, idFuncion}))
				.done(
				function(newContent) {
					$("#"+fragmentToUpdate).html(newContent);
				});
}

function buildHrefGet(uri, params) {
	return cCurrentGunixViewPath + uri + "?idAplicacion=" + cIdAplicacion + "&" + (params != null && params != undefined ? $.param(params):'');
}


/** http://blog.jonathanargentiero.com/downloading-files-with-jquery/ */
jQuery.download = function(url, data, method){
    //url and data options required
    if( url && data ){ 
        //data can be string of parameters or array/object
        data = typeof data == 'string' ? data : jQuery.param(data);
        //split params into form inputs
        var inputs = '';
        jQuery.each(data.split('&'), function(){ 
            var pair = this.split('=');
            inputs+='<input type="hidden" name="'+ pair[0] +'" value="'+ pair[1] +'" />'; 
        });
        
        inputs+='<input type="hidden" name="idAplicacion" value="'+ cIdAplicacion +'" />'; 
        inputs+='<input type="hidden" name="cgCommandName" value="'+ cgCommandName +'" />'; 
        //send request
        jQuery('<form action="'+ cCurrentGunixViewPath + url +'" method="'+ (method||'post') +'" target="_blank">'+inputs+'</form>')
        .appendTo('body').submit().remove();
    };
};