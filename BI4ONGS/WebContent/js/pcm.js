var idOfFolderTrees = ['dhtmlgoodies_tree','dhtmlgoodies_tree2']; 
var imageFolder = 'images/'; // Path to images
var folderImage = 'dhtmlgoodies_folder.gif'; // Default folder image
var plusImage = 'dhtmlgoodies_plus.gif'; // [+] icon
var minusImage = 'dhtmlgoodies_minus.gif'; // [-] icon
var useAjaxToLoadNodesDynamically = true;
var ajaxRequestFile = 'writeNodes.php'; 


function changeColor(fieldId){
    var negNum = document.getElementById(fieldId);
    if (negNum < 0){
        document.getElementById(fieldId).style.color="red";
    }
}


function checkSelection(event_, name){
	replaceEvent(event_, 0);
	var radioSelec = document.getElementsByName(name);
	for ( var i = 0; i < radioSelec.length; i++ ) {
		if (radioSelec[i].checked){
			//alert('seleccion realizada con exito');
			return true;
		}
	}
	alert('seleccione una fila del grid de resultados');
	return false;
}

function clickAndDisable(link) {

    // add any additional logic here 
	//alert("button-id pressed " + link);
    // disable subsequent clicks
    link.onclick = function(event) {
       e.preventDefault();
    }
}

function cleanForm(form){
	form.reset();
	var inputs = form.getElementsByTagName("input");
	for(var i=0;i<inputs.length;i++){		
		if (inputs[i].type =='text'){
		    //alert('text ctrl: ');
		    //alert(inputs[i].name);
			inputs[i].value = "";
		}else if (inputs[i].type =='textarea'){
		    //alert('textarea ctrl: ');
		    alert(inputs[i].name);
			inputs[i].value = "";
		}else if (inputs[i].type == 'radio'){
			inputs[i].checked = false;
		}else if (inputs[i].type == 'select'){
			inputs[i].selected = false;
		}else if (inputs[i].type == 'checkbox'){
			inputs[i].checked = false;
		}else if (inputs[i].type == 'password'){
			inputs[i].value = "";
		}
	}
}

function cambiaVisualizacion(id){
	 actual = document.getElementById(id).style.display;
	 if(actual == 'none'){
	    document.getElementById(id).style.display='block';
	 }else{
	    document.getElementById(id).style.display='none';
	 }
}


function showClock(){
	//alert("reloj...begins!");
	var Digital=new Date();
	var hours=Digital.getHours();
	var minutes=Digital.getMinutes();
	var seconds=Digital.getSeconds();
	
	if (hours<=9){
		hours="0"+hours;
	}
	if (minutes<=9){
		minutes="0"+minutes;
	}
	if (seconds<=9){
		seconds="0"+seconds;
	}
	
	myclock="<font size='1' face='Arial'><b>"+hours+":"+minutes+":" +seconds + "</b></font>";
	//alert("reloj...is " + myclock);
	if (document.layers){
		//alert("document.layers");
		document.layers.liveclock.document.write(myclock);
		document.layers.liveclock.document.close();
	} else if (document.all){
		//alert("document.all");
		liveclock.innerHTML=myclock;
	} else if (document.getElementById){
		//alert("document.getElementById");
		document.getElementById("liveclock").innerHTML=myclock;
	}
	
	setTimeout("showClock()",1000);
}

	

     


