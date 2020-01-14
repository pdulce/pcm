function arrastrarInfoAyudaDenominaPrueba(ref,programa,clasificacionEconomica,ejercicio){	
	try{
		var enlace=document.getElementById(ref);	
		var prog;
		if(programa){
			prog=document.getElementById(programa);
		}
		var clas;
		if(clasificacionEconomica){
			clas=document.getElementById(clasificacionEconomica);
		}
		var ejer;
		if(ejercicio){
			ejer=document.getElementById(ejercicio);
		}

		if(enlace && (prog || clas || ejer)){
			enlace=enlace.nextSibling;
			if(enlace && enlace.tagName.toUpperCase()!='A'){
				enlace=enlace.nextSibling;
				if(!enlace || enlace.tagName.toUpperCase()!='A'){				
					return;
				}
			}
			if(enlace){				
				var hrefEnlace=enlace.href;
				if(hrefEnlace){					
					var params=hrefEnlace.split('&');									
					
					if(params){	
						var arrayParams = new Array(3);
						arrayParams[0]=ejer;
						arrayParams[1]=clas;
						arrayParams[2]=prog;
						
						var indice;
						var param;
						for(i in arrayParams){
							if(arrayParams[i]){
								indice=params.length-i-1;
								param=params[indice].split('=');
								params[indice]=param[0]+'='+arrayParams[i].value;
							}
						}
																													
						hrefEnlace='';
						for(i in params){
							if(i!=0){
								hrefEnlace+='&';
							}
							hrefEnlace+=params[i];											
						}
						enlace.href=hrefEnlace;
					}
				}
			}
		}
	}catch(e){}
}

function arrastrarInfoAyudaNif(centro, referencia){
	try{
						
		var enlace=document.getElementById(referencia);
		var centro = document.getElementById(centro);
																							
		if(enlace && centro){
			enlace=enlace.nextSibling;			
			if(enlace && enlace.tagName.toUpperCase()!='A'){
				enlace=enlace.nextSibling;
				if(!enlace || enlace.tagName.toUpperCase()!='A'){
					return;
				}
			}			
			if(enlace){
				var hrefEnlace=enlace.href;
				if(hrefEnlace){
					var params=hrefEnlace.split('&');									
					
					if(params){	
						param=params[9].split('=');
						params[9]=param[0]+'='+ centro.value;
																															
						hrefEnlace='';
						for(i in params){
							if(i!=0){
								hrefEnlace+='&';
							}
							hrefEnlace+=params[i];											
						}
						enlace.href=hrefEnlace;
					}
				}
			}
		}								
	}catch(e){}
}	
											
function arrastrarInfoAyudaProyectoPrueba(ref,proyecto,delegacion,ejercicio,programa,clasificacionEconomica){
	try{
		var enlace=document.getElementById(ref);		
		var nProy;
		if(proyecto){
			nProy=document.getElementById(proyecto);
		}		
		var deleg;
		if(delegacion){
			deleg=document.getElementById(delegacion);
		}
		var ejer;
		if(ejercicio){
			ejer=document.getElementById(ejercicio);
		}				
		var prog;
		if(programa){		
			prog=document.getElementById(programa);
		}
		var clasif;
		if(clasificacionEconomica){
			clasif=document.getElementById(clasificacionEconomica);						
		}		
		
		if(enlace  && (nProy || deleg || ejer || prog || clasif)){
			enlace=enlace.nextSibling;			
			if(enlace && enlace.tagName.toUpperCase()!='A'){
				enlace=enlace.nextSibling;
				if(!enlace || enlace.tagName.toUpperCase()!='A'){
					return;
				}
			}
			if(enlace){
				var hrefEnlace=enlace.href;
				if(hrefEnlace){					
					var params=hrefEnlace.split('&');
					
					if(params){						
						
						var arrayParams = new Array(5);
						arrayParams[0]=clasif;
						arrayParams[1]=prog;						
						arrayParams[2]=ejer;
						arrayParams[3]=nProy;
						arrayParams[4]=deleg;
						
						var indice;
						var param;
						for(i in arrayParams){
							if(arrayParams[i]){
								indice=params.length-i-1;
								param=params[indice].split('=');
								params[indice]=param[0]+'='+arrayParams[i].value;
							}
						}
																										
						hrefEnlace='';
						for(i in params){
							if(i!=0){
								hrefEnlace+='&';
							}
							hrefEnlace+=params[i];											
						}
						enlace.href=hrefEnlace;	
					}									
				}
			}			
		}
	}catch(e){}
}
						
function arrastrarInfoAyudaLEXPrueba(pantalla,ref,tipoGasto,indiceTGasto,ejercicio){							
	try{								
		var enlace=document.getElementById(ref);
		var tg;
		if(tipoGasto){
			tg=document.getElementById(tipoGasto);
		}
		var itg;
		if(indiceTGasto){
			itg=document.getElementById(indiceTGasto);
		}
		
		var ejer;
		if(ejercicio){
			ejer=document.getElementById(ejercicio);
		}
								
		if(!ejer && pantalla=='fac'){
		   ejer=document.getElementById('selEjercicio');
		}
		
		var prog;
		var clasif;
		var nif;
		if(pantalla=='doc'){
			prog=document.getElementById('programa');
			clasif=document.getElementById('clasificacionEconomica');
			nif=document.getElementById('CIF');
		}		
				
		if(enlace && (tg || itg || ejer || prog || clasif || nif)){
			enlace=enlace.nextSibling;
			if(enlace && enlace.tagName.toUpperCase()!='A'){
				enlace=enlace.nextSibling;
				if(!enlace || enlace.tagName.toUpperCase()!='A'){
					return;
				}
			}
			if(enlace){													
				var hrefEnlace=enlace.href;
				if(hrefEnlace){
					var params = hrefEnlace.split('&');												
					if(params){																								

						var arrayParams = new Array(6);
						arrayParams[0]=nif;
						arrayParams[1]=clasif;						
						arrayParams[2]=prog;
						arrayParams[3]=ejer;
						arrayParams[4]=itg;
						arrayParams[5]=tg;						
						
						var indice;
						var param;
						for(i in arrayParams){
							if(arrayParams[i]){
								indice=params.length-i-1;
								param=params[indice].split('=');
								params[indice]=param[0]+'='+arrayParams[i].value;
							}
						}
					
						hrefEnlace='';
						for(i in params){
							if(i!=0){
								hrefEnlace+='&';
							}
							hrefEnlace+=params[i];
						}
						enlace.href=hrefEnlace;
					}
				}
			}
		}	
	}catch(e){}
}