
	var hash = "#";
    //var combo = "combo";
    var context = CONTEXT_APP;
    
    var isSelectedData=false
    var selectedDataMap = {}

    function listaCombosAnidados(comboId){
    	var jsonVar = {variables:{nextComboId:comboId,comboName:"idRamo", comboId:"",selected:""}};
		populateCombo(jsonVar);
    }
    
    jQuery(document).ready(function (){
    	jQuery(document).foundation();
    	onChangeCombo();
    });
	      
    function populateCombo(jsonVar){

	    var nextCombo =  jQuery(hash.concat(jsonVar.variables.nextComboId));
	    if(nextCombo.attr('id')!=undefined && nextCombo.attr('id')!=null){
	    	var json = {data:{nextComboId:jsonVar.variables.nextComboId,nextComboName:nextCombo.attr('name'),comboName:jsonVar.variables.comboName,comboId:jsonVar.variables.comboId,selected:jsonVar.variables.selected}}; 
			var rest = new RestClient(context + '/rest/combosAnidados/consultaInformacionCatalogo', 'GET', JSON.stringify(json) , successPopulateCombo,errorCombo);
			rest.call();
			jQuery("#ajax-loader-".concat(nextCombo.attr('name'))).html("<img src='images/comun/ajax-loader.gif' height='23' width='23'/>");
		}
					
	}
	
    function successPopulateCombo(data){
	    jQuery("div[id^='ajax-loader']").empty();
	  
	    if(data.response.message=='OK'){
	    	var comboId=data.response.data.comboId;
	    	var combo =  jQuery(hash.concat(comboId));
	    	combo.empty();
	    	var info = eval(data.response.data.info);
	    	combo.append(jQuery("<option />").val("").text("Seleccione una opción").prop( "disabled", true ).prop( "selected", true ));
	    	jQuery.each(info,function(){
	    		combo.append(jQuery("<option />").val(this.value).text(this.name));	
			});
	    	cleaningBelowCombos(comboId);	
	    }else{
	    	if(data.response.message=='NOK_DATA'){	    		
	    		var header =""
	    		var body = data.response.details
	    		cleaningBelowCombos(data.response.data.comboId) 		
	    		showModal(header,body)
	    	}else{
	    		jQuery("#saneWorkspace").html(data.response.data.htmlData);
	    	}
	    	
	    }
	    
	    if(isSelectedData==true){
			jQuery(hash.concat(comboId)+' option[value="'+selectedDataMap[comboId+".valor"]+'"]').prop('selected', true)
			jQuery( hash.concat(comboId) ).trigger( "change" );
	    }
	    
	}


	function onChangeCombo(){			
		jQuery("select[id^='combo']").on('change',function(event)
		        {	  		        		       	
		        	var selected = jQuery(this).find(':selected').val();
		        	var comboId = jQuery(this).attr("id");
		        	var comboName = jQuery(this).attr("name");
		        	var nextComboId = (comboId.split("_")[0]+"_").concat(parseInt(comboId.split("_")[1])+1);  // Es necesario que el id del combo termine con n�mero
		        	var jsonVar = {variables:{nextComboId:nextComboId,comboName:comboName,comboId:comboId,selected:selected}}
		        	populateCombo(jsonVar);			        				    
		        });

	}

	function selectedData(m){
		
		jQuery.each( m, function( key, value ) {
			selectedDataMap[key]=value
		});
		
		//selectedDataMap = m
		isSelectedData = true
	}
	
	function cleaningBelowCombos(comboId){
		
		
		var nextComboId = (comboId.split("_")[0]+"_").concat(parseInt(comboId.split("_")[1])+1);
		var nextCombo = jQuery(hash.concat(nextComboId));
		if(nextCombo.attr('id')!=undefined && nextCombo.attr('id')!=null){
			nextCombo.empty();
			nextCombo.append(jQuery("<option />").val("").text("Seleccione una opción").prop( "disabled", true ).prop( "selected", true ))
			cleaningBelowCombos(nextComboId);
		}
	}
	
	function errorCombo(data){
		jQuery("div[id^='ajax-loader']").empty();
		cleaningBelowCombos(data.response.data.comboId);
		errorViewFunction(data);
	}
		