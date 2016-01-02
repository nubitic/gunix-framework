
function addNodeTree(treeId){
	var zTreeView = jQuery.fn.zTree.getZTreeObj(treeId);
	var newNode = { name:"Nuevo"};
	var newNodeReturned = null;
	var target = zTreeView.getSelectedNodes()[0]; //se tiene el multiselect deshabilitado
	if (target) {
		newNode.checked = target.checked;
		newNodeReturned = zTreeView.addNodes(target, newNode);
	} else {
		newNodeReturned = zTreeView.addNodes(null, newNode);
	}
	var rMenu = jQuery("#rMenu_"+treeId);
	hideRMenu(rMenu);

	newNodeReturned[0].id = "tmp_"+ newNodeReturned[0].pId +"_"+zTreeView.setting.consecutive;
	zTreeView.setting.consecutive++;
	zTreeView.editName(newNodeReturned[0]);
}

function removeNodeTree(treeId){
	var rMenu = jQuery("#rMenu_"+treeId)
	hideRMenu(rMenu);
	var zTreeView = jQuery.fn.zTree.getZTreeObj(treeId);
	var nodes = zTreeView.getSelectedNodes();
	if (nodes && nodes.length>0) {
		var msg = "Â¿Desea elmininar el elemento?";
		if (confirm(msg)==true){
			zTreeView.removeNode(nodes[0]);
			zTreeView.setting.treeStructure.removeNode(nodes[0]);
		}
	}
}

function nodeUp(treeId){
	var treeObj = jQuery.fn.zTree.getZTreeObj(treeId);
	var moved = cloneTreeNode(treeObj.getSelectedNodes()[0]); //se tiene el multiselect deshabilitado
	var tmpPrev = previousNode(treeObj.getSelectedNodes()[0],treeId);
	//var previous = cloneTreeNode(tmpPrev);
	if(!moved.isFirstNode && tmpPrev!=null){
		var previous = treeObj.getNodeByParam("id", tmpPrev.id, null);
		var test = treeObj.moveNode(previous,treeObj.getSelectedNodes()[0],"prev",false);
		treeObj.setting.treeStructure.moveNode(moved,-1);
	}
	var rMenu = jQuery("#rMenu_"+treeId)
	hideRMenu(rMenu);
}

function nodeDown(treeId){	
	var treeObj = jQuery.fn.zTree.getZTreeObj(treeId);
	var moved = cloneTreeNode(treeObj.getSelectedNodes()[0]); //se tiene el multiselect deshabilitado
	var tmpNext = nextNode(treeObj.getSelectedNodes()[0],treeId);
	//var next = cloneTreeNode(tmpNext);
	if(!moved.isLastNode && tmpNext!=null){
		var next = treeObj.getNodeByParam("id", tmpNext.id, null);
		treeObj.moveNode(next,treeObj.getSelectedNodes()[0],"next",false);
		treeObj.setting.treeStructure.moveNode(moved,1);
	}
	var rMenu = jQuery("#rMenu_"+treeId)
	hideRMenu(rMenu);
}

function hideRMenu(menu) {
	if (menu) menu.css({"visibility": "hidden"});
	//jQuery("body").unbind("mousedown", onBodyMouseDown);
	jQuery("#saneWorkspace").unbind("mousedown", onBodyMouseDown);
}

function onBodyMouseDown(event){
	if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length>0)) {
		rMenu.css({"visibility" : "hidden"});
	}
}

function editName(treeId){
	var rMenu = jQuery("#rMenu_"+treeId)
	hideRMenu(rMenu);

	var treeObj = jQuery.fn.zTree.getZTreeObj(treeId);
	var edit = treeObj.getSelectedNodes()[0]; //se tiene el multiselect deshabilitado
	treeObj.editName(edit);
}


/*Esta función no es precisamente del menú, sirve para agregar datos adicionales a un elemento del árbol*/
function treeData(treeId,map){
	var treeObj = jQuery.fn.zTree.getZTreeObj(treeId);
	if (treeObj === undefined || treeObj == null){
		return;
	}
	var selectedNode = treeObj.getSelectedNodes()[0]; //se tiene el multiselect deshabilitado
	if(selectedNode.extraData==undefined){
		selectedNode.extraData = {};	
	}
	jQuery.each(map,function(key,value){
		selectedNode.extraData[key]= value;
	});

	treeObj.setting.treeStructure.addExtraData(selectedNode,map);//se agrega en la estructura interna del árbol
}

/*Función auxiliar para clonar un nodo. Básicamente es para tener una copia de un nodo del árbol en pantalla pero sin su referencia*/
function cloneTreeNode(node){
	if(node==null){
		return null;
	}
	var cloned = {};
	jQuery.each(node,function(key,value){
		if(key=="children"){
			cloned.children=[];
			for(var i=0;i<value.length;i++){
				//cloneNode(cloned.children);
				cloned.children[i]=cloneTreeNode(value[i]);
			}
		}else{
			cloned[key]=value;
		}
	});
	//cloned.cloned=true;
	return cloned;
}

/*Función auxiliar para traer el nodo previo (hermano) de otro nodo*/

function previousNode(node,treeId){//ese node es una referencia al nodo gráfico
	var previous;
	var parent = node.getParentNode();
	
	if(parent == null){//es root
		var treeObj = jQuery.fn.zTree.getZTreeObj(treeId);
		var structureRoots = treeObj.setting.treeStructure.roots;
		var sri = 0
		for(;sri<structureRoots.length;sri++){
			if(structureRoots[sri].id==node.id){
				break;
			}
		}
		if(sri-1>=0){
			return structureRoots[sri-1];
		}else{
			return null;
		}
		//return node.getPreNode();
	}
	
	var siblings = parent.children; //siempre tiene children porque tiene a node como hijo
	
	var i = 0;
	for(;siblings.length;i++){
		if(siblings[i].id==node.id){
			break;
		}
	}
	if(i-1>=0){//siempre está el nodo en siblings
		previous = siblings[i-1];
	}else{
		previous = null;
	}
	return previous;
}

/*Función auxiliar para traer el nodo siguiente (hermano) de otro nodo*/

function nextNode(node,treeId){
	var next;
	var parent = node.getParentNode();
	
//	if(parent == null){
//		return node.getNextNode();
//	}
	
	if(parent == null){//es root
		var treeObj = jQuery.fn.zTree.getZTreeObj(treeId);
		var structureRoots = treeObj.setting.treeStructure.roots;
		var sri = 0
		for(;sri<structureRoots.length;sri++){
			if(structureRoots[sri].id==node.id){
				break;
			}
		}
		if(sri+1<structureRoots.length){
			return structureRoots[sri+1];
		}else{
			return null;
		}
		//return node.getPreNode();
	}
	
	var siblings = parent.children; //siempre tiene children porque tiene a node como hijo
	
	var i = 0;
	for(;siblings.length;i++){
		if(siblings[i].id==node.id){
			break;
		}
	}
	if(i+1<siblings.length){//siempre está el nodo en siblings
		next = siblings[i+1];
	}else{
		next = null;
	}
	return next;
}



