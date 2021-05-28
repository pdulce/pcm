/******** Variables PRIVADAS ARQUITECTURA ********/
var ParqGeneralJS= true;
var bARQDPopupActivo= false;
var bARQDErrorCarga = false;
var iARQDTIMEOUT_CARGA_XML= 2000;
var sARQD_PREFIJO_DIV_GTB= "divMsg";
var NOMBRE_VENTANA_WINDOWOPEN= "ARQDwindow";
var bARQDRequestedSubmit= false;
var IE_MAXIMA_LONGITUD_GET= 2048;


/****** FIN Variables PRIVADAS ARQUITECTURA ******/

/******** Variables PUBLICAS ********/

var gIdioma;
var metaTags;

/****** FIN Variables PUBLICAS ******/

gIdioma= 'ES';

//function manejadorEventos(e){
//	var event = window.event||e;
//	var obj = event.srcElement||event.target;
//	var args=Array.prototype.slice.call(arguments,1);	
//	var fRef = args[0];//la funcion a invocar es el primer argumento
//	var fn = window[fRef];
//	args.unshift(obj);
//	fn.apply(this, args);
//}
 
//function chargeRecord(value_){	
//alert('obj.value: ' + value_);
//if (value_ != ''){
//	alert('submitting page...');
//	document.forms[0].submit();
//}
//}


//*************************************************************************
//* Inicializa el javascript de Prosa con el idioma y el fichero de       *
//* errores de validacion; asi como la ayuda                              *
//*************************************************************************
function InicializarProsa()
{
	InicializarValidaciones();
	//InicializarGTB();
}

//*************************************************************************
//* Crea una cadena de parametros de URL a partir de los argumentos		  *
//*************************************************************************	
function CrearParametrosURL(args, index)
{
	var stURL= "";
	for (var i=index;i < args.length; i+=2)
	   stURL += (i==index ? "?" : "&") + args[i] + (args[i+1]==null?"": "=" + args[i+1] );
	return stURL;
}


function obtenerMarcado(nameEl){
	//alert('llamando a marcado...');
	var elementosRadio=document.getElementsByName(nameEl);
	//alert('radios...' + elementosRadio.length);
	for (var i=0; i < elementosRadio.length; i++){
		if (elementosRadio[i].checked){
			//alert('devolvemos el radio position ' + i);
			return elementosRadio[i].value;
		}		
	}
	return "";
}


function obtenerMasterId(nameEl){
	var elementoIdForm=document.getElementById(nameEl);
	return elementoIdForm.name + '=' + elementoIdForm.value;
}

//*************************************************************************
//* Invoca una URL opcionalmente a una nueva ventana                      *
//*************************************************************************
function invoke(ventana, paramsVentana, URL)
{
    var oVentana= null;
	if (ventana != null)
	{
		if (URL == null)
			// Debe indicar URL , solamente se obliga si
			showMessageCod(MSG_DATONOESPECIFICADO, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS),'ARQ',null,null,"DATO","URL"); 
		else
			oVentana= window.open(URL + CrearParametrosURL(arguments, 3), ventana, paramsVentana);
	}
	else
		// No se ha indicado la ventana de invocacion
		showMessageCod(MSG_DATONOESPECIFICADO, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS),'ARQ',null,null,"DATO","Ventana de invocacion"); 
	return oVentana;
}

function invokeTarea()
{
    if (metaTags('URLFRONTEND') == null)
        // URL para FrontEnd no especificada
        showMessageCod(MSG_DATONOESPECIFICADO, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS),'ARQ',null,null,"DATO","URL para FrontEnd");
    else
        return URLFRONTEND.content + CrearParametrosURL(arguments, 0) ;
}

//*************************************************************************
//* Devuelve una URL conteniendo la invocacion a la Aplicacion dada.      *
//*************************************************************************
function invokeApp(App)
{
	if ( (arguments.length != 1) || (App != null) )
	{
		if (metaTags('URLFRONTEND') == null)
		    // URL para FrontEnd no especificada
		    showMessageCod(MSG_DATONOESPECIFICADO, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS),'ARQ',null,null,"DATO","URL para FrontEnd");
		else
		{
		    var sParams= CrearParametrosURL(arguments, 1);
		    return URLFRONTEND.content + sParams + (sParams=="" ? "?" : "&") + "ARQ.IDAPP=" + App;
		}
	}
	else
		// No se ha indicado la Aplicacion
		showMessageCod(MSG_DATONOESPECIFICADO, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS),'ARQ',null,null,"DATO","Aplicacion");
}

//*************************************************************************
//* Quita los espacios de delante y detros de todos los campos text y     *
//* textarea de un formulario      										  *
//*************************************************************************
function trimForm(gdForms)
{
	for (x=0; x < gdForms.elements.length; x++)	
	{		
		var oElemen = gdForms.elements[x];		
		if (oElemen.type == "text" || oElemen.type == "textarea")
			oElemen.value = trim(oElemen.value);
	}
}

function tieneElementosFile(oForm)
{
	for (var x=0; x < oForm.elements.length; x++)	
		if (oForm.elements[x].type == "file")
			return true;
    return false;
}

function trimLeft( str )
{   
   return(str)?str.replace(/^[ \t]*/g,""):str;   
}
function trimRight( str	)
{
   return(str)?str.replace(/[ \t]*$/g,""):str;
}
function trim( str )
{
   return trimRight(trimLeft(str));
}
function quitarAcentos(buf) 
{				
	if (buf)
	{
		buf=buf.replace(/[\xC1\xC4]/g,'A').replace(/[\xE1\xE4]/g,'a');
		buf=buf.replace(/[\xC9\xCB]/g,'E').replace(/[\xE9\xEB]/g,'e');
		buf=buf.replace(/[\xCD\xCF]/g,'I').replace(/[\xED\xEF]/g,'i');
		buf=buf.replace(/[\xD3\xD6]/g,'O').replace(/[\xF3\xF6]/g,'o');
		buf=buf.replace(/[\xDA\xDC]/g,'U').replace(/[\xFA\xFC]/g,'u');
    }	
	return buf;
}

//**************************************************************************
//* escapaEspeciales: se encarga de escapar aquellos caracteres que tienen *
//*						cierta significacion en Expresiones regulares	   *
//**************************************************************************
function escapaEspeciales(buf)
{
   return (buf)?buf.replace(/[\/\\\.\*\+\?\|\(\)\[\]\{\}\^\$]/g,"\\$&"):buf;
}

function doNothing()
{
	return true;
}



var idCombos = new Array();




//**************************************************************************
//* cambiaColor: cambia el color de las cajas de texto cuando son          *
//*						cumplimentadas  	                               *
//**************************************************************************
function cambiaColor(obj,bError) 
{  
	var wk_gDeshabStylePreffix_NP="Deshab";
	var wk_gErrorStylePreffix_NP	="Error";
	
	if (obj != null && typeof(obj.type) != "undefined" && (obj.type.indexOf("text")==0 ||obj.type.indexOf("select-")==0) && typeof(obj.className)!= "undefined" )
	{
		var sClassName=obj.className;    	
		var iOffset=-1;   	
    	
		if ( new RegExp("^.*"+wk_gErrorStylePreffix_NP+"$").test(sClassName) )
			iOffset=sClassName.indexOf(wk_gErrorStylePreffix_NP);    		
    	    	
		var sNuevoClassNameBase=(iOffset==-1)?sClassName:sClassName.substring(0,iOffset);    	
    	    	
    	//En funcion del valor del flag, definiremos el estilo con el que pintar el elemento    	
		var sClassNameAAsignar=((bError==null || ! bError)?sNuevoClassNameBase:(sNuevoClassNameBase+wk_gErrorStylePreffix_NP));       	
   	
    	//Asignamos finalmente el codigo del estilo
		obj.className=sClassNameAAsignar;  	
	}

}   



//Definimos el manejador del evento KeyDown
if ( document.onkeydown != null )
{
  // El objeto document ya tiene definido un manejador para el evento KeyDown
  showMessageCod(MSG_MANEJADORYADEFINIDO, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS), 'ARQ',null, null, "EVENTO", document.onkeydown);
}
else
{
  document.onkeydown=fOnKeyDown;   
}

function fOnKeyDown()
{
	cambiaColor(document.activeElement);
}

function desactivarEves()
{
	document.body.onkeydown=function(){return false};
}
function activarEventos()
{
	document.body.onkeydown=fOnKeyDown;
}

//Definimos el manejador del evento Change:
if ( document.onchange != null )
{
  // El objeto document ya tiene definido un manejador para el evento Click  
  showMessageCod(MSG_MANEJADORYADEFINIDO, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS), 'ARQ', null, null, "EVENTO", document.onchange);
}
else
{
  document.onchange=function(){cambiaColor(document.activeElement);};   
}

function reemplazarSubcadena(Texto, Origen, Destino)
{
	var salida= Texto;

	if ( (Texto!= null) && (Origen != null) && (Destino != null) )
	{
		var texto= Texto;
		var posicion= 0;

		posicion= Texto.indexOf(Origen, posicion);
		while ( posicion != -1 )
		{
			salida= texto.substring(0, posicion);
			salida= salida + Destino;
			salida= salida + texto.substring(posicion + Origen.length);
			texto= salida;
			posicion= texto.indexOf(Origen, posicion+Destino.length);
		}
	}

	return salida;
}



function openWindow(sURL)
{
	var oVentana;
	if (sURL == null)
		showMessageCod(MSG_DATONOESPECIFICADO, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS),'ARQ',null,null,"DATO","URL para FrontEnd"); 
	else
		oVentana= window.open(sURL + CrearParametrosURL(arguments, 1), NOMBRE_VENTANA_WINDOWOPEN);

	return oVentana;
}

function openPopup(sCodTarea)
{
    var args= ""; 
    for (var k=1; k< arguments.length; k++)
        args+= ",'"+arguments[k]+"'";
    var href= eval("invokeApp('"+sCodTarea+"', 'SPM.CONTEXT', '" + metaTags('SPM.CONTEXT').content + "'" + args + ", 'ARQ.SPM.APPTYPE','SERVICE')");
	openPopupA(href);
}




function closePopup()
{		
	if (parent.bARQDPopupActivo)
		parent.closeWindow();
}



function createFormSalida()
{
	if (metaTags('URLFRONTEND') == null) 
	    showMessageCod(MSG_DATONOESPECIFICADO, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS),'ARQ',null,null,"DATO","URL para FrontEnd");
	else 
	{
		if ((arguments[1] == "CANCELAPP") &&
			( !metaTags('ARQ.SPM.TICKET') || metaTags('ARQ.SPM.TICKET').content == "" ) ) 
		{
			cerrarAplicacion();
		} 
		else 
		{
			var bPortalJava= false;
		    if (metaTags('TIPOPORTAL')) 
		    {
			    if (metaTags('TIPOPORTAL').content=="java") 
			    	bPortalJava= true;
			}
			if (bPortalJava && parent && !parent.bARQDPopupActivo)
			{
				window.status= "PCU:FIN:ARQ.SPM.ACTION=CANCELAPP&SPM.CONTEXT="+metaTags('SPM.CONTEXT').content+"&SPM.FROMFW3=0&ARQ.SPM.TICKET="+metaTags('ARQ.SPM.TICKET').content+"&SPM.ISPOPUP=0";
				window.status= "";
			}
			else
			{
                document.body.style.cursor="wait";
                
                oForm = document.createElement('form');
                oForm.name = "ARQFORMSALIR";
                oForm.method = "post";
                oForm.action = URLFRONTEND.content;
                document.body.appendChild(oForm);
    
                for (var k=0; k< arguments.length; k+=2)
                {
                    campo = document.createElement('input');
                    campo.type = "hidden";
                    campo.name = arguments[k];
                    campo.value = arguments[k+1];
                    
                    document.forms[document.forms.length-1].appendChild(campo);        	                    
                }
                js.submit(document.forms[document.forms.length-1], null, null, false);
			}
       	}
    }
}

function cerrarPorSesion()
{    
    if (!metaTags('TIPOPORTAL')) 
    {
    	window.status="PCB:ERROR-SESIONINVALIDA";
    	window.status="";
    	window.close();
    }
    else 
    {
	    if (metaTags('TIPOPORTAL').content=="html") 
	    {
	        if (metaTags('ARQ.URLEXIT')) 
	            location= metaTags('ARQ.URLEXIT').content;
		    else
	           	window.close();
	    } 
	    if (metaTags('TIPOPORTAL').content=="java") 
	    {
	        window.status="PCB:ERROR-SESIONINVALIDA";
           	window.status="";
	    }
	}
}

function cerrarAplicacion() 
{
    if (!metaTags('TIPOPORTAL')) 
    {
    	if (top.self['deleteTareaActiva']) 
    	{
			top.deleteTareaActiva();
    	} 
    	else 
    	{
            window.status='PCB:CERRAR';
            window.status="";
            window.close();
    	}
    } else {
	    if (metaTags('TIPOPORTAL').content=="html") 
	    {
	        if (metaTags('ARQ.URLEXIT')) 
	        {
	            location= metaTags('ARQ.URLEXIT').content;
	        }
		else if (top.document.frames.length == 0) { //No tiene Frames, con lo que es una excepcion a nivel de portal
	           	window.close();
			} else {
				if (parent.bARQDPopupActivo) {
					js.closePopup();
				} else {
					top.deleteTareaActiva();
				}
			}
	    } else {
            if (parent.bARQDPopupActivo)
                js.closePopup();
            else
            {
                //window.status='PCB:CERRAR';
                window.close();
            }
		}
	}
}

function cancelApp()
{
    createFormSalida("ARQ.SPM.ACTION", "CANCELAPP");
}

function logOut(bLaunchServerRequest)
{
    if (bLaunchServerRequest)
        createFormSalida("ARQ.SPM.ACTION", "LOGOUT");
    else
        window.close();
}

function deshabilitarSubmit()
{
    for (var k=0; k<document.forms.length; k++)
        document.forms[k].onsubmit= new Function("return false;");
}

//*************************************************************************
//* quitarRetornos: quita los retornos de carro de los strings 			  *
//* @param sCadena: elemento a limpiar                             		  *
//* @return string: cadena limpia                                 		  *
//*************************************************************************	
function quitarRetornos(sCadena)
{
		var sCadLimpia = escape(sCadena);
		
		re = /%0D/gi;
		sCadLimpia = sCadLimpia.replace(re, ' ');
		re = /%0A/gi;		
		sCadLimpia = sCadLimpia.replace(re, ' ');
		re = /%20/gi;
		sCadLimpia = sCadLimpia.replace(re, ' ');
	
		return sCadLimpia;
}

