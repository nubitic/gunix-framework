/**
 * 
 */
var RestClientURI = Class.create();

RestClientURI.prototype = {
		initialize : function(type, data, successViewForm, errorViewForm)
		{
			this.type 	= type;
			this.data	= data;
			this.successCallbackFunction = successViewForm;
			this.errorCallbackFunction = errorViewForm;
			this.url = null;
		}// end initialize
		, 
		call : function() {
			
			/*
			 * Validar que no se hayan achacampalcado y que los parámetros
			 * de entrada sean los correctos además de que no sean null.
			 * */
			// Validar que el parametro: type, sólo corresponda a: GET ó POST
			/*if(this.type != "GET" || this.type != "POST")
				return this.errorCallbackFunction('Error en la inicializacion de RestClientURI: ' + this.type + ' no reconocido para la propiedad: [type]! * Requerido: GET - POST');
			// Validar que el parametro type no sea nulo.
			if(this.type == null)
				return this.errorCallbackFunction('Error en la inicializacion de RestClientURI: la propiedad [type] no puede procesarse con valor nulo! * Requerido: GET - POST');
			
			// Validar que el json que contiene los datos no sea nulo. 
			if(this.data == null)
				return this.errorCallbackFunction('Error en la inicializacion de RestClientURI: la propiedad [data] no puede procesarse con valor nulo!');*/
			
			// * Calcular el contexto en el que se está ejecutando la aplicación.
			//var context = jQuery(location).attr('href');
			var context = CONTEXT_APP;
			
			// * Construir la url para invocar al servicio que utilizará el desarrollador.
			console.log('context: ' + context.replace("#",""));
			this.url = context + '/rest/restClient/' + this.type.toLowerCase();
			jQuery.ajax({
				url : this.url,
				cache : false,
				type : this.type,
				contentType: 'application/x-www-form-urlencoded; charset=ISO-8859-1',
				dataType : "jsonp",
				crossDomain : true,
				async : false,
				data : {
					json : this.data
				},
				success : this.successCallbackFunction,
				error : this.errorCallbackFunction
			}); // end jQuery.ajax
		
		} // end call
} // end prototype





