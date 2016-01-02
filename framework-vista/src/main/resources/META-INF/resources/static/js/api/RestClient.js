var RestClient = Class.create();

RestClient.prototype = {
	initialize : function(url, type, data,successCallbackFunction,errorCallbackFunction) 
	{
		this.url = url;
		this.type = type;
		this.data = data;
		this.successCallbackFunction=successCallbackFunction
		this.errorCallbackFunction=errorCallbackFunction
	}
	,	
	async:true,
	
	call: function() 
	{					
		jQuery.ajax({
			url : this.url,
			cache : false,
			type : this.type,
			contentType: 'application/x-www-form-urlencoded; charset=ISO-8859-1',
			dataType : "jsonp",
			data : {
				json : this.data
			},
			success : this.successCallbackFunction,
			error : this.errorCallbackFunction,
			async:this.async
		});
		
		
		
	},
	html: function()
	{
		jQuery.ajax({
			url : this.url,
			cache : false,
			type : this.type,
			contentType: 'application/x-www-form-urlencoded; charset=ISO-8859-1',
			dataType : "html",
			data : this.data,
			success : this.successCallbackFunction,
			error : this.errorCallbackFunction
		});
	}
	
	
};


