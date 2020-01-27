function validarDocumento(doc) {
	var res = "ok";
	doc = doc.toUpperCase();
	
	//La longitud debe ser de 9 o 10 caracteres
	if( (doc.length == 9) || (doc.length == 10)){
		//Comprobar si es in CIF
		var regular = new RegExp(/^[ABCDEFGHKLMNPQS]\d\d\d\d\d\d\d[0-9,A-J]$/g); 
       	
       	if (!regular.exec(doc)){
  		var num = 0;
  		if (doc.length == 10){ //NIE
  			var letraInicio = doc.charAt(0) ;
  			if (letraInicio == 'X' || letraInicio == 'Y'){
  				num = doc.substring(1,9);
  			}else{
  				if (!validarCIF(doc)){
       				res = "El formato de CIF/NIF/NIE no es correcto";
       			}  
  			}  			
  		}else { //NIF  		
  			num = doc.substring(0,8);
  		}
  		var lockup = 'TRWAGMYFPDXBNJZSQVHLCKE';
  		var letra = lockup.charAt(num % 23);
  		if (letra != (doc.charAt(doc.length-1)) ){
  			if (!validarCIF(doc)){
       			res = "El formato de CIF/NIF/NIE no es correcto";
       		}  			
  		}         		
       	}else{
       		if (!validarCIF(doc)){
				res = "El formato de CIF/NIF/NIE no es correcto";
       		}
       	}	
	
	}else{
		if(doc.length != 0){
			res = "El formato de CIF/NIF/NIE no es correcto";
		}
	}
	if(res != "ok"){
		alert(res);
	}
	return res;
 }//end validarDocumento function
 
 function validarCIF(texto){ 
     var pares = 0; 
     var impares = 0; 
     var suma; 
     var ultima; 
     var unumero; 
     var uletra = new Array("J", "A", "B", "C", "D", "E", "F", "G", "H", "I"); 
     var xxx; 
      
     texto = texto.toUpperCase(); 
      
     var regular = new RegExp(/^[ABCDEFGHKLMNPQS]\d\d\d\d\d\d\d[0-9,A-J]$/g); 
      if (!regular.exec(texto)) return false; 
           
      ultima = texto.substr(8,1); 
	
	  	
      for (var cont=1; cont<7; cont ++){
          xxx = (2 * parseInt(texto.substr(cont++,1))).toString() + "0"; 
          impares += parseInt(xxx.substr(0,1)) + parseInt(xxx.substr(1,1)); 
          pares += parseInt(texto.substr(cont,1)); 
      } 
      xxx = (2 * parseInt(texto.substr(cont,1))).toString() + "0"; 
      impares += parseInt(xxx.substr(0,1)) + parseInt(xxx.substr(1,1)); 
       
      suma = (pares + impares).toString(); 
      unumero = parseInt(suma.substr(suma.length - 1, 1)); 
      unumero = (10 - unumero).toString(); 
      if(unumero == 10) unumero = 0; 
       
      if ((ultima == unumero) || (ultima == uletra[unumero])) 
          return true; 
      else 
          return false; 
 }//end validarCIF function