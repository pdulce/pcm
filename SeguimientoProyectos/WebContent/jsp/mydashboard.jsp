<%@page import="java.util.Enumeration"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
    
    
    <form class="pcmForm" action="prjManager" method="POST">
    
    	<input id="exec" name="exec" type="hidden" value="dashboard"/>
		<input id="event" name="event" type="hidden" value="dashboard"/>
		<input id="entities" name="entities" type="hidden" value="resumenEntregas$resumenPeticiones"/>
    
    <fieldset class="collapsible"><legend>Filtrar por criterios generales</legend>	
    	<div>
    		<label class="infoCls" title="Entorno" id="aplicativo.id_tecnologiaLABEL" for="aplicativo.id_tecnologia">
    		&nbsp;&nbsp;Entorno&nbsp;
    		</label>
			<select class="textInput" id="aplicativo.id_tecnologia" name="aplicativo.id_tecnologia"
				onChange="javascript:document.forms[0].submit();return true;" size="7" multiple>
					<option value="aplicativo.id_tecnologia=1" id="aplicativo.id_tecnologia=1">HOST</option>
					<option value="aplicativo.id_tecnologia=2" id="aplicativo.id_tecnologia=2">Pros@</option>
					<option value="aplicativo.id_tecnologia=3" id="aplicativo.id_tecnologia=3">Web Services</option>
					<option value="aplicativo.id_tecnologia=4" id="aplicativo.id_tecnologia=4">Servicios API Rest</option>
					<option value="aplicativo.id_tecnologia=5" id="aplicativo.id_tecnologia=5">Mobile Tech</option>
			</select>

   			<label class="infoCls"  title="Servicio" id="aplicativo.id_servicioLABEL" for="aplicativo.id_servicio">
			&nbsp;&nbsp;Servicio&nbsp;
			</label>
			<select class="textInput" size="3" id="aplicativo.id_servicio" name="aplicativo.id_servicio" 
				onChange="javascript:document.forms[0].submit();return true;"
				 multiple>
				<option value="aplicativo.id_servicio=1"  id="aplicativo.id_servicio=1">Servicio Mto. HOST</option>
				<option value="aplicativo.id_servicio=2"  id="aplicativo.id_servicio=2">Servicio Nuevos Desarrollos Pros@</option>
				<option value="aplicativo.id_servicio=3"  id="aplicativo.id_servicio=3">Servicio Mto. Pros@</option>
			</select>

			<label class="infoCls"  title="Aplicativo" id="aplicativo.idLABEL" for="aplicativo.id">
			&nbsp;&nbsp;Aplicativo&nbsp;</label>
			<select class="textInput" size="7" id="aplicativo.id" name="aplicativo.id"
				onChange="javascript:document.forms[0].submit();return true;" 
				multiple>
				<option value="aplicativo.id=1"  id="aplicativo.id=1">AFLO</option>
				<option value="aplicativo.id=2"  id="aplicativo.id=2">APRO</option>
				<option value="aplicativo.id=3"  id="aplicativo.id=3">AYFL</option>
				<option value="aplicativo.id=4"  id="aplicativo.id=4">BISM</option>
				<option value="aplicativo.id=5"  id="aplicativo.id=5">CMAR</option>
				<option value="aplicativo.id=6"  id="aplicativo.id=6">CONT</option>
				<option value="aplicativo.id=7"  id="aplicativo.id=7">FAM2</option>
				<option value="aplicativo.id=8"  id="aplicativo.id=8">FAMA</option>
				<option value="aplicativo.id=9"  id="aplicativo.id=9">FARM</option>
				<option value="aplicativo.id=10"  id="aplicativo.id=10">FMAR</option>
				<option value="aplicativo.id=11"  id="aplicativo.id=11">FOM2</option>
				<option value="aplicativo.id=12"  id="aplicativo.id=12">FOMA</option>
				<option value="aplicativo.id=13"  id="aplicativo.id=13">FRMA</option>
				<option value="aplicativo.id=14"  id="aplicativo.id=14">GFOA</option>
				<option value="aplicativo.id=15"  id="aplicativo.id=15">IMAG</option>
				<option value="aplicativo.id=16"  id="aplicativo.id=16">INBU</option>
				<option value="aplicativo.id=17"  id="aplicativo.id=17">INCA</option>
				<option value="aplicativo.id=18"  id="aplicativo.id=18">INVE</option>
				<option value="aplicativo.id=19"  id="aplicativo.id=19">ISMW</option>
				<option value="aplicativo.id=20"  id="aplicativo.id=20">MEJP</option>
				<option value="aplicativo.id=21"  id="aplicativo.id=21">MGEN</option>
				<option value="aplicativo.id=22"  id="aplicativo.id=22">MIND</option>
				<option value="aplicativo.id=23"  id="aplicativo.id=23">MOVI</option>
				<option value="aplicativo.id=24"  id="aplicativo.id=24">OBIS</option>
				<option value="aplicativo.id=25"  id="aplicativo.id=25">PAGO</option>
				<option value="aplicativo.id=26"  id="aplicativo.id=26">PRES</option>
				<option value="aplicativo.id=27"  id="aplicativo.id=27">SANI</option>
				<option value="aplicativo.id=28"  id="aplicativo.id=28">SBOT</option>
				<option value="aplicativo.id=29"  id="aplicativo.id=29">SIEB</option>
				<option value="aplicativo.id=30"  id="aplicativo.id=30">TASA</option>
				<option value="aplicativo.id=31"  id="aplicativo.id=31">TISM</option>
				<option value="aplicativo.id=32"  id="aplicativo.id=32">WBOF</option>
				<option value="aplicativo.id=33"  id="aplicativo.id=33">WISM</option>
				<option value="aplicativo.id=34"  id="aplicativo.id=34">WSAO</option>
				<option value="aplicativo.id=35"  id="aplicativo.id=35">WSCR</option>
				<option value="aplicativo.id=36"  id="aplicativo.id=36">WSPX</option>
				<option value="aplicativo.id=37"  id="aplicativo.id=37">WSRT</option>
			</select>
			
			
			<div>
				<br>
				<label class="infoCls"  title="Escala">	&nbsp;&nbsp;Escala&nbsp;</label>
				<label class="radiogroupcontainer">&nbsp;Diario&nbsp;
					<input onChange="javascript:document.forms[0].submit();return true;" 
							type="radio" class="checkmarkradio" value="dayly" id="escaladodayly" name="escalado">
					<span class="checkmarkradio"></span>
				</label>
				<label class="radiogroupcontainer">&nbsp;Semanal&nbsp;
					<input onChange="javascript:document.forms[0].submit();return true;" 
							type="radio" class="checkmarkradio" value="weekly" id="escaladoweekly" name="escalado">
					<span class="checkmarkradio"></span>
				</label>
				<label class="radiogroupcontainer">&nbsp;Mensual&nbsp;
					<input onChange="javascript:document.forms[0].submit();return true;" 
							type="radio" class="checkmarkradio" value="monthly" id="escaladomonthly" name="escalado">
					<span class="checkmarkradio"></span>
				</label>
				<label class="radiogroupcontainer">&nbsp;Trimestral&nbsp;
					<input onChange="javascript:document.forms[0].submit();return true;" 
							type="radio" class="checkmarkradio" value="3monthly" id="escalado3monthly" name="escalado">
					<span class="checkmarkradio"></span>
				</label>
				<label class="radiogroupcontainer">&nbsp;Semestral&nbsp;
					<input onChange="javascript:document.forms[0].submit();return true;" 
							type="radio" class="checkmarkradio" value="6monthly" id="escalado6monthly" name="escalado">
					<span class="checkmarkradio"></span>
				</label>
				<label class="radiogroupcontainer">&nbsp;Anual&nbsp;
					<input onChange="javascript:document.forms[0].submit();return true;" 
							type="radio" class="checkmarkradio" value="anualy" id="escaladoanualy" name="escalado">
					<span class="checkmarkradio"></span>
				</label>
				<label class="radiogroupcontainer">&nbsp;Autom&aacute;tico&nbsp;
					<input onChange="javascript:document.forms[0].submit();return true;" 
							type="radio" class="checkmarkradio" value="automatic" id="escaladoautomatic" name="escalado">
					<span class="checkmarkradio"></span>
				</label>

				<label class="infoCls" title="Operación de agregación">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Operaci&oacute;n de agregaci&oacute;n&nbsp;</label>
				<label class="radiogroupcontainer">&nbsp;Promediar&nbsp;
					<input onChange="javascript:document.forms[0].submit();return true;" type="radio" class="checkmarkradio" value="AVG" id="operationAVG" name="operation">
					<span class="checkmarkradio"></span>
				</label>
				<label class="radiogroupcontainer">Totalizar&nbsp;
					<input onChange="javascript:document.forms[0].submit();return true;" type="radio" class="checkmarkradio" value="SUM" id="operationSUM" name="operation">
					<span class="checkmarkradio"></span>
				</label>
			</div>

			</div>
			
		</fieldset>
    </form>
    
	<table>
		<tr>
			<td>
				<jsp:include page="${containerJSP_11}">
					<jsp:param name="idseries" value="_serie01" />
					<jsp:param name="width" value="740px" />
					<jsp:param name="height" value="480px" />
				</jsp:include>
			</td>
			<td>
				<jsp:include page="${containerJSP_120}">
					<jsp:param name="idseries" value="_serie021" />
					<jsp:param name="width" value="500px" />
					<jsp:param name="height" value="320px" />
				</jsp:include>
				<jsp:include page="${containerJSP_121}">
					<jsp:param name="idseries" value="_serie022" />
					<jsp:param name="width" value="500px" />
					<jsp:param name="height" value="160px" />
				</jsp:include>
			</td>
		</tr>	
		<tr>
			<td>
				<jsp:include page="${containerJSP_21}">
					<jsp:param name="idseries" value="_serie03" />
					<jsp:param name="width" value="640px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
			<td>
				<jsp:include page="${containerJSP_22}">
					<jsp:param name="idseries" value="_serie04" />
					<jsp:param name="width" value="600px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
		</tr>	
		<tr>
			<td>
				<jsp:include page="${containerJSP_31}">
					<jsp:param name="idseries" value="_serie05" />
					<jsp:param name="width" value="640px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
			<td>
				<jsp:include page="${containerJSP_32}">
					<jsp:param name="idseries" value="_serie06" />
					<jsp:param name="width" value="600px" />
					<jsp:param name="height" value="360px" />
				</jsp:include>
			</td>
		</tr>	
		<tr>
			<td>
				<jsp:include page="${containerJSP_41}">
					<jsp:param name="idseries" value="_serie07" />
					<jsp:param name="width" value="640px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
			<td>
				<jsp:include page="${containerJSP_42}">
					<jsp:param name="idseries" value="_serie08" />
					<jsp:param name="width" value="600px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
		</tr>
	</table>
	
	<script type="text/javascript">
	
	<% 	
	Object escaladoParamValue = request.getAttribute("escalado");
	if (escaladoParamValue != null){
		String[] as= (String[]) escaladoParamValue;
		for (int i=0;i<as.length;i++){
			//System.out.println("value selected:" + as[i]);
			if ("dayly".contentEquals(as[i])){%>
			   	document.getElementById('escaladodayly').checked= 'checked';
		  <%}else if ("weekly".contentEquals(as[i])){%>			  
		   		document.getElementById('escaladoweekly').checked= 'checked';
		  <%}else if ("monthly".contentEquals(as[i])){%>			  
		   		document.getElementById('escaladomonthly').checked= 'checked';
		  <%}else if ("3monthly".contentEquals(as[i])){%>			  
		   		document.getElementById('escalado3monthly').checked= 'checked';
		  <%}else if ("6monthly".contentEquals(as[i])){%>			  
		   		document.getElementById('escalado6monthly').checked= 'checked';
		  <%}else if ("anualy".contentEquals(as[i])){%>			  
		   		document.getElementById('escaladoanualy').checked= 'checked';	
		  <%}else if ("automatic".contentEquals(as[i])){%>			  
		   		document.getElementById('escaladoautomatic').checked= 'checked';	
		  <%}%>	
		<%}//for values
	}else{ %>
		  	document.getElementById('escaladomonthly').checked= 'checked';		  
  <%} %>
  
  <% 	
	Object operationParamValue = request.getAttribute("operation");
	if (operationParamValue != null){
		String[] as= (String[]) operationParamValue;
		for (int i=0;i<as.length;i++){
			//System.out.println("value selected:" + as[i]);
			if ("AVG".contentEquals(as[i])){%>
			   	document.getElementById('operationAVG').checked= 'checked';
		  <%}else if ("SUM".contentEquals(as[i])){%>			  
		   		document.getElementById('operationSUM').checked= 'checked';
		  <%}%>	
		<%}//for values
	}else{ %>
		  	document.getElementById('operationAVG').checked= 'checked';  
  <%} %>
	
	<% 	
	Object entornoParamValues = request.getAttribute("aplicativo.id_tecnologia");
	if (entornoParamValues != null){
		String[] as= (String[]) entornoParamValues;
		for (int i=0;i<as.length;i++){
			int selectedValue = (Integer.valueOf(as[i].split("=")[1]) - 1);
			//System.out.println("value selected:" + selectedValue);
		%>			
			document.getElementById('aplicativo.id_tecnologia').options[<%=selectedValue%>].selected= 'selected';
		<%}//for values
	}else{//if %>
		   for (var j=0;j<5;j++){
		  	document.getElementById('aplicativo.id_tecnologia').options[j].selected = 'selected';
		   }
  <%} %>
  
  <% 	
	Object servicioParamValues = request.getAttribute("aplicativo.id_servicio");
	if (servicioParamValues != null){
		String[] as= (String[]) servicioParamValues;
		for (int i=0;i<as.length;i++){
			int selectedValue = (Integer.valueOf(as[i].split("=")[1]) - 1);
			//System.out.println("value selected:" + selectedValue);
		%>			
			document.getElementById('aplicativo.id_servicio').options[<%=selectedValue%>].selected= 'selected';
		<%}%>				
	<%}else{//if %>
		   for (var j=0;j<3;j++){
		  	document.getElementById('aplicativo.id_servicio').options[j].selected = '';
		   }
  <%} %>

  	
	<% 	
	Object aplicativoParamValues = request.getAttribute("aplicativo.id");
	if (aplicativoParamValues != null){
		String[] as= (String[]) aplicativoParamValues;
		for (int i=0;i<as.length;i++){
			int selectedValue = (Integer.valueOf(as[i].split("=")[1]) - 1);
			//System.out.println("value selected:" + selectedValue);
		%>			
			document.getElementById('aplicativo.id').options[<%=selectedValue%>].selected= 'selected';
		<%}%>
		
	<%}else{//if %>
		   for (var j=0;j<37;j++){
		  	document.getElementById('aplicativo.id').options[j].selected = 'selected';
		   }
	<%} %>
	
	
	  </script>
	
	
