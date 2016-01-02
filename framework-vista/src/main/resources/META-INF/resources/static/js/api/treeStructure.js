var TreeStructure = Class.create();

TreeStructure.prototype = {
		initialize : function(rootNodes){ //constructor
			this.roots = new Array();
			if(rootNodes !== undefined && rootNodes != null){
				//this.roots = rootNodes;
				for(var i=0;i<rootNodes.length;i++){
					this.roots[i] = cloneTreeNode(rootNodes[i]);
				}
			}//else{
//				//this.roots = new Array();
//			}
		},

		addNode : function(node){
			
			if(node.pId === undefined || node.pId == null){
				this.roots[this.roots.length] = node;
			}else{
				var found = false;
				for(var i=0, rl=this.roots.length; i<rl; i++){
					found = this.searchAndInsertNode(this.roots[i],node);
					if(found){
						break;
					}
				}
			}
		},

		searchAndInsertNode : function(targetNode,newNode){
			if(targetNode.children===undefined || targetNode.children.length==0){
				if(newNode.pId == targetNode.id){
					targetNode.children = new Array();
					targetNode.children[targetNode.children.length] = newNode;
					return true;
				}else{
					return false;
				}
			}else{
				if(targetNode.id == newNode.pId){
					targetNode.children[targetNode.children.length] = newNode;
					return true;
				}else{
					var found = false;
					for(var i=0, cl= targetNode.children.length; i<cl; i++){
						found = this.searchAndInsertNode(targetNode.children[i],newNode);
						if(found){
							break;
						}
					}
					return found;
				}
			}
			return false;
		},

		removeNode : function(node){
			if(node.pId === undefined || node.pId == null){
				//this.roots[this.roots.length] = nodeMap;
				var i = 0;
				for(var lr=this.roots.length;i<lr;i++){
					if(node.id==this.roots[i].id){
						break;
					}
				}
				this.roots.splice(i,1);
			}else{
				var found = false;
				for(var i=0, rl=this.roots.length; i<rl; i++){
					found = this.searchAndDeleteNode(this.roots[i],node);
					if(found){
						break;
					}
				}
			}
		},

		searchAndDeleteNode : function(targetNode,node){
			if(targetNode.children===undefined || targetNode.children.length==0){
				if(targetNode.id == node.id){
					return true;
				}else{
					return false;
				}
			}else{
				if(targetNode.id == node.id){
					return true;
				}
				var found = false;
				for(var i=0, cl= targetNode.children.length; i<cl; i++){
					found = this.searchAndDeleteNode(targetNode.children[i],node);
					if(found){
						targetNode.children.splice(i,1);
						break;
					}
				}
			}
			return false;
		},

		moveNode : function(node,movement){
			if(node.pId === undefined || node.pId == null){
				var i = 0;
				for(var lr=this.roots.length;i<lr;i++){
					if(node.id==this.roots[i].id){
						break;
					}
				}
				if(i+movement>=0 && i+movement<this.roots.length){
					this.roots.splice(i,1);
					this.roots.splice(i+movement,0,node);
				}else{
					return;
				}
			}else{
				var found = false;
				for(var i=0, rl=this.roots.length; i<rl; i++){

					found = this.searchAndMoveNode(this.roots[i],node,movement);
					if(found){
						break;
					}
				}
			}
		},

		searchAndMoveNode : function(targetNode,node,movement){
			if(targetNode.children===undefined || targetNode.children.length==0 || targetNode.id==node.id){
				if(targetNode.id == node.id){
					return true;
				}else{
					return false;
				}
			}else{
				var found = false;
				for(var i=0, cl= targetNode.children.length; i<cl; i++){
					found = this.searchAndMoveNode(targetNode.children[i],node,movement);
					if(found){
						targetNode.children.splice(i,1);
						targetNode.children.splice(i+movement,0,node);
						break;
					}
				}
				//return found;
			}
			return false;
		},

		addExtraData : function(node,map){
			if(node.pId === undefined || node.pId == null){
				var i = 0;
				for(var lr=this.roots.length;i<lr;i++){
					if(node.id==this.roots[i].id){
						break;
					}
				}
				if(this.roots[i].extraData === undefined || this.roots[i].extraData == null){
					this.roots[i].extraData={};
				}
				var rootstmp = this.roots;
				jQuery.each(map,function(key,value){
					rootstmp[i].extraData[key]= value;
				});
			}else{
				var found = false;
				for(var i=0, rl=this.roots.length; i<rl; i++){
					found = this.searchAndAddData(this.roots[i],node,map);
					if(found){
						break;
					}
				}
			}
		},

		searchAndAddData : function(targetNode,node,map){
			if(targetNode.id==node.id){
				if(targetNode.extraData === undefined || targetNode.extraData == null){
					targetNode.extraData={};
				}
				jQuery.each(map,function(key,value){
					targetNode.extraData[key]= value;
				});
				return true;
			}
			if(targetNode.children===undefined || targetNode.children.length==0){
				return false;
			}
			var found = false;
			for(var i=0,tnl=targetNode.children.length;i<tnl;i++){
				found = this.searchAndAddData(targetNode.children[i],node,map);
				if(found){
					return true;
				}
			}
			return false;
		}, 

		existNode : function(node){
			var exist = false;

			for(var i=0,rl=this.roots.length;i<rl;i++){
				exist = this.existNodeLeaf(this.roots[i],node);
				if(exist){
					return exist;
				}
			}

			return exist;
		},

		existNodeLeaf : function(target, node){
			if(target.id==node.id){
				return true;
			}
			if(target.children!==undefined && target.children.length!=0){
				var exist = false;
				for(var i=0, tcl=target.children.length;i<tcl;i++){
					exist = this.existNodeLeaf(target.children[i],node);
					if(exist){
						return true;
					}
				}
			}else{
				return false;
			}
			return false;
		},

		editNodeName : function(node){
			var modified = false;
			for(var i=0,rl=this.roots.length;i<rl;i++){
				modified = this.editNodeLeaf(this.roots[i],node);
				if(modified){
					return modified;
				}
			}
		},

		editNodeLeaf : function(target, node){
			if(target.id==node.id){
				target.name = node.name;
				return true;
			}
			if(target.children!==undefined && target.children.length!=0){
				var modified = false;
				for(var i=0, tcl=target.children.length;i<tcl;i++){
					modified = this.editNodeLeaf(target.children[i],node);
					if(modified){
						return modified;
					}
				}
			}else{
				return false;
			}
		},
		
		checkNode : function(node,parentNode){
			var modified = false;
			for(var i=0,rl=this.roots.length;i<rl;i++){
				modified = this.checkNodeLeaf(this.roots[i],node);
				if(modified){
					return modified;
				}
			}
		},
		
		checkNodeLeaf : function(target, node){
			if(target.id==node.id){
				target.checked = node.checked;
				//revisar si se marcó un padre y marcar sus hijos
				if(target.children!=undefined && target.children.length!=0){
					for(var j=0;j<target.children.length;j++){
						this.setChecked(target.children[j],node.checked);
					}
				}
				return true;
			}
			if(target.children!==undefined && target.children.length!=0){
				var modified = false;
				for(var i=0, tcl=target.children.length;i<tcl;i++){
					modified = this.checkNodeLeaf(target.children[i],node);
					if(modified){
						if(node.checked){//si se marca un hijo a fuerza se marca su padre
							target.checked = true;
						}else{
							var ch = false;
							for(var i=0;i<target.children.length;i++){
								if(target.children[i].checked==true){
									ch = true;
									break;
								}
							}
							target.checked=ch;
						}
						return modified;
					}
				}
			}else{
				return false;
			}
		},
		
		setChecked : function(node,check){
			node.checked=check;
			if(node.children==undefined || node.children.length==0){
				node.checked = check;
				return;
			}
			for(var i=0;i<node.children.length;i++){
				this.setChecked(node.children[i], check);
			}
		}

};
