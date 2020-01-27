
/******** Variables PRIVADAS ARQUITECTURA ********/
var fieldViewC = new FieldViewCollection();
var ParqValidaJS= true;
var oARQDFuncValidacion = new Array();
var oARQDFuncValidacionParticular = new Array();  // para tipos de datos que inserte el usuario
var sDecimalJS = '.';
//Definicion de	constantes de para los codigos de propiedad y de tipo de dato
var prBase	       = 1;
var dtBase	       = 1;
var dtParticulares = dtBase + 100;
var iNumDtParticulares= 0;
var dtError		   = dtBase * -1;
var bValidar= true;
var bARQIncluirFicheroMensajes= false;
/****** FIN Variables PRIVADAS ARQUITECTURA ******/

/******** Variables PUBLICAS ********/
var gValGeneral = "validacionGeneral(oFormulario)";
var gPreSubmit = "preSubmit(oFormulario)";
var sDecimal = ",";
var sSepFec = "/";
var sSepHora = ':';
var sSepFecHora = ' ';

//Propiedades validas para numeros (gissInteger	y gissReal)
var prNoZero	 		  =	prBase + 1;
var prOnlyPositive	      =	prBase + 2;
var prOnlyPositiveAndZero =	prBase + 3;
var prOnlyNegative        =	prBase + 4;
var prOnlyNegativeAndZero =	prBase + 5;
//Propiedades validas para cadenas alfanumericas (gissAlpha y gissAlphaNum)
var prUpperCaseOnly	      =	prBase + 6;
var prLowerCaseOnly	      =	prBase + 7;
//Constantes de Tipos de Datos
var dtNumeric	      			= dtBase + 1;
var dtInteger	      			= dtBase + 2;
var dtReal		      			= dtBase + 3;
var dtMonedaEuro	  			= dtBase + 4;
var dtAlpha		      			= dtBase + 5;
var dtTokenList	      		    = dtBase + 6;
var dtNonAlpha	      		    = dtBase + 7;
var dtAlphaNumeric	  		    = dtBase + 8;
var dtNonAlphaNumeric 		    = dtBase + 9;
var dtDate		      			= dtBase + 10;
var dtDay		      			= dtBase + 11;
var dtMonth	      				= dtBase + 12;
var dtYear	      				= dtBase + 13;
var dtTime		      			= dtBase + 14;
var dtHour	      				= dtBase + 15;
var dtSecond      				= dtBase + 16;
var dtMinute      				= dtBase + 17;
var dtDateTime	      		    = dtBase + 18;
var dtFormatted	      		    = dtBase + 19;
var dtRadioButton				= dtBase + 20;
/****** FIN Variables PUBLICAS ******/

//*************************************************************************
//* Carga el fichero de errores de validaciones							  *
//*************************************************************************
function InicializarValidaciones(sRutaFichXMLValid)
{
   if ((bARQIncluirFicheroMensajes) || self['validacionGeneral'])
		IncluirFicheroMensajes(sRutaFichXMLValid + 'ParqMensajes', 'ARQ');
}

//*************************************************************************
//*	Recorre los campos de un formulario llamando a su validacion		  *
//*************************************************************************		
function validaDatos(oFormulario)
{
	var sStringErrores = "";
	var sResultado   = "";
	var gdForms = "";
	var sTipoP;
	var oPrimerElemenError;
	var sRetorno = new ParqMensaje('', null, 'ARQ');
	
	sTipoP = typeof(oFormulario);
	
	if (sTipoP.toUpperCase() == "OBJECT") //Es formulario, obtengo nombre (si es que realmente lo necesito para algo
		gdForms = oFormulario;
	else
	{		
		if ( sTipoP.toUpperCase() == "STRING" ) //Es el nombre del formulario
			gdForms = document.forms(oFormulario);
		else	
		{	
			// ERROR EN EL PARAMETRO DEL FORMULARIO A VALIDAR
			showMessageCod(MSG_DATONOESPECIFICADO, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS),null,null,"DATO","Formulario a validar");
			return "Formulario desconocido";
		}
	}

	// Recorremos el array de validaciones
	for (cont=0; cont < oArrayValidaciones.length; cont++)	
	{				
		sResultado="";
		var oElemen;
		// var oElemen = gdForms.elements[cont];
		
		var oValidacion = oArrayValidaciones[cont];
		
		if (oValidacion.id.length > 0)
			oElemen = document.getElementById(oValidacion.id);
		else
			oElemen = gdForms.elements[oValidacion.nombre];
		
		if (oElemen!=null && oElemen.type == undefined && oElemen[0]!=null && (oElemen[0].type == "radio" || oElemen[0].type == "checkbox") ){
			oElemen = oElemen[0];
		}
		
			
		if (oElemen!=null  &&  oElemen.type != undefined  && (oElemen.type == "text" || oElemen.type == "password" || oElemen.type == "textarea" || oElemen.type == "file" || oElemen.type.substring(0,6) == "select" || oElemen.type == "radio" || oElemen.type == "checkbox"))
		{	
			desmarcarCampoError(oElemen);
					
			if (!oElemen.disabled){
				sResultado = validaDato(oElemen, oValidacion);
				}	
	
			var sTipoResultado= typeof(sResultado);
			if (sTipoResultado.toUpperCase() == "STRING")
			{
				if (sResultado != "")
				{
					if (sStringErrores == "")
						oPrimerElemenError= oElemen;
					sStringErrores = sStringErrores + (sStringErrores.length==0?"":"\n") + (cont+1) + ".- " + ((sResultado.substring(0,2)=="{{")?sResultado.substring(2,sResultado.length-2):getMensajeARQD(sResultado));
					//cambiaColor(oElemen, true);  // se cambia el color para indicar el error
					marcarCampoError(oElemen);
				}
			}
			else if ( sTipoResultado.toUpperCase() == "OBJECT" )
			{   // deberoa ser un objeto ParqMensaje			
				if (sResultado.codigo != "")
				{
					if (sStringErrores == "")
						oPrimerElemenError= oElemen;
					var args= "";
					for (var k=0; k< sResultado.params.length; k++)
						args+= ",'"+sResultado.params[k].nombre+"','"+sResultado.params[k].valor+"'";
					
					var sTextoMsg;
					try
					{
						if (sResultado.dominio != 'ARQ' && sResultado.dominio != null)
							sTextoMsg= eval("getMensaje('"+sResultado.codigo+"','"+sResultado.dominio+"'"+args+")");
						else
							sTextoMsg= eval("getMensajeARQD('"+sResultado.codigo+"'"+args+")");
					}
					catch(e)
					{
						sTextoMsg= eval("getMensaje('"+sResultado.codigo+"','"+sResultado.dominio+"'"+args+")");
					}
					
					sStringErrores = sStringErrores + (sStringErrores.length==0?"":"\n") + sTextoMsg ;					
					//cambiaColor(oElemen, true);  // se cambia el color para indicar el error
					marcarCampoError(oElemen);
				}
			}
		}
	}	
	
	if (sStringErrores == "") // NO HAY ERRORES	
	{
		if(self['validacionGeneral']) 
		{     		
			sRetorno = eval(gValGeneral);        	
			
			var typRet= typeof(sRetorno);

			if (typRet.toUpperCase() == "STRING")
				sRetorno= new ParqMensaje(sRetorno, null, null);

			// Es un Array de ParqMensajes
			if ( (!sRetorno.codigo) && (sRetorno.codigo != '') )
			{  
				for (var k=0; k< sRetorno.length; k++)
				{
					var oRetornoK= sRetorno[k];
					if (oRetornoK.codigo != "")
					{
						var args= "";
						for (var m=0; m< oRetornoK.params.length; m++)
							args+= ",'"+oRetornoK.params[m].nombre+"','"+oRetornoK.params[m].valor+"'";
						
						var sTextoMsg;
						try
						{
							sTextoMsg= eval("getMensaje('"+oRetornoK.codigo+"','"+oRetornoK.dominio+"'"+args+")");
						}
						catch(e)
						{
							sTextoMsg= eval("getMensajeARQD('"+oRetornoK.codigo+"'"+args+")");
						}
						
						sStringErrores = sStringErrores + (sStringErrores.length==0?"":"\n") + sTextoMsg ;					
					}
				}
				
				if (sStringErrores != "")
				{				
					showMessage(sStringErrores, iMsgTipoCerrar, 'Validacion de datos', 'Se han producido los siguientes errores:');
				}
				
				return sStringErrores;
			}
			//UN SOLO MENSAJE
			if (sRetorno.codigo != "") 
			{
				var args= "";
				for (var k=0; k< sRetorno.params.length; k++)                
					args+= ",'"+sRetorno.params[k].nombre+"','"+sRetorno.params[k].valor+"'";

				var sTextoMsg;
				try
				{
					sTextoMsg= eval("getMensaje('"+sRetorno.codigo+"','"+sRetorno.dominio+"'"+args+")" );
				}
				catch(e)
				{
					sTextoMsg= eval("getMensajeARQD('"+sRetorno.codigo+"'"+args+")");
				}

				sStringErrores = sStringErrores + (sStringErrores.length==0?"":"\n") + sTextoMsg ;					
					
				// eval("showMessageCod('"+sRetorno.codigo+"', '"+sRetorno.dominio+"', "+iMsgTipoCerrar+", 'Validacion de datos global', 'Se han producido los siguientes errores:',null,null"+oParamMsg+")");
				showMessage(sStringErrores, iMsgTipoCerrar, 'Validacion de datos', 'Se han producido los siguientes errores:');
			}			
		}
		
		if (self['preSubmit'])
		{
			sRetorno = eval(gPreSubmit);
		}				
		
		return sRetorno.codigo;
	}
	else
	{
		showMessage(sStringErrores, iMsgTipoCerrar, 'Validacion de datos', 'Se han producido los siguientes errores:');
		
		try 
		{ 
			oPrimerElemenError.focus(); // ponemos el foco en el campo	
			oPrimerElemenError.select(); // seleccionamos el contenido del campo								
		} 
		catch(e) {}
		
		if (self['preSubmit'])
		{
			sRetorno = eval(gPreSubmit);
		}	
		
		return sStringErrores;
	}	
}


//*************************************************************************
// Valida si una validacion se puede ejecutar en funcion del boton pulsado*                                                                       *
//*************************************************************************

function getEjecucionValidacion(oObjVali)
{
	//  NO HAY, ASUMIMOS TODOS LOS POSIBLES
	if (oObjVali.botones == '')
		return true;

	if (oObjVali.botones.indexOf(',') != -1) // es un array
	{
		var oBot = new Array();
		oBot = oObjVali.botones.split(',');
		for (x=0; x < oBot.length; x++)
		{
			if ("SPM.ACC." + oBot[x] == sBotonActivo)
				return true;
		}	
	}
	
	if ("SPM.ACC." + oObjVali.botones == sBotonActivo)
		return true;
	else
		return false;

}

//*************************************************************************
//*	Valida un campo de un formulario y devuelve error si lo hubiera		  *
//*************************************************************************	
function validaDato(oElemen, oValidacion)
{
	// COMPROBAMOS QUE SE PUEDE EJECUTAR SU VALIDACION POR EL BOTON PULSADO
	if (!getEjecucionValidacion(oValidacion))
		return new ParqMensaje('', null, null);

	var sValor       = trim(oElemen.value);
	// TEXTAREAS, quitamos retornos de carro y demos para que no fallen los EVAL
	if (oElemen.type == "textarea")
		sValor = quitarRetornos(sValor);
	
	var sTipoDato    = oValidacion.tipodato;			
	var bObligatorio = oValidacion.obligatorio;
	var sValidacion  = oValidacion.validacion;
	var sCualificador= oValidacion.cualificador;
	var sIdError     = oValidacion.idEV;
	var oMsgError;
	var bTipoDatoArquitectura= true;
	
	// SI VIENE MENSAJE, MACHACAMOS EL CONCEPTO	
	if (oValidacion.idMensaje != null && oValidacion.idMensaje != '')
	{	
		sCon = "";
		try 
		{ 
			sCon = getMensaje(oValidacion.idMensaje, oValidacion.dominio);
		}
		catch(e)
		{	
			sCon = oValidacion.concepto;
		}	
		oValidacion.concepto = sCon;			
	}
	
	if ( (bObligatorio == 1) && (sValor == ''))	
	{	
		return new ParqMsgObligatorio(oValidacion.concepto);
	}
	
	// Comprobamos que cumple la longitud minima esperada (si asi se especifico):
	if ( oValidacion.longmin != null && sValor != '' )
	{
		if (trim(sValor).length < oValidacion.longmin)
		{
			return new ParqMsgErrorLongMinima(oValidacion.concepto, oValidacion.longmin, sValor);
		}
	}
	if ( ( sTipoDato != null ) && ( sValor != '' ) )
	{
	var itipoDato     = null;				      
	//Comprobamos que el valor del tipo de dato es un tipo de dato permitido
	try 
	{ 
		itipoDato = eval(sTipoDato);
	}
	catch(e)
	{	
		bTipoDatoArquitectura= false; 
	}
	
	if (sTipoDato)
	{
		var oParam= null;
		if (sTipoDato == 'dtFormatted') 
		{
			if (sCualificador != null && sCualificador != '')
				oParam= sCualificador;
			else
			{
				return new ParqMsgElementoSinFormato(oValidacion.concepto);
			}    		
		}
		else if ( (sCualificador != null) && (sCualificador != ""))
			oParam= eval(sCualificador);
		if (bTipoDatoArquitectura)
		{
			//Validamos la funcion
			if (oParam)
				oMsgError = oARQDFuncValidacion[getFnIndex(itipoDato)](sValor, oValidacion.concepto, oParam);
			else{
				
				if (sTipoDato == 'dtRadioButton'){
					oMsgError = oARQDFuncValidacion[getFnIndex(itipoDato)](oValidacion);
				} else{
					oMsgError = oARQDFuncValidacion[getFnIndex(itipoDato)](sValor, oValidacion.concepto);
				}
			}
			if (oMsgError.codigo.length > 0)
				return (sIdError == null || sIdError == '') ? oMsgError : new ParqMensaje(sIdError, null, 'ARQ');
		}
		else
		{
			// Puede ser que se deba a que es un nuevo tipo de datos definido por el 
			//	usuario dinomicamente (mediante la funcion annadirTipoDatos():
			var idex= getIndexFuncionDeTipoDato(sTipoDato);
			if (idex != -1)
			{
				var oRetFuncPart= new ParqMensaje("",null, 'ARQ');
				try
				{
					var sTipoP= typeof(oARQDFuncValidacionParticular[idex]);
					if (sTipoP.toUpperCase() == "FUNCTION") 
					{
						if (oParam)
							oRetFuncPart= oARQDFuncValidacionParticular[idex](sValor, oParam);
						else
							oRetFuncPart= oARQDFuncValidacionParticular[idex](sValor);
					}
					else
					{
						if (oParam)
							oRetFuncPart= eval(oARQDFuncValidacionParticular[idex])(sValor, oParam);
						else
							oRetFuncPart= eval(oARQDFuncValidacionParticular[idex])(sValor);
					}
					var typRet= typeof(oRetFuncPart);
					if (typRet.toUpperCase() == "STRING")
						oRetFuncPart= new ParqMensaje(null, oRetFuncPart, null);
				}
				catch(e)
				{
					return new ParqMsgFuncValidNoDefinida(oARQDFuncValidacionParticular[idex]);
				}
				if (oRetFuncPart.codigo.length > 0)
					return (sIdError == null) ? oRetFuncPart : (new ParqMensaje(sIdError,null,'ARQ'));
				}
				else
				{
					return new ParqMsgTipoDatoNoPermitido(sTipoDato);
				}
			}
		}	    
	}
	
	// EJECUTAMOS SU VALIDACION PARTICULAR SI LA TIENE
	if (sValidacion != undefined && sValidacion != '')
	{
		var oRetValPart;
		if(self[sValidacion.substring(0, (sValidacion.indexOf("(")==-1?sValidacion.length:sValidacion.indexOf("(")) )]) 
		{ 
			sValidacion= sValidacion + (sValidacion.indexOf("()")==-1?"()":"") ;
			oRetValPart= eval(sValidacion);
			var typRet= typeof(oRetValPart);
			if (typRet.toUpperCase() == "STRING")
			{
				return new ParqMensaje(oRetValPart, null);
			}
			else
			{
				return oRetValPart;
			}
		}
		else
		{
			return new ParqMsgFuncValidNoDefinida(sValidacion);
		}
	}
	
	// SI TODO HA IDO BIEN
	return new ParqMensaje('', null, null);
}

function oValida(id, nombre, longmin, obligatorio, tipodato, validacion, cualificador, idEV, concepto, idMensaje, dominio, botones)
{
	this.id= id;
	this.nombre = nombre;
	this.longmin = longmin;
	this.obligatorio = obligatorio;
	this.tipodato = tipodato;
	this.validacion = validacion;
	this.cualificador = cualificador;
	this.idEV = idEV;
	this.concepto = concepto;
	this.idMensaje = idMensaje;
	this.dominio = dominio;
	this.botones = botones;

	if (this.concepto.length == 0)
		this.concepto= nombre;
}

//**************************************************************************
//* annadirTipoDato: Aoade un nuevo tipo de datos junto con la funcion que *
//*                      lo valida.                           	           *
//* @param sTipoDato: nombre del tipo de dato                              *
//* @param sFuncion:  Funcion de validacion del tipo de datos			   *
//**************************************************************************
function annadirTipoDato(sTipoDato, sFuncion)
{
	iNumDtParticulares++;
	oARQDFuncValidacionParticular[dtParticulares + iNumDtParticulares]= trim(sTipoDato);	
	iNumDtParticulares++;
	oARQDFuncValidacionParticular[dtParticulares + iNumDtParticulares]= sFuncion;	
}

//**************************************************************************
//* annadirVariosTiposDatos: Aoade varios tipos de datos  junto con las    *
//*                      funciones que los validan                         *
//* @param sTiposDato:  nombre de los tipos de datos (separados por coma)  *
//* @param sFunciones:  Funciones de validacion de los tipos de datos      *
//*                     (separadas por coma)                               *
//* Ejemplo: annadirVariosTiposDatos('dt1,dt2,dt3', 'func1,func2,func3')   *
//**************************************************************************
function annadirVariosTiposDatos(sTiposDato, sFunciones)
{
	var oArrayTD= sTiposDato.split(',');
	var oArrayFunc= sFunciones.split(',');
	if (oArrayTD.length != oArrayFunc.length)
		// En la funcion de aoadir tipos de datos, se ha especificado distinto nomero de tipos de datos y de funciones de validacion
		showMessageCod(MSG_ERRORANNADIRTIPOSDATOS, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS));
	else
		for (var k=0; k< oArrayTD.length; k++)
		{ 
			try 
			{
				annadirTipoDato(oArrayTD[k], eval(oArrayFunc[k]));        
			} 
			catch (e)
			{
				// La funcion "+oArrayFunc[k]+" no existe (quizos deba insertar el fichero Javascript)
				showMessageCod(MSG_FUNCIONNOEXISTE, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS), null, null, "FUNCION", oArrayFunc[k]);
			}
		}
}

function validaFechaPorItem(dia, mes, anio)
{
	var bRet = false;
	if ((dia=trim(dia)).length && (mes=trim(mes)).length && (anio=trim(anio)).length == 4)
	{
		var f1 = Date.parse(mes + "/" + dia + "/" + anio);
		if ( isFinite(f1) )
		{
			var f2 = new Date(f1);
			//Comparamos la fecha pasada con lo devuelto para la fecha calculada
			bRet = f2.getDate()==dia && (f2.getMonth()+ 1)==mes && f2.getFullYear()==anio;
		}
	}
	return bRet;
}

function validFecha(strFecha, sSep)
{
	var bRet=false;
	if (strFecha && (strFecha=trim(strFecha)).length)
	{
		var fArray = strFecha.split(sSep);
		bRet=(fArray && fArray.length==3)?validaFechaPorItem(fArray[0],fArray[1],fArray[2]):bRet;
	}
	return bRet;
}

//**************************************************************************
//* getFnIndex: Funcion que	se encarga de obtener el indice	del elemento en*
//*						base al codigo de tipo de dato                 	   *
//**************************************************************************
function getFnIndex(nDtIndice)
{
	var nIndexRetValue=-1;
	if (nDtIndice == null)
		// Tipo de Dato NO DEFINIDO/INVALIDO.Compruebe el identificador de tipo de dato especificado
		showMessageCod(MSG_TIPODATONODEFINIDO, iMsgTipoCerrar, getMensajeARQD(MSG_ERROR), getMensajeARQD(MSG_ERRORJS));
	else
		nIndexRetValue=nDtIndice - dtBase;
	return nIndexRetValue;
}

//**************************************************************************
//* getFnIndex: Funcion que	se encarga de obtener el indice	del elemento en*
//*						base al tipo de dato (particular)    	           *
//**************************************************************************
function getIndexFuncionDeTipoDato(sTipoDato)
{
	for (var k= (dtParticulares+1); k<= (dtParticulares + iNumDtParticulares); k+=2)
	{
		if (oARQDFuncValidacionParticular[k] == sTipoDato)
		return k+1;
	}
	return -1;
}

//*************************************************************************
//* isDtFormatted: Funcion encargada de comprobar si la cadena pasada como*
//*					primer	parametro es valida de acuerdo al patron de   *
//*					correspondencia especificado como segundo parametro   *
//* @param buf: cadena de caracteres a validar   						  *
//* @param sMatchP: patron de correspondencia contra el que validar		  *
//* @param sFlag: cualificador de la Expresion regular					  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtFormatted(buf,parNombre,sMatchP,sFlag)
{
	var bRet = false;
	
	if (buf && sMatchP && trim(buf).length	&& trim(sMatchP).length)
	{
		// Comprobamos que nos llega el circunflejo '^' y	el signo dolar '$' al comienzo
		//y final, respectivamente, del patron de	comparacion. Si	no es asi, los aniadi-
		//mos.
		sMatchP=((sMatchP.charAt(0)!='^')?"^":"") + sMatchP + ((sMatchP.charAt(sMatchP.length-1)!='$' )?"$":"");
		//Validamos el buffer contra la expresion	regular
		bRet= new RegExp(sMatchP,((sFlag)?trim(sFlag):"")).test(buf);
	}
	if (bRet)
		return new ParqMensaje("",null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "(formato personalizado)", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtFormatted)]=isDtFormatted;

//*************************************************************************
//* isDtNumeric: Funcion encargada de comprobar	si la cadena pasada como  *
//*					parametro se corresponde con un valor numerico entero *
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtNumeric(buf, parNombre)
{
	if (isDtFormatted(buf,parNombre,"^[0-9]+$").codigo=="")
		return new ParqMensaje("", null, null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "numorico", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtNumeric)]=isDtNumeric;

//*************************************************************************
//* isDtInteger: Funcion encargada de comprobar	si la cadena pasada como  *
//*					parametro se corresponde con un valor numerico entero *
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtInteger(buf,parNombre,sCualif)
{
	var	bRet=(isDtFormatted(buf,parNombre,"^[+-]?[0-9]+$").codigo=="");
	
	if (bRet && sCualif)
	{
		//Lo convertimos a nuemrico entero
		var iParsedItem = parseInt(buf);
		
		//Hacemos uso del cualificador
		switch(sCualif)
		{
			case prNoZero:		     			 bRet = iParsedItem !=0;break;
			case prOnlyPositive:	     	 bRet = iParsedItem >	0;break;
			case prOnlyPositiveAndZero: bRet = iParsedItem >=0;break;
			case prOnlyNegative:	     	 bRet = iParsedItem <	0;break;
			case prOnlyNegativeAndZero: bRet = iParsedItem <=0;break;
		}
	}
	if (bRet)
		return new ParqMensaje("",null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "nomerico entero", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtInteger)]=isDtInteger;

//*************************************************************************
//* isDtReal: Funcion encargada de comprobar	si la cadena pasada como  *
//*					parametro se corresponde con un valor numerico real.  *
//*				El separador de decimales es la coma (,) y NO SE          *
//*					ESPECIFICA decimales.  								  *
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtReal(buf,parNombre,sCualif)
{
	var bRet= (isDtFormatted(buf,parNombre,"^[+-]?[0-9]+([,|.][0-9]*)?$").codigo=="");
	
	//Comprobamos si a especificado	algun cualificador
	if (sCualif && sCualif.length && bRet)
	{
		var fParsedItem = parseFloat(buf.replace(new RegExp(sDecimal),sDecimalJS));
		switch(sCualif)
		{
			case prNoZero: bRet = fParsedItem != 0;break;
			case prOnlyPositive: bRet = fParsedItem >  0;break;
			case prOnlyPositiveAndZero: bRet = fParsedItem >= 0;break;
			case prOnlyNegative: bRet = fParsedItem <  0;break;
			case prOnlyNegativeAndZero: bRet = fParsedItem <= 0;break;
		}
	}
	if (bRet)
		return new ParqMensaje("",null);
	else
	{		
		return new ParqMsgFormatoIncorrecto(parNombre, "numorico real", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtReal)]=isDtReal;

//*************************************************************************
//* isDtMonedaEuro: Funcion encargada de comprobar	si la cadena pasada como  
//*					parametro se corresponde con una cantidad monetaria expresada en Euros.
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtMonedaEuro(buf,parNombre)
{		
	if (isDtFormatted(buf,parNombre,"^(\d|-)?(\\d{1,3}[.]{0,1}){0,1}(\\d{1,3}[.]{0,1}){0,1}(\\d{1,3})([,]\\d{1,2}){0,1}$").codigo=="")
		return new ParqMensaje("",null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "moneda Euro", buf);	
	}
}
oARQDFuncValidacion[getFnIndex(dtMonedaEuro)]=isDtMonedaEuro;

//*************************************************************************
//* isDtAlpha: Funcion encargada de comprobar	si la cadena pasada como  
//*					parametro se corresponde con valor Alfabetico exclusivamente.
//* @param buf: cadena de caracteres a validar   						  *
//* @param sCualif: cualificador del buffer.                              *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtAlpha(buf,parNombre,sCualif)
{
	var bRet=false;
	
	if (buf && trim(buf).length)
	{
		buf=quitarAcentos(trim(buf));
		
		//Asignamos el patron por defecto
		var sPat2Apply="^[\x20-\x2F\x3A-\xFF]+$";
		
		//Comprobamos si existe	cualificador para el buffer de entrada.	Si no es asi,
		//lo que hacemos es definirle el patron	completo
		if (sCualif && sCualif.length)
			switch(sCualif) //Comprobamos cual es el cualificador expresado para
			{
				case prUpperCaseOnly: sPat2Apply = "^[A-Z]+$";break;
				case prLowerCaseOnly: sPat2Apply = "^[a-z]+$";break;
				default: sPat2Apply = ""; //ERROR
			}
		
		if (sPat2Apply.length) 
			bRet = (isDtFormatted(buf,parNombre,sPat2Apply).codigo=="");
	}
	
	if (bRet)
		return new ParqMensaje("",null)
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "alfabotico", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtAlpha)]=isDtAlpha;

//*************************************************************************
//* isDtTokenList: Funcion encargada de comprobar	si la cadena pasada como  
//*					parametro se corresponde con valor Alfabetico exclusivamente.
//* @param buf: cadena de caracteres a validar   						  *
//* @param sCualif: cualificador del buffer.                              *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtTokenList(buf,parNombre,sCualif)
{
	var bRet=false;
	
	if (buf != null && trim(buf).length)
	{
		buf = quitarAcentos(trim(buf));
		
		//Asignamos el patron por defecto
		var sPat2Apply=	"^[\x20-\x2F\x3A-\xFF][\x20-\x2F\x3A-\xFF \t]*$";
		
		//Comprobamos si existe	cualificador para el buffer de entrada.	Si no es asi,
		//lo que hacemos es definirle el patron completo
		if (sCualif && sCualif.length)
			switch(sCualif) //Comprobamos cual es el cualificador expresado para
			{
				case prUpperCaseOnly:	sPat2Apply = "^[A-Z][A-Z \t]*$";break;
				case prLowerCaseOnly:	sPat2Apply = "^[a-z][a-z \t]*$";break;
				default: sPat2Apply = ""; //ERROR
			}
		
		if (sPat2Apply.length) 
			bRet= (isDtFormatted(buf,parNombre,sPat2Apply).codigo=="");
	}
	
	if (bRet)
		return new ParqMensaje("",null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "lista de elementos", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtTokenList)]=isDtTokenList;

//*************************************************************************
//* isDtNonAlpha: Funcion encargada de comprobar	si la cadena pasada como  
//*					parametro se corresponde con valor compuesto unicamente de caracteres
//    				diferentes a los alfabeticos. Se invoca a la funcion isDtAlpha(),
//    				negando el valor devuelto
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtNonAlpha(buf,parNombre)
{
	if (isDtAlpha(buf).codigo=="")
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "no alfabotico", buf);
	}
	else
		return new ParqMensaje("",null);
}
oARQDFuncValidacion[getFnIndex(dtNonAlpha)]=isDtNonAlpha;

//*************************************************************************
//* isDtAlphaNumeric: Funcion encargada de comprobar	si la cadena pasada como  
//*					parametro se corresponde con valor alfanumorico exclusivamente.
//* @param buf: cadena de caracteres a validar   						  *
//* @param sCualif: cualificador del buffer.                              *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtAlphaNumeric(buf,parNombre,sCualif)
{
	var bRet = false;
	
	if (buf && trim(buf).length)
	{
		var sPat2Apply="^[\x20-\xFF]+$";
		if	(sCualif && sCualif.length)
			switch(sCualif) //Comprobamos cual es el cualificador expresado	para
			{
				case prUpperCaseOnly: sPat2Apply = "^[\x20-\x60]+$";break;
				case prLowerCaseOnly: sPat2Apply = "^[\x20-\x40\x5B-\xFF]+$";break;
				default: sPat2Apply = ""; //ERROR
			}
		
		if	(sPat2Apply.length) 
			bRet =	(isDtFormatted(buf,parNombre,sPat2Apply).codigo=="");
	}
	
	if (bRet)
		return new ParqMensaje("",null)
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "alfanumorico", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtAlphaNumeric)]=isDtAlphaNumeric;

//*************************************************************************
//* isDtNonAlphaNumeric: Funcion encargada de comprobar	si la cadena pasada como  
//*					parametro se corresponde con un valor compuesto unicamente de caracteres	
//*					distinto a los	alfanumericos.Para ello, se niega el valor resultante
//*		    		de la llamada a la rutina isDtAlphaNumeric()
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtNonAlphaNumeric(buf, parNombre)
{
	if (isDtAlphaNumeric(buf).codigo=="")
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "no alfanumorico", buf);
	}
	else
		return new ParqMensaje("",null);
}
oARQDFuncValidacion[getFnIndex(dtNonAlphaNumeric)]=isDtNonAlphaNumeric;

//*************************************************************************
//* isDtTime: Funcion encargada de comprobar si la cadena pasada como  
//*					parametro se corresponde con una hora volida.
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtTime(buf, parNombre)
{
	var bRet= false;
	if(buf	&& sSepHora && trim(buf).length)
	{
		//Escapamos los	caracteres especiales, siempre que no vengan de	isDtDateTime
		var bEscapar = isDtTime.caller != isDtDateTime;
		bRet = (isDtFormatted(buf,parNombre,"^([01]|2(?=[0-3]))[0-9]("+ sSepHora + "[0-5][0-9]){2}$").codigo=="");
	}
	
	if (bRet)
		return new ParqMensaje("",null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "hora", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtTime)]=isDtTime;


//*************************************************************************
//* isDtHour: Funcion encargada de comprobar si la cadena pasada como  
//*					parametro se corresponde con un valor de hora volida.
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtHour(buf,parNombre)
{
	if (isDtFormatted(buf,parNombre,"^([0-1][0-9]?|2[0-3]{0,1})$").codigo=="")
		return new ParqMensaje("",null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "hora", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtHour)]=isDtHour;

//*************************************************************************
//* isDtMinute: Funcion encargada de comprobar si la cadena pasada como  
//*					parametro se corresponde con un valor de minuto volida.
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtMinute(buf,parNombre)
{
	if (isDtFormatted(buf,parNombre,"^([0-5][0-9]?)$").codigo=="")
		return new ParqMensaje("",null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "minuto", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtMinute)]=isDtMinute;

//*************************************************************************
//* isDtSecond: Funcion encargada de comprobar si la cadena pasada como  
//*					parametro se corresponde con un valor de segundo volida.
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtSecond(buf, parNombre)
{
	if (isDtMinute(buf).codigo=="")
		return new ParqMensaje("",null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "segundo", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtSecond)]=isDtSecond;

//*************************************************************************
//* isDtDate: Funcion encargada de comprobar si la cadena pasada como  
//*					parametro se corresponde con una fecha volida.
//*           La fecha debera venir informada de la forma dd[sep]MM[sep]yyyy
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtDate(buf, parNombre)
{
	var bRet	= false;
	if (buf != null && trim(buf).length)
	{
		//Escapamos los caracteres especiales, siempre que no vengan de isDtDateTime
		var bEscapar = isDtDate.caller != isDtDateTime;
		
		var sPat2Apply  = "^[0-3][0-9]" + sSepFec;
		sPat2Apply     +=  "[01][0-9]" + sSepFec;
		sPat2Apply     +=  "(19|20)[0-9]{2}$";
		
		if (isDtFormatted(buf,parNombre,sPat2Apply).codigo=="") 
			bRet= validFecha(buf,sSepFec);
	}
	
	if (bRet)
		return new ParqMensaje("",null)
	else
	{
		return new ParqMsgFormatoFechaIncorrecto(parNombre, "dd/mm/aaaa", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtDate)]=isDtDate;

//*************************************************************************
//* isDtDay: Funcion encargada de comprobar	si la cadena pasada como  
//*					parametro se corresponde con valor de doa volido.
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtDay(buf,parNombre)
{
	if (isDtFormatted(buf,parNombre,"^(0[1-9]{1}|(1|2)[0-9]{0,1}|3[0-1]{0,1})$").codigo=="")
		return new ParqMensaje("",null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "doa", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtDay)]=isDtDay;

//*************************************************************************
//* isDtMonth: Funcion encargada de comprobar	si la cadena pasada como  
//*					parametro se corresponde con valor de mes volido.
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtMonth(buf,parNombre)
{
	if (isDtFormatted(buf,parNombre,"^(0[1-9])|(1[0-2]?)$").codigo=="")
		return new ParqMensaje("",null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "mes", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtMonth)]=isDtMonth;

//*************************************************************************
//* isDtYear: Funcion encargada de comprobar	si la cadena pasada como  
//*					parametro se corresponde con valor de aoo volido.
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtYear(buf,parNombre)
{
	if (isDtFormatted(buf,parNombre,"^(19|20)[0-9][0-9]$").codigo=="")
		return new ParqMensaje("",null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "aoo", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtYear)]=isDtYear;

//*************************************************************************
//* isDtDateTime: Funcion encargada de comprobar si la cadena pasada como  
//*					parametro se corresponde con una fecha volida.
//*           La fecha debera venir informada de la forma:
//*				dd<sepFecha>MM<sepFecha>yyyy<sepFechaHora>HH<sepHora>mm<sepHora>ss
//* @param buf: cadena de caracteres a validar   						  *
//* @return devuelve una cadena vacoa si se cumple la validacion, o bien  *
//*		un texto de error si oste se produce               				  *
//*************************************************************************	
function isDtDateTime(buf,parNombre)
{
	var bRet	= false;
	if (buf)
	{
		var sPat2Apply  = "";
		
		sSepFec =(sSepFec  && sSepFec.length )?escapaEspeciales(sSepFec) :"";
		sSepFecHora=(sSepFecHora && sSepFecHora.length)?escapaEspeciales(sSepFecHora):"";
		sSepHora =(sSepHora  && sSepHora.length )?escapaEspeciales(sSepHora) :"";
		
		sPat2Apply	+= "^" 	 + "([0-2]|3(?=[01]))][0-9]";	 									//Comienzo de linea y doa del mes
		sPat2Apply	+= sSepFec + "(0(?=[^0])|1(?=[012]))[0-9]";								//Mes del anio
		sPat2Apply	+= sSepFec + "(19|20|21)[0-9]{2}";	 											//Anio
		sPat2Apply	+= sSepFecHora;				 																			//Separador Fecha/Hora
		sPat2Apply	+= "([01]|2(?=[0-3]))[0-9]("+ sSepHora + "[0-5][0-9]){2}";	//Hora
		sPat2Apply	+= "$";					 																				//Final de linea
		
		//Comprobamos que la fecha	hora se	corresponde con	un valor de fecha hora valido
		bRet = isDtFormatted(buf,parNombre,sPat2Apply).codigo=="";
		
		if	(bRet && buf && trim(buf).length)
		{
			var bHayErr=false;
			
			var sFH = trim(buf);
			var sF;
			var sH;
			
			var sEndSepF  = (sSepFec)?sSepFec :sSepFec;
			var sEndSepH  = (sSepHora)?sSepHora :sSepHora;
			var sEndSepFH = (sSepFec)?sSepFecHora:sSepFecHora;
			
			if (sSepFec.length)
			{
				bHayErr = ! ( sFH.length >= 8 );
				if (! bHayErr)
				{
					sEndSepF = sSepFec;
					sF=sFH.substring(0,2) + sEndSepF + sFH.substring(2,4) + sEndSepF + sFH.substring(4,8);
					iFechaLength = 8;
				}
			}
			else
			{
				var vTokens =	sFH.split(sEndSepF);
				bHayErr = ! (vTokens.length >= 3);
				
				//Debemos comprobar que se corresponde, como minimo, con un array de tres elementos
				if (! bHayErr)
				{
					sF=vTokens[0] + sEndSepF + vTokens[1] + sEndSepF + vTokens[2].substring(0,4);
					iFechaLength =	8 + 2 *	sEndSepF.length;
				}
			}
			
			//Obtenemos la hora en base al offset acumulado	de la fecha
			
			if (! bHayErr)
			{
				//Extraemos la	parte <sepFechaHora><Fecha>
				sFH = sFH.substring(iFechaLength+sEndSepFH.length,sFH.length);
				
				//Comprobamos lo que nos llega	un separador de	Hora
				if (sEndSepH.length)
				{
					bHayErr=! (sFH.length >= 6);
					
					if (! bHayErr)
					{
						sEndSepH = sSepHora;
						sH = sFH.substring(0,2) + sEndSepH + sFH.substring(2,4) + sEndSepH + sFH.substring(4,6);
					}
				}
				else
				{
					var	vTokens = sFH.split(sEndSepH);
					bHayErr=!(vTokens.length >= 3);
					
					//Debemos comprobar	que se corresponde, como minimo, con un	array de tres elementos
					if (! bHayErr) 
						sH=vTokens[0] + sEndSepH + vTokens[1] + sEndSepH + vTokens[2];
				}
			}
			
			//Comprobamos que, una vez separados tanto la fecha como la hora, no se ha producido ningun error
			if (! bHayErr) 
				bRet= (isDtTime(sH)=="") && (isDtDate(sF)=="");
		}
	}
	
	if (bRet)
		return new ParqMensaje("",null);
	else
	{
		return new ParqMsgFormatoIncorrecto(parNombre, "fecha larga", buf);
	}
}
oARQDFuncValidacion[getFnIndex(dtDateTime)]=isDtDateTime;

function ARQValidaciones()
{
	if (!bValidar)
		return true;
	else
	{
		var bRet= (validaDatos(document.forms["formDatos"]).length == 0);
		if (!bRet)
			bARQDRequestedSubmit= false;
		return bRet;
	}
}

function marcarCampoError(parCampo)
{
	var tip= typeof(parCampo);
	if (tip.toLowerCase() == "string")
	{
		if (document.getElementById(parCampo) != null)
			parCampo= document.getElementById(parCampo);
		else
		{
			if (document.getElementsByName(parCampo).length > 0)
				parCampo= document.getElementsByName(parCampo)[0];
			else
				return;
		}
	}
	if (parCampo.id)
	{	
			if (parCampo.className.indexOf("error") != 0)
			{
				parCampo.className= "error" + parCampo.className;				
			}

	} 

}

function desmarcarCampoError(parCampo)
{
	var tip= typeof(parCampo);
	if (tip.toLowerCase() == "string")
	{
		if (document.getElementById(parCampo) != null)
			parCampo= document.getElementById(parCampo);
		else
		{
			if (document.getElementsByName(parCampo).length > 0)
				parCampo= document.getElementsByName(parCampo)[0];
			else
				return;
		}
	}
	if (parCampo.id)
	{
		var oCampo= document.getElementById(parCampo.id);
		if (oCampo)
			if (oCampo.className.indexOf("error") == 0)
				oCampo.className= oCampo.className.substring("error".length, oCampo.className.length);
	}
}

//*************************************************************************
//* isDtRadioButton: Funcion encargada de comprobar si se ha seleccionado un radiobutton dentro de un grupo de ostos *
//*					  
//* @param oValidacion: objeto oValida que contiene la informacion necesaria para realizar la validacion *  						  		*
//* @return Si se ha seleccionado un radiobutton devuelve un objeto ParqMensaje vacoo   *
//*			En caso contrario devuelve un mensaje de error.               				  *
//*************************************************************************	
function isDtRadioButton(oValidacion)
{
	var radioButtonName = oValidacion.nombre;
	var listaRB = document.getElementsByName(radioButtonName);	
	var seleccionado = false;
	
	for(var i=0; i<listaRB.length; i++) {
		if(listaRB[i].checked) {
			seleccionado=true;
		}
	}
	
	if(seleccionado == true) {
		return new ParqMensaje();
	} else {
		return new ParqMsgSeleccionObl(oValidacion.concepto);
	}
}
oARQDFuncValidacion[getFnIndex(dtRadioButton)]=isDtRadioButton;


//*************************************************************************************************//


function showHideFieldSet(obj){//obj===legend
	// Modifica la propiedad de estilo "display" del siguiente 
	// elmento (del mismo nivel) a "legend" segun corresponda.
	var nsibs = obj.nextSibling.style;
	if(nsibs.display=="none"){
		nsibs.display="";
	}else{
		nsibs.display="none";
	}
}		
				
function FieldView(_req, _type, _id, _title, _min_size, _max_size){

	this.required=_req;
	this.type=_type;
	this.id=_id;
	this.title=_title;
	this.min_size=_min_size;
	this.max_size=_max_size;
	this.elmnt=null;
	this.value="";
}

FieldView.prototype.validate=function(){
	
	// itero todos los elementos del array (this), objeto dfieldViewC
	var result=null;
	//alert("validating element... " + this.id);
	
	if(this.elmnt==null)this.elmnt=document.getElementsByName(this.id)[0];
	
	this.value = this.elmnt.value;
	
	//alert("tipo: " + this.type);
	//alert("min_size: " + this.min_size);
	//alert("max_size: " + this.max_size);
	//alert("required? " + this.required);
	
	if(this.required==true && this.value == ""){

		result = new ParqMsgObligatorio(this.title);
		
	}else if (this.value != "") {
		
		//alert("valor: " + this.value);
		
		if (this.value.length < this.min_size){

			result = new ParqMsgErrorLongMinima(this.title, this.min_size, this.value);

		}else if (this.value.length > this.max_size){
					
			result = new ParqMsgErrorLongMaxima(this.title, this.max_size, this.value);
			
		}else{// comprobamos el tipo y parseamos
			if (this.type == "date"){

				result = isDtDate(this.value, this.title);
								
			}else if (this.type == "int" || this.type == "integer" || this.type == "biginteger" 
						|| this.type == "short" 
							|| this.type == "long"
								|| this.type == "byte"){

				result = isDtInteger(this.value, this.title, false/*prOnlyPositive*/);
				
			}else if (this.type == "boolean"){

				var valueR = parseInt(this.value);
				if (isNaN(valueR) || (valueR != 0 && valueR != 1)){
					result = ParqMsgDatoIncorrecto(this.title, this.value);
				}
									
			}else if (this.type == "decimal" || this.type == "double"){

				result = isDtMonedaEuro(this.value,this.title);
			}
		}
	}
	
	if(result!=null && result.codigo!=''){
		return result;				
	}
	//alert(this.value + ": tipo correcto");
	return null;
}

function FieldViewCollection(_fields){
	this.fields=[];
	if(_fields!=null && _fields.length>0){
		for(var i=_fields.length-1;i>=0;i--){
			_field = _fields[i];
			if(_field instanceof FieldView){
				this.fields.push(_field);
			}else{
				this.fields=[];
				return false;
			}
		}
	}
	this.size = this.fields.length;
}

FieldViewCollection.prototype.add=function(_field){
	//alert('function prototype.add invoked');
	if(_field instanceof FieldView){
		//alert('instancia de FieldView');
		this.fields.push(_field);
		this.size++;
	}
}

FieldViewCollection.prototype.remove=function(_index){
	if(_index>-1 && _index<this.fields.length){
		this.fields.splice(_index,1);
		this.size--;
	}
}
		
FieldViewCollection.prototype.validate=function(event_){
	//alert("validando evento..." + event_);
	var miDiv = document.getElementById('javascriptErrors');
	miDiv.style.display = 'none';
	miDiv.innerHTML = '';
		
	if (this.size == 0){
		//alert("this.size es cero");
		return new ParqMensaje("",null);
	}
	//alert("recorre...o");
	var msgArray = [];
	msgArray.codigo=null;
	for(var i=this.size-1;i>=0;i--){
		msg = this.fields[i].validate();
		var el = document.getElementById(this.fields[i].id);
		if(msg != null){
			msgArray.push(msg);			
			el.style.background= '#FFFFED';
			el.style.color= '#EB7310';
		}else{
			el.style.background= 'white';
			el.style.color= 'black';
		}
	}
	
	if (msgArray.length == 0){
		replaceEvent(event_, 0);
		return true;
	}else{
		// hacemos visible el orea inferior de mensajes
		var miDiv = document.getElementById('javascriptErrors');
		miDiv.style.display = 'block';
		for (var m=0;m<msgArray.length;m++){
			var msg = msgArray[m];
			var sMsg = '';
			for (var i=0;i < msg.params.length; i++){
				var parqMsgParam = msg.params[i];
				if (i==0){
					sMsg+= 'campo [<b>' + parqMsgParam.valor+  '</b>] ==> ';
					if (msg.params.length == 1){
						sMsg+= '<i>' + parqMsgParam.nombre + ' obligatorio</i>';
					}
				}else{
					sMsg+= parqMsgParam.nombre + ': <i>' + parqMsgParam.valor + '</i>';
					if (i < msg.params.length-1){
						sMsg+= ', ';						
					}
					//alert(sMsg);
				}
			}
			miDiv.innerHTML = miDiv.innerHTML + '<span><label style="background-color: #FFFFED; color: #EB7310;">' + sMsg + '</label></span><br/>';
		}
		return false;
	}
}

function replaceEvent(newEv, increm){ 
   //alert("replaceEvent invoked!");
   if (increm != 0){
	   // alert("increment vale: " + increm);
	 document.forms[0].pageClicked.value = increm;
   }
   if (newEv.indexOf('.', 0) == -1){
	   //alert('primer if..');
	   var ev_ = document.forms[0].event.value;
	   var ind_ = ev_.indexOf('.', 0);
	   var ev2_ = ev_.substring(0, ind_+1) + newEv;
	   document.forms[0].event.value= ev2_;
    }else {
    	//alert('else..');
	   document.forms[0].event.value= newEv;
	}
}



