var Tree = Class.create();

Tree.prototype = {
		initialize : function(idDiv, jsonNodos, settings, click){ //constructor
			this.settingDefault = {
					view: {
						dblClickExpand: false,
						showIcon: false,
						selectedMulti: false
					},
					check: {
						enable: true
					},
					edit: {
						enable: true,
						showRemoveBtn: false,
						showRenameBtn: false
					},
					data: {
						simpleData: {
							enable: true
						}
					},
					callback: { //se asocian las funciones al objeto zTree
						onRightClick: this.onRightClick,
						beforeDrag: this.beforeDrag,
						onClick: this.onClick,
						beforeDrop: this.beforeDrop,
						onRename: this.onRename,
						onCheck: this.onCheck
					},
					treeStructure : new TreeStructure(jsonNodos),
					onClickFunction : click,
					consecutive: 1
			}
			this.idDiv = idDiv;
			this.jsonNodos = jsonNodos;
			this.settings = settings;
			this.zTreeView = null;
			this.rMenu = null;
			this.addMenu(idDiv);
			this.getJsonTreeView(idDiv);
		},

		create: function(idDiv) {
			jQuery.fn.zTree.init(jQuery("#"+this.idDiv), this.settingDefault, this.jsonNodos)
			this.zTreeView = jQuery.fn.zTree.getZTreeObj(this.idDiv)
			this.rMenu = jQuery("#rMenu_" + this.idDiv)
		},

		onBodyMouseDown: function(event){
			if (!(event.target.id == "rMenu" || jQuery(event.target).parents("#rMenu").length>0)) {
				this.rMenu.css({"visibility" : "hidden"});
			}
		},

		onRightClick: function(event, treeId, treeNode) {
			var zTreeView = jQuery.fn.zTree.getZTreeObj(treeId)
			var rMenu = jQuery("#rMenu_"+treeId)

			this.onBodyMouseDown = function(event){
				if (!(event.target.id == ("rMenu_"+treeId) || jQuery(event.target).parents("#rMenu_"+treeId).length>0)) {
					rMenu.css({"visibility" : "hidden"});
				}
			}
			this.showRMenu = function(type, x, y) {
				rMenu = jQuery("#rMenu_"+treeId)

				jQuery("#rMenu_"+treeId).show();
				if (type=="root") {
					jQuery("#m_del").hide();
					jQuery("#m_check").hide();
					jQuery("#m_unCheck").hide();
				} else {
					jQuery("#m_del").show();
					jQuery("#m_check").show();
					jQuery("#m_unCheck").show();
				}
				x =  x + document.body.scrollLeft;
				//y =  document.body.scrollTop;
				rMenu.css({ "position": "fixed", "top":y+"px", "left":x+"px", "visibility":"visible"});

				//jQuery("body").bind("mousedown", this.onBodyMouseDown);
				jQuery("#saneWorkspace").bind("mousedown", this.onBodyMouseDown);
			}

			if (treeNode && !treeNode.noR) {
				zTreeView.selectNode(treeNode)
				this.showRMenu("node", event.clientX, event.clientY	)
			}
		},

		addMenu:function( treeId ){
			jQuery("#saneWorkspace").append(
					'<div id="rMenu_'+treeId+'" class="treeMenu">' +
					'<div id="add" class="sep-cursor-default treeMenuContent" onclick="javascript:addNodeTree(\''+treeId+'\');">Agregar elemento</div>'+
					'<div id="del" class="sep-cursor-default treeMenuContent" onclick="javascript:removeNodeTree(\''+treeId+'\');">Eliminar elemento</div>'+
					'<div id="del" class="sep-cursor-default treeMenuContent" onclick="javascript:nodeUp(\''+treeId+'\');">Subir</div>'+
					'<div id="del" class="sep-cursor-default treeMenuContent" onclick="javascript:nodeDown(\''+treeId+'\');">Bajar</div>'+
					'<div id="del" class="sep-cursor-default treeMenuContent" onclick="javascript:editName(\''+treeId+'\');">Editar nombre</div>'+
					'</div>'
			)
		},

		getJsonTreeView:function(treeId){//obtiene los nodos checked (del ztree, no de la estructura interna)
			var treeObj = jQuery.fn.zTree.getZTreeObj(treeId);
			if(treeObj!=null){
				var nodes = treeObj.getCheckedNodes(true);
				var nodeArray = new Array();
				if(nodes!=""){
					jQuery.each(nodes, function(index,value){
						var jsonObj = {
								"id": nodes[index].id,
								"name": nodes[index].name,
								"level" : nodes[index].level,
								"pId" : nodes[index].pId
						}
						nodeArray[index] = jsonObj;
					});
				}
				console.log(JSON.stringify(nodeArray))
				return nodeArray;
			}
		},

		beforeDrag:function(treeId, treeNodes) {
			for (var i=0,l=treeNodes.length; i<l; i++) {
				if (treeNodes[i].drag === false) {
					return false;
				}
			}
			return true;
		},

		beforeDrop:function(treeId, treeNodes, targetNode, moveType) {
			var treeObj = jQuery.fn.zTree.getZTreeObj(treeId);
			var nodes = treeObj.transformTozTreeNodes( treeObj.getNodes() );
		},

		onClick:function(event,treeId,treeNode,clickFlag){ //sirve para mandar ejecutar una función con un click en un nodo
			var treeObj = jQuery.fn.zTree.getZTreeObj(treeId);
			if(treeObj.setting.onClickFunction!=undefined){
				treeObj.setting.onClickFunction(treeId,treeNode);
			}
		},

		onRename:function(event,treeId,treeNode, isCancel){ //sirve actualizar la estructura del árbol cuando se agrega un nodo y se edita su nombre 
			var treeObj = jQuery.fn.zTree.getZTreeObj(treeId);
			var cloned = cloneTreeNode(treeNode);//clonamos para perder la referencia
			if(!treeObj.setting.treeStructure.existNode(cloned)){
				treeObj.setting.treeStructure.addNode(cloned);
			}else{
				treeObj.setting.treeStructure.editNodeName(cloned);
			}
			return true;
		},
		
		onCheck:function(event,treeId,treeNode){
			var treeObj = jQuery.fn.zTree.getZTreeObj(treeId);
			var cloned = cloneTreeNode(treeNode);
			var parentCloned = cloneTreeNode(treeNode.getParentNode());
			treeObj.setting.treeStructure.checkNode(cloned,parentCloned);
		},

		getTreeStructure:function(treeId){ //para obtener la estructura que se maneja internamente del árbol
			var treeObj = jQuery.fn.zTree.getZTreeObj(treeId);
			if(treeObj!=null){
				var nodes = treeObj.setting.treeStructure.roots;
				console.log(JSON.stringify(nodes))
				return nodes;
			}
		}

};
