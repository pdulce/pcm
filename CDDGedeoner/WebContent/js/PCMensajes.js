
var MSG_CAMPOOBLIGATORIO=        "3";
var MSG_FORMATOINCORRECTO=       "6";
var MSG_ERRORLONGMINIMA=         "4";
var MSG_ERRORLONGMAXIMA=         "8";
var MSG_DATONOESPECIFICADO=     "18";
var MSG_NUMPARAMSINCORRECTO=    "19";
var MSG_DOCUMENTOINCOMPLETO=    "20";
var MSG_ERRORCARGAAYUDAS=       "21";
var MSG_ERRORANNADIRTIPOSDATOS= "22";
var MSG_FUNCIONNOEXISTE=        "23";
var MSG_TIPODATONODEFINIDO=     "24";
var MSG_MANEJADORYADEFINIDO=    "25";
var MSG_ACCIONNOPERMITIDA=      "26";
var MSG_ELEMENTOSINFORMATO=     "27";
var MSG_FUNCVALIDNODEFINIDA=    "28";
var MSG_TIPODATONOPERMITIDO=    "29";
var MSG_TAMANOGETEXCESIVO=      "30";
var MSG_AVISO=                  "31";
var MSG_ERROR=                  "32";
var MSG_ERRORJS=                "33";
var MSG_FORMATOINCORRECTOFECHA= "37";


var ParqMensajesJS= true;
var sARQDidXMLMsg = "idXMLMsg";
var sDominio = "SPM.DOMAIN"
var oARQDxmlElemMsgAux= new Array();
var	oARQDficheroXMLMsgAux= new Array();
var iARQDMsgCnt= 0;
var iMsgTipoBase = 1;
var iRetMsgTipoBase = 1;

/******** Variables PUBLICAS ********/
var iMsgTipoCerrar		= iMsgTipoBase;
var iMsgTipoSiNo		= iMsgTipoBase + 1;
var iMsgTipoACancel	    = iMsgTipoBase + 2;
var iRetMsgTipoCerrar	= iRetMsgTipoBase;
var iRetMsgTipoSi		= iRetMsgTipoBase + 1;
var iRetMsgTipoNo		= iRetMsgTipoBase + 2;
var iRetMsgTipoAceptar	= iRetMsgTipoBase + 3;
var iRetMsgTipoCancelar	= iRetMsgTipoBase + 4;
var iMsgRetValue = iRetMsgTipoCerrar;

function ParqMsgParam(nombre, valor) 
{          
	if (nombre==null) 
		nombre= "";
	this.nombre = nombre;

	if (valor==null) 
	{
		valor= "";
	}
	else if (typeof(valor) == "object") // PARQMENSAJE, LO RESOLVEMOS
	{
		if (valor.codigo != "")
			valor = getMensaje(valor.codigo, valor.dominio);
		else
			valor = ""; 
	}		
		
	this.valor= valor;
}

function ParqMensaje(codigo, params, dominio)
{   
	if (codigo==null) 
		codigo= "";
	this.codigo = codigo;
	
	this.dominio = dominio; 
	
	if (params==null)
		params= new Array();
	this.params= params;
	
}


function ParqFicheroXML(domain, XML)
{
	if (domain == null || domain == "" || domain == "undefined")
		domain = metaTags('SPM.DOMAIN').content;
	
	this.domain = domain;
	this.XML = XML;
}
/****** FIN Variables PUBLICAS ******/

function IncluirFicheroMensajes(xmlFichero, domain)
{
	var xmlDOC;
	var xmlFicheroReal= xmlFichero +"_"+ gIdioma+ ".xml"
	if (document.implementation && document.implementation.createDocument)
		xmlDOC = document.implementation.createDocument("", "doc", null);  //Mozilla
	else if (window.ActiveXObject) //IE
	{
		xmlDOC = new ActiveXObject("Microsoft.XMLDOM");
		xmlDOC.setProperty("ForcedResync", false);
	}
	else
	{
		alert('El navegador no soporta XML.');
		return;    
	}
	xmlDOC.async = false;
	xmlDOC.load(xmlFicheroReal);
	
	oObjetoFichero = new ParqFicheroXML(domain, xmlDOC);
	oARQDxmlElemMsgAux[oARQDficheroXMLMsgAux.length]= oObjetoFichero;
	oARQDficheroXMLMsgAux[oARQDficheroXMLMsgAux.length]= xmlFicheroReal;
}


//*************************************************************************	
//* Devuelve el texto del mensaje especificado							  * 
//*************************************************************************
function getMensaje(msCodigo, domain)
{
	if (domain == null || domain == "" || domain == "undefined")
		domain = metaTags('SPM.DOMAIN').content;

	var sMsg= getMensajeAD(msCodigo, domain);
	
	if (!sMsg)    
		sMsg= "Mensaje inexistente para [" + msCodigo + "] y dominio [" + domain + "]";
	else
	{
		for (var i=2;i < arguments.length; i+=2)
		{ 
			posIni= sMsg.indexOf("{"+arguments[i]+"}", 0);    	       	    
			if (posIni == -1) continue;
				posFin= sMsg.indexOf("}", posIni);
			if ( posIni != -1 && posFin > posIni)
				sMsg= sMsg.substring(0, posIni) + arguments[i+1] + sMsg.substring(posFin+1);			
		}
	}
	
	return sMsg;
}


function getMensajeARQD(msCodigo)
{
	var args= "";
	for (var k=1; k< arguments.length; k++)
	      args+= " ,'"+arguments[k]+"'";
	return eval("getMensaje("+msCodigo+", 'ARQ'" + args + ")");
}

function getMensajeAD(msCodigo, domain)
{ 
	var item= null;
	// BUSCAMOS EL FICHERO DE SU DOMINIO
	for (var k= 0; ( (k< oARQDficheroXMLMsgAux.length) && (item==null) ); k++)
	{
		if (!tablaExiste(oARQDxmlElemMsgAux[k]))
			return "Error en carga de Mensajes. No existe el fichero: " + oARQDficheroXMLMsgAux[k];

		if (oARQDxmlElemMsgAux[k].domain == domain)		
		{
			item= obtenerMsg(oARQDxmlElemMsgAux[k].XML, msCodigo);  // item = oARQDxmlElemMsgAux[k].selectSingleNode("//mensaje[@id='"+msCodigo+"']");
		}
	}
	return item;
}

function obtenerMsg(oFich, sID)
{
	var oMensajes = oFich.getElementsByTagName("Mensajes")[0].getElementsByTagName("mensaje");

	for (var k=0; k<oMensajes.length; k++)
	{
		if (oMensajes[k].getAttribute("id") == sID)
			return trim(oMensajes[k].firstChild.nodeValue);
	}
	return null;
}

//*************************************************************************
//* showMessageCod: Muestra un mensaje a travos de showMessage, pero 
//*         indicando el codigo del mensaje, en vez de su texto.
//* @param sCodMensaje: codigo del mensaje
//* @param iTipo: Tipo de botones a presentar. Posibles valores:
//*         iMsgTipoCerrar, iMsgTipoSiNo, iMsgTipoACancel
//* @param sTitulo: texto del totulo
//* @param sEncabezado: texto del encabezado
//* A continuacion del cuarto parometro se pueden indicar los siguientes:
//* @param func1 (opcional): funcion javascript asociada al primer boton
//* @param func2 (opcional): funcion javascript asociada al segundo boton
//* @param lista pares codigo-valor para los parometros del mensaje 
//*         (primer parometro), seguidos unos de otros.
//* Ejemplo: showMessageCod('cod01', iMsgTipoSiNo, 'totulo', 'encabezado', 'funcSi()', 'funcNo()', par1, val1, par2, val2);
//*************************************************************************	
function showMessageCod(sCodMensaje, sDominio, iTipo, sTitulo, sEncabezado)
{
	var args= "";
	for (var k=6; k< arguments.length; k++)
		args+= ",'"+arguments[k]+"'";
	showMessage(eval("getMensaje('"+sCodMensaje+"','"+sDominio+"'"+args+")"), iTipo, sTitulo, sEncabezado, arguments[4], arguments[5]);
}

function showMessage(sMensaje, iTipo, sTitulo, sEncabezado)
{
	if (sMensaje!=null)
	{
		alert(sMensaje);
	}
}

//*************************************************************************
//* tablaExiste: funcion que indica si una tabla se ha cargado			  *
//* @param tablaA: elemento del documento con los datos de la tabla		  *
//* @return boolean: verdadero o falso segun exista esa tabla o no		  *
//*************************************************************************	
function tablaExiste(tablaA)
{
	return true;
	//return (tablaA.parseError.errorCode == 0);
}

//* ParqMsgObligatorio: Construye el mensaje "{DATO} obligatorio/a."
//* @param dato: ParqMensaje o String con el valor del parometro {DATO}
function ParqMsgObligatorio(dato)
{
	var param = new ParqMsgParam("DATO", dato);

	return new ParqMensaje(MSG_CAMPOOBLIGATORIO, new Array(param), 'ARQ');
}

//*	ParqMsgSeleccionObl: Construye el mensaje "Debe seleccionar un/a {DATO}."
//* @param	oMensaje: String o ParqMensaje con el valor del parometro {DATO}
function ParqMsgSeleccionObl(oMensaje){
	
	var param = new ParqMsgParam("DATO", oMensaje);

	return new ParqMensaje(MSG_SELECCIONOBLIGATORIA, new Array(param), 'ARQ');
}

//* ParqMsgErrorLongMinima:	Construye el mensaje "La informacion {DATO} tiene una longitud menor de la esperada: {LONG}. Valor introducido: {VALOR}".
//* @param oDato: String o ParqMensaje con el valor del parometro {DATO}
//* @param long: Valor de la longitud monima.
//* @sValor: Valor que toma el dato.
function ParqMsgErrorLongMinima(oDato, longitud, sValor){
	
	var param1 = new ParqMsgParam("DATO", oDato);
	var param2 = new ParqMsgParam("LONG", longitud);
	var param3 = new ParqMsgParam("VALOR", sValor);
	
	var listaParam = new Array(param1, param2, param3);
	
	return new ParqMensaje(MSG_ERRORLONGMINIMA, listaParam, 'ARQ');
}

//* ParqMsgFormatoIncorrecto: Construye el mensaje "La informacion {DATO} no tiene el formato esperado: {FORMATO}. Valor introducido: {VALOR}"
//* @param oDato: String o ParqMensaje con el valor del parometro {DATO}
//* @param sTipoFormato: Tipo de formato que deberoa cumplir el dato.
//* @param sValor: Valor que toma el dato.
function ParqMsgFormatoIncorrecto(oDato, sTipoFormato, sValor){

	var param1 = new ParqMsgParam("DATO", oDato);
	var param2 = new ParqMsgParam("FORMATO", sTipoFormato);
	var param3 = new ParqMsgParam("VALOR", sValor);
	
	var listaParam = new Array (param1, param2, param3);

	return new ParqMensaje(MSG_FORMATOINCORRECTO, listaParam, 'ARQ');
	
}

//* ParqMsgElementoSinFormato:	Construye el mensaje "Elemento {ELEMENTO} no tiene definido formato."
//* @param oElemento: String o ParqMensaje con el valor del parometro {ELEMENTO}
function ParqMsgElementoSinFormato(oElemento){
	var param = new ParqMsgParam("ELEMENTO", oElemento);
	
	return new ParqMensaje(MSG_ELEMENTOSINFORMATO, new Array(param), 'ARQ'); //27
}

//* ParqMsgTipoDatoNoPermitido: Construye el mensaje "Tipo de Dato {TIPODATO} no permitido o no definido."
//*	@param oTipoDato: String o ParqMensaje con  el valor del parometro {TIPODATO}
function  ParqMsgTipoDatoNoPermitido(oTipoDato){	
	var param = new ParqMsgParam("TIPODATO", oTipoDato);
	
	return new ParqMensaje(MSG_TIPODATONOPERMITIDO, new Array(param), 'ARQ');//29
}

//*	ParqMsgFormatoFechaIncorrecto: Construye el mensaje "La informacion {DATO} no es una fecha volida o no tiene el formato esperado: {FORMATO}. Valor introducido: {VALOR}"
//* @param oDato: String o ParqMensaje con el valor del parometro {DATO}
//* @param sFormato: Representa el tipo de formato que deberoa cumplir la fecha.
//* @param sValor: Valor que toma el dato.
function ParqMsgFormatoFechaIncorrecto (oDato, sFormato, sValor){
	var param1 = new ParqMsgParam ("DATO", oDato);
	var param2 = new ParqMsgParam("FORMATO", sFormato);
	var param3 = new ParqMsgParam("VALOR", sValor);
	
	var listaParam = new Array (param1, param2, param3);
	
	return new ParqMensaje(MSG_FORMATOINCORRECTOFECHA, listaParam, 'ARQ');//37
}

//* ParqMsgFuncValidNoDefinida:	Construye el mensaje "Funcion de validacion no definida o con codigo incorrecto: {FUNCION}"
//* @param oFuncion: ParqMensaje o String con el nombre de la funcion.
function ParqMsgFuncValidNoDefinida(oFuncion){
	var param = new ParqMsgParam("FUNCION", oFuncion);

	return new ParqMensaje(MSG_FUNCIONNOEXISTE, new Array(param), 'ARQ');//23
}

//* ParqMsgErrorLongMaxima:	Construye el mensaje "La informacion {DATO} tiene una longitud mayor de la esperada: {LONG}. Valor introducido: {VALOR}".
//* @param oDato: ParqMensaje o String con el valor del parometro {DATO}.
//* @param long: valor de la longitud moxima.
//* @param sValor: valor que toma el dato.
function ParqMsgErrorLongMaxima(oDato, longitud, sValor){
	
	var param1 = new ParqMsgParam("DATO", oDato);
	var param2 = new ParqMsgParam("LONG", longitud);
	var param3 = new ParqMsgParam("VALOR", sValor);
	
	var listaParam = new Array(param1, param2, param3);
	
	return new ParqMensaje(MSG_ERRORLONGMAXIMA, listaParam, 'ARQ');//5
}

//* ParqMsgDatoIncorrecto: Construye el mensaje "{DATO} incorrecto/a. Valor introducido: {VALOR}"
//* @param oDato: ParqMensaje o String con el valor del parometro {DATO}
//* @param sValor: valor que toma el dato.
function ParqMsgDatoIncorrecto(oDato, sValor){
	var param1 = new ParqMsgParam("DATO", oDato);
	var param2 = new ParqMsgParam("VALOR", sValor);
	
	listaParam = new Array(param1, param2);

	return new ParqMensaje(MSG_DATOINCORRECTO, listaParam, 'ARQ');//7
}

//* ParqMsgDatoMenorMinimo: Construye el mensaje "La informacion {DATO} tiene un valor menor del esperado: {MINIMO}. Valor introducido: {VALOR}"
//* @param oDato: ParqMensaje o String con el valor del parometro {DATO}.
//* @param long: valor monimo.
//* @param sValor: valor que toma el dato.
function ParqMsgDatoMenorMinimo(oDato, sMinimo, sValor){
	var param1 = new ParqMsgParam("DATO", oDato);
	var param2 = new ParqMsgParam("MINIMO", sMinimo);
	var param3 = new ParqMsgParam("VALOR", sValor);
	
	var listaParam = new Array(param1, param2, param3);
	
	return new ParqMensaje(MSG_ERRORMENORMINIMO, listaParam, 'ARQ');//34
}

//* ParqMsgDatoMayorMaximo: Construye el mensaje "La informacion {DATO} tiene un valor mayor del esperado: {MAXIMO}. Valor introducido: {VALOR}"
//* @param oDato: ParqMensaje o String con el valor del parometro {DATO}.
//* @param long: valor moximo.
//* @param sValor: valor que toma el dato.
function ParqMsgDatoMayorMaximo(oDato, sMaximo, sValor){
	
	var param1 = new ParqMsgParam("DATO", oDato);
	var param2 = new ParqMsgParam("MAXIMO", sMaximo );
	var param3 = new ParqMsgParam("VALOR", sValor);
	
	var listaParam = new Array(param1, param2, param3);
	
	return new ParqMensaje(MSG_ERRORMAYORMAXIMO, listaParam, 'ARQ');//35
}