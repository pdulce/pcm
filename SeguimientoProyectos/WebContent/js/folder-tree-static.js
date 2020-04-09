/************************************************************************************************************
Static folder tree
Copyright (C) October 2005  DTHMLGoodies.com, Alf Magne Kalleland

www.dhtmlgoodies.com

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

Dhtmlgoodies.com., hereby disclaims all copyright interest in this script
written by Alf Magne Kalleland.

Alf Magne Kalleland, 2006
Owner of DHTMLgoodies.com
	
************************************************************************************************************/	
	
/*
	Update log:
	December, 19th, 2005 - Version 1.1: Added support for several trees on a page(Alf Magne Kalleland)
	January,  25th, 2006 - Version 1.2: Added onclick event to text nodes.(Alf Magne Kalleland)
	February, 3rd 2006 - Dynamic load nodes by use of Ajax(Alf Magne Kalleland)
*/
		
	var idOfFolderTrees = ['dhtmlgoodies_tree'];
	
	var imageFolder = 'img/';	// Path to images
	var folderImage = 'dhtmlgoodies_folder.jpeg';
	var sheetImage = 'dhtmlgoodies_sheet.jpeg';
	var plusImage = 'dhtmlgoodies_plus.jpeg';
	var minusImage = 'dhtmlgoodies_minus.jpeg';
	var initExpandedNodes = '';	// Cookie - initially expanded nodes;
	var useAjaxToLoadNodesDynamically = true;
	var ajaxRequestFile = 'writeNodes.php';
	var contextMenuActive = true;	// Set to false if you don't want to be able to delete and add new nodes dynamically
	
	var ajaxObjectArray = new Array();
	var treeUlCounter = 0;
	var nodeId = 1;
	
	/*
	These cookie functions are downloaded from 
	http://www.mach5.com/support/analyzer/manual/html/General/CookiesJavaScript.htm
	*/
	function Get_Cookie(name) { 
	   var start = document.cookie.indexOf(name+"="); 
	   var len = start+name.length+1; 
	   if ((!start) && (name != document.cookie.substring(0,name.length))) return null; 
	   if (start == -1) return null; 
	   var end = document.cookie.indexOf(";",len); 
	   if (end == -1) end = document.cookie.length; 
	   return unescape(document.cookie.substring(len,end)); 
	} 
	// This function has been slightly modified
	function Set_Cookie(name,value,expires,path,domain,secure) { 
		expires = expires * 60*60*24*1000;
		var today = new Date();
		var expires_date = new Date( today.getTime() + (expires) );
	    var cookieString = name + "=" +escape(value) + 
	       ( (expires) ? ";expires=" + expires_date.toGMTString() : "") + 
	       ( (path) ? ";path=" + path : "") + 
	       ( (domain) ? ";domain=" + domain : "") + 
	       ( (secure) ? ";secure" : ""); 
	    document.cookie = cookieString; 
	} 
	
	function expandAll(treeId)
	{
		//alert('1.expandiendo...showHideNode: ' + treeId);
		var menuItems = document.getElementById(treeId).getElementsByTagName('LI');
		for(var no=0;no<menuItems.length;no++){
			var subItems = menuItems[no].getElementsByTagName('UL');
			if(subItems.length>0 && subItems[0].style.display!='block'){
			    //alert('2.expandiendo...showHideNode ' + menuItems[no].id.replace(/[^0-9]/g,''));
				showHideNode(true/*false*/,menuItems[no].id.replace(/[^0-9]/g,''));
			}			
		}
	}
	
	function collapseAll(treeId)
	{
		var menuItems = document.getElementById(treeId).getElementsByTagName('LI');
		for(var no=0;no<menuItems.length;no++){
			var subItems = menuItems[no].getElementsByTagName('UL');
			if(subItems.length>0 && subItems[0].style.display=='block'){
				showHideNode(false,menuItems[no].id.replace(/[^0-9]/g,''));
			}			
		}		
	}
	
	function getNodeDataFromServer(ajaxIndex,ulId,parentId)
	{
		document.getElementById(ulId).innerHTML = ajaxObjectArray[ajaxIndex].response;
		ajaxObjectArray[ajaxIndex] = false;
		parseSubItems(ulId,parentId);
	}

	
	function parseSubItems(ulId,parentId)
	{
		
		if(initExpandedNodes){
			var nodes = initExpandedNodes.split(',');
		}
		var branchObj = document.getElementById(ulId);
		var menuItems = branchObj.getElementsByTagName('LI');	// Get an array of all menu items
		for(var no=0;no<menuItems.length;no++){
			var imgs = menuItems[no].getElementsByTagName('IMG');
			if(imgs.length>0)continue;
			nodeId++;
			var subItems = menuItems[no].getElementsByTagName('UL');
			var img = document.createElement('IMG');
			img.src = imageFolder + plusImage;
			img.onclick = showHideNode;
			if(subItems.length==0){
				img.style.visibility='hidden';
			}else{
				subItems[0].id = 'tree_ul_' + treeUlCounter;
				treeUlCounter++;
			}
			var aTag = menuItems[no].getElementsByTagName('A')[0];
			aTag.onclick = showHideNode;
			if(contextMenuActive)aTag.oncontextmenu = showContextMenu;

							
			menuItems[no].insertBefore(img,aTag);
			menuItems[no].id = 'dhtmlgoodies_treeNode' + nodeId;
			var folderImg = document.createElement('IMG');
			if(menuItems[no].className){
				folderImg.src = imageFolder + menuItems[no].className;
			}else{
				folderImg.src = imageFolder + folderImage;
			}
			menuItems[no].insertBefore(folderImg,aTag);
			
			var tmpParentId = menuItems[no].getAttribute('parentId');
			if(!tmpParentId)tmpParentId = menuItems[no].tmpParentId;
			if(tmpParentId && nodes[tmpParentId])showHideNode(false,nodes[no]);	
		}		
	}
		
			
	function showHideNode(e,inputId)
	{	    
		//alert('entramos en funcion showHideNode');
		if(inputId){
			if(!document.getElementById('dhtmlgoodies_treeNode'+inputId)){
			    //alert('showHideNode--><inputId> no existe'); 
				return;
			}
			thisNode = document.getElementById('dhtmlgoodies_treeNode'+inputId).getElementsByTagName('IMG')[0]; 
		}else {
			thisNode = this;
			if(this.tagName=='A')thisNode = this.parentNode.getElementsByTagName('IMG')[0];				
		}
		
		try{
			if (thisNode.style.visibility=='hidden') {
				return;
			}

			//alert('showHideNode-->thisNode.style.visibility:' + thisNode.style.visibility);
					
			var parentNode = thisNode.parentNode;
			inputId = parentNode.id.replace(/[^0-9]/g,'');
			if(thisNode.src.indexOf(plusImage)>=0){
				//alert('showHideNode-->ABRIMOS EL FOLDER');
				
				thisNode.src = thisNode.src.replace(plusImage,minusImage);
				var ul = parentNode.getElementsByTagName('UL')[0];
				ul.style.display='block';
				if(!initExpandedNodes)initExpandedNodes = ',';
				if(initExpandedNodes.indexOf(',' + inputId + ',')<0) initExpandedNodes = initExpandedNodes + inputId + ',';
				
				if(useAjaxToLoadNodesDynamically){	// Using AJAX/XMLHTTP to get data from the server
					
					var firstLi = ul.getElementsByTagName('LI')[0];
					var parentId = firstLi.getAttribute('parentId');
					if (!parentId) parentId = firstLi.parentId;
					if(parentId){
						//alert('showHideNode--> ESTE FOLDER NOOOOO TIENE HIJOS');
						ajaxObjectArray[ajaxObjectArray.length] = new sack();
						var ajaxIndex = ajaxObjectArray.length-1;
						ajaxObjectArray[ajaxIndex].requestFile = ajaxRequestFile + '?parentId=' + parentId;					
						ajaxObjectArray[ajaxIndex].onCompletion = function() { getNodeDataFromServer(ajaxIndex,ul.id,parentId); };	// Specify function that will be executed after file has been found					
						ajaxObjectArray[ajaxIndex].runAJAX();
					}else{
						//alert('showHideNode--> ESTE FOLDER TIENE HIJOS');
						ajaxObjectArray[ajaxObjectArray.length] = new sack();
						var ajaxIndex = ajaxObjectArray.length-1;
						ajaxObjectArray[ajaxIndex].requestFile = ajaxRequestFile + '?parentId=' + parentNode.id;					
						ajaxObjectArray[ajaxIndex].onCompletion = function() { getNodeDataFromServer(ajaxIndex,ul.id,parentNode.id); };	// Specify function that will be executed after file has been found					
						ajaxObjectArray[ajaxIndex].runAJAX();
					}
				}
				
			}else{
				//alert('showHideNode--> CERRAMOS EL FOLDER');
				thisNode.src = thisNode.src.replace(minusImage,plusImage);
				parentNode.getElementsByTagName('UL')[0].style.display='none';
				initExpandedNodes = initExpandedNodes.replace(',' + inputId,'');
			}	
			Set_Cookie('dhtmlgoodies_expandedNodes',initExpandedNodes,500);
			
			return false;
		}catch (e){
			return;
		}
	}
	
	var okToCreateSubNode = true;
	function addNewNode(e)
	{
		if(!okToCreateSubNode)return;
		setTimeout('okToCreateSubNode=true',200);
		contextMenuObj.style.display='none';
		okToCreateSubNode = false;
		source = contextMenuSource;
		while(source.tagName.toLowerCase()!='li')source = source.parentNode;
		var nameOfNewNode = prompt('Name of new node');
		if(!nameOfNewNode)return;

		uls = source.getElementsByTagName('UL');
		if(uls.length==0){
			var ul = document.createElement('UL');
			source.appendChild(ul);
			
		}else{
			ul = uls[0];
			ul.style.display='block';
		}
		var img = source.getElementsByTagName('IMG');
		img[0].style.visibility='visible';
		var li = document.createElement('LI');
		li.className=sheetImage;
		var a = document.createElement('A');
		a.href = '#';
		a.innerHTML = nameOfNewNode;
		li.appendChild(a);
		ul.id = 'newNode' + Math.round(Math.random()*1000000);
		ul.appendChild(li);
		parseSubItems(ul.id);
		saveNewNode(nameOfNewNode,source.getElementsByTagName('A')[0].id);
		
	}
	
	/* Save a new node */
	function saveNewNode(nodeText,parentId)
	{
		self.status = 'Ready to save node ' + nodeText + ' which is a sub item of ' + parentId;
		
	}
	
	function deleteNode()
	{
		if(!okToCreateSubNode)return;		
		setTimeout('okToCreateSubNode=true',200);		
		contextMenuObj.style.display='none';
		source = contextMenuSource;
		
		if(!confirm('Click OK to delete the node ' + source.innerHTML))return;
		okToCreateSubNode = false;
		
		var parentLi = source.parentNode.parentNode.parentNode;
		while(source.tagName.toLowerCase()!='li')source = source.parentNode;		

		var lis = source.parentNode.getElementsByTagName('LI');
		source.parentNode.removeChild(source);
		if(lis.length==0)parentLi.getElementsByTagName('IMG')[0].style.visibility='hidden';
		deleteNodeOnServer(source.id);
	}
	
	function deleteNodeOnServer(nodeId)
	{
		self.status = 'Ready to delete node' + nodeId;
		
	}
	
	function initTree(idFolder, idGrandParent, idGrandGrandParent){
		try{
			//alert('initTree...');
			for(var treeCounter=0;treeCounter<idOfFolderTrees.length;treeCounter++){
				var dhtmlgoodies_tree = document.getElementById(idOfFolderTrees[treeCounter]);
				var menuItems = dhtmlgoodies_tree.getElementsByTagName('LI');	// Get an array of all menu items
				for(var no=0;no<menuItems.length;no++){					
					nodeId++;
					var subItems = menuItems[no].getElementsByTagName('UL');
					var img = document.createElement('IMG');
					img.src = imageFolder + plusImage;
					img.onclick = showHideNode;
					if(subItems.length==0){
						img.style.visibility='hidden';
					}else{
						subItems[0].id = 'tree_ul_' + treeUlCounter;
						treeUlCounter++;
					}
					var aTag = menuItems[no].getElementsByTagName('A')[0];
					if(contextMenuActive){
						aTag.oncontextmenu = showContextMenu;
					}
					aTag.onclick = showHideNode;
					menuItems[no].insertBefore(img,aTag);
					if(!menuItems[no].id){
						menuItems[no].id = 'dhtmlgoodies_treeNode' + nodeId;
					}
					var folderImg = document.createElement('IMG');
					if(menuItems[no].className){
						folderImg.src = imageFolder + menuItems[no].className;
					}else{
						folderImg.src = imageFolder + folderImage;
					}
					menuItems[no].insertBefore(folderImg,aTag);
				}			
			}
			initExpandedNodes = Get_Cookie('dhtmlgoodies_expandedNodes');
			if(initExpandedNodes){
				var nodes = initExpandedNodes.split(',');
				for(var no=0;no<nodes.length;no++){				
					if(nodes[no])showHideNode(false,nodes[no]);	
				}
			}
			
			collapseAll('dhtmlgoodies_tree');
			if (idFolder != null){
				//alert('initTree...receiving argument parentid ' + idFolder);		
				showHideNode(true, idFolder+1);//Seguimiento AM: 22
			}
			if (idGrandParent != null && idGrandParent != -1){
				//alert('initTree...receiving argument grantParent ' + idGrantParent);				
				showHideNode(true, idGrandParent+1);//Seguimiento AM: 22
			}
			if (idGrandGrandParent != null && idGrandGrandParent != -1){
				//alert('initTree...receiving argument grantParent ' + idGrantParent);				
				showHideNode(true, idGrandGrandParent+1);//Seguimiento AM: 22
			}
			
		}catch (e){
		}
	}
