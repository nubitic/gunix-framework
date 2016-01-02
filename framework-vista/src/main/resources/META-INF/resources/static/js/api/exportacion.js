
function exporta(idInformacionAExportar){
	urlDocs = CONTEXT_APP + "/fileManager/exporta";
	
	var form = jQuery('<form style="display:none;" id="exportaForm" name="exportaForm" '+
				'action="'+urlDocs+'" method="Post" enctype="multipart/form-data">'+
				'<input type="hidden" name="idInformacionAExportar" value="' + idInformacionAExportar+'"></input>'+
				'</form>').appendTo(document.body);
		
	form.submit();
	form.remove()	
}