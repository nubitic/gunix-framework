var Paginador = Class.create();

Paginador.prototype = {
		
		//id del div contenedor, servicio para el conteo, servicio de bd (si es null va a Activiti), paramtros del servicio a consultar, lista de nombre de las columnas extra que queremos de la bd, mapa con las columnas como se deben definir en el jtable, variables por las que se va a filtrar en caso de ir a Activiti, processKey
		initialize : function (div, servicioCount, servicioDatos,params,extraParams,defColum, filtroActiviti,processKey){
			var ps = {}
			ps["parametros"]=params
			this.countTotalRows  //parece que esta duplicada, esta y la de abajo
			this.totalRows
			this.definicionColumnas = defColum
			// se saca de definicionColumnas
			var mc = {}
			jQuery.each(this.definicionColumnas,function(key,value){
				if(value.campoBaseDatos != undefined){
					mc[key.split(" ").join("")] = value.campoBaseDatos
				}
			});
			this.mapaColumnas = mc
			ps["mapaColumnas"] = this.mapaColumnas
			this.servicioCount = servicioCount
			ps['servicioCount'] = this.servicioCount
			this.servicio = servicioDatos//si lo mandan como nulo, en paginationListService se toma /services/PRX_SC_ActivitiApiService/processConsultaBandeja
			ps["servicio"] = this.servicio
			ps["extraParams"] = extraParams
			ps["filtroActiviti"] = filtroActiviti
			ps["processKey"] = processKey
			this.parametrosServicio = {"json":JSON.stringify( {"data": ps} )}
			this.idDiv = div
			this.jTable
		},

		crearTabla : function(){
			var servicioDatos = CONTEXT_APP+"/rest/paginationList/getList";//a servicio grails
			jQuery('#'+this.idDiv).jtable({
				paging : true,
				pageSizes: [15],
				ajaxSettings: {type: 'GET'},
				columnResizable: false,
	            actions: { listAction: servicioDatos },
	            fields: this.definicionColumnas
	        });
			
			jQuery("#"+ this.idDiv).jtable('load',this.parametrosServicio)
		}

}
