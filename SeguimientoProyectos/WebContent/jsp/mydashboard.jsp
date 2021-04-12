<%@page import="java.util.Enumeration"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
    
    
    <form action="prjManager" method="POST">
    	<input id="exec" name="exec" type="hidden" value="dashboard"/>
		<input id="event" name="event" type="hidden" value="dashboard"/>
		<input id="entities" name="entities" type="hidden" value="resumenEntregas$resumenPeticiones"/>
    
    <fieldset class="collapsible"><legend>Filtrar por criterios generales</legend>	
    	<div>
    		<label class="infoCls" title="Entorno" id="estudiosPeticiones.id_entornoLABEL" for="estudiosPeticiones.id_entorno">
    			&nbsp;&nbsp;Entorno&nbsp;
    		</label>
			<select class="textInput" id="estudiosEntregas.id_entorno" name="estudiosEntregas.id_entorno"
				onChange="javascript:vaciarOnSubmit('estudiosEntregas.id_servicio');vaciarOnSubmit('estudiosPeticiones.id_servicio');
						submitDashboard('estudiosEntregas.id_entorno','estudiosPeticiones.id_entorno');" size="7" multiple>
					<option value="estudiosEntregas.id_entorno=1" id="estudiosEntregas.id_entorno=1">HOST</option>
					<option value="estudiosEntregas.id_entorno=2" id="estudiosEntregas.id_entorno=2">Pros@</option>
					<option value="estudiosEntregas.id_entorno=3" id="estudiosEntregas.id_entorno=3">Web Services</option>
					<option value="estudiosEntregas.id_entorno=4" id="estudiosEntregas.id_entorno=4">Servicios API Rest</option>
					<option value="estudiosEntregas.id_entorno=5" id="estudiosEntregas.id_entorno=5">Mobile Tech</option>
				</select>
				<input id="estudiosPeticiones.id_entorno" name="estudiosPeticiones.id_entorno" type="hidden" value=""/>
	
    			<label class="infoCls"  title="Servicio" id="estudiosEntregas.id_servicioLABEL" for="estudiosEntregas.id_servicio">
				&nbsp;&nbsp;Servicio&nbsp;
				</label>
			<select class="textInput" size="3" id="estudiosEntregas.id_servicio" name="estudiosEntregas.id_servicio" 
				onChange="javascript:vaciarOnSubmit('estudiosEntregas.id_aplicativo');
						vaciarOnSubmit('estudiosPeticiones.id_aplicativo');
						submitDashboard('estudiosEntregas.id_servicio', 'estudiosPeticiones.id_servicio');"
				 multiple>
				<option value="estudiosEntregas.id_servicio=1"  id="estudiosEntregas.id_servicio=1">Servicio Mto. HOST</option>
				<option value="estudiosEntregas.id_servicio=2"  id="estudiosEntregas.id_servicio=2">Servicio Nuevos Desarrollos Pros@</option>
				<option value="estudiosEntregas.id_servicio=3"  id="estudiosEntregas.id_servicio=3">Servicio Mto. Pros@</option>
			</select>
			<input id="estudiosPeticiones.id_servicio" name="estudiosPeticiones.id_servicio" type="hidden" value=""/>

			<label class="infoCls"  title="Aplicativo" id="estudiosEntregas.id_aplicativoLABEL" for="estudiosEntregas.id_aplicativo">&nbsp;&nbsp;Aplicativo&nbsp;</label>
			<select class="textInput" size="7" id="estudiosEntregas.id_aplicativo" name="estudiosEntregas.id_aplicativo"
				onChange="javascript:vaciarOnSubmit('estudiosEntregas.id_servicio');vaciarOnSubmit('estudiosPeticiones.id_servicio');
						submitDashboard('estudiosEntregas.id_aplicativo', 'estudiosPeticiones.id_aplicativo');" 
				multiple>
				<option value="estudiosEntregas.id_aplicativo=1"  id="estudiosEntregas.id_aplicativo=1">AFLO</option>
				<option value="estudiosEntregas.id_aplicativo=2"  id="estudiosEntregas.id_aplicativo=2">APRO</option>
				<option value="estudiosEntregas.id_aplicativo=3"  id="estudiosEntregas.id_aplicativo=3">AYFL</option>
				<option value="estudiosEntregas.id_aplicativo=4"  id="estudiosEntregas.id_aplicativo=4">BISM</option>
				<option value="estudiosEntregas.id_aplicativo=5"  id="estudiosEntregas.id_aplicativo=5">CMAR</option>
				<option value="estudiosEntregas.id_aplicativo=6"  id="estudiosEntregas.id_aplicativo=6">CONT</option>
				<option value="estudiosEntregas.id_aplicativo=7"  id="estudiosEntregas.id_aplicativo=7">FAM2</option>
				<option value="estudiosEntregas.id_aplicativo=8"  id="estudiosEntregas.id_aplicativo=8">FAMA</option>
				<option value="estudiosEntregas.id_aplicativo=9"  id="estudiosEntregas.id_aplicativo=9">FARM</option>
				<option value="estudiosEntregas.id_aplicativo=10"  id="estudiosEntregas.id_aplicativo=10">FMAR</option>
				<option value="estudiosEntregas.id_aplicativo=11"  id="estudiosEntregas.id_aplicativo=11">FOM2</option>
				<option value="estudiosEntregas.id_aplicativo=12"  id="estudiosEntregas.id_aplicativo=12">FOMA</option>
				<option value="estudiosEntregas.id_aplicativo=13"  id="estudiosEntregas.id_aplicativo=13">FRMA</option>
				<option value="estudiosEntregas.id_aplicativo=14"  id="estudiosEntregas.id_aplicativo=14">GFOA</option>
				<option value="estudiosEntregas.id_aplicativo=15"  id="estudiosEntregas.id_aplicativo=15">IMAG</option>
				<option value="estudiosEntregas.id_aplicativo=16"  id="estudiosEntregas.id_aplicativo=16">INBU</option>
				<option value="estudiosEntregas.id_aplicativo=17"  id="estudiosEntregas.id_aplicativo=17">INCA</option>
				<option value="estudiosEntregas.id_aplicativo=18"  id="estudiosEntregas.id_aplicativo=18">INVE</option>
				<option value="estudiosEntregas.id_aplicativo=19"  id="estudiosEntregas.id_aplicativo=19">ISMW</option>
				<option value="estudiosEntregas.id_aplicativo=20"  id="estudiosEntregas.id_aplicativo=20">MEJP</option>
				<option value="estudiosEntregas.id_aplicativo=21"  id="estudiosEntregas.id_aplicativo=21">MGEN</option>
				<option value="estudiosEntregas.id_aplicativo=22"  id="estudiosEntregas.id_aplicativo=22">MIND</option>
				<option value="estudiosEntregas.id_aplicativo=23"  id="estudiosEntregas.id_aplicativo=23">MOVI</option>
				<option value="estudiosEntregas.id_aplicativo=24"  id="estudiosEntregas.id_aplicativo=24">OBIS</option>
				<option value="estudiosEntregas.id_aplicativo=25"  id="estudiosEntregas.id_aplicativo=25">PAGO</option>
				<option value="estudiosEntregas.id_aplicativo=26"  id="estudiosEntregas.id_aplicativo=26">PRES</option>
				<option value="estudiosEntregas.id_aplicativo=27"  id="estudiosEntregas.id_aplicativo=27">SANI</option>
				<option value="estudiosEntregas.id_aplicativo=28"  id="estudiosEntregas.id_aplicativo=28">SBOT</option>
				<option value="estudiosEntregas.id_aplicativo=29"  id="estudiosEntregas.id_aplicativo=29">SIEB</option>
				<option value="estudiosEntregas.id_aplicativo=30"  id="estudiosEntregas.id_aplicativo=30">TASA</option>
				<option value="estudiosEntregas.id_aplicativo=31"  id="estudiosEntregas.id_aplicativo=31">TISM</option>
				<option value="estudiosEntregas.id_aplicativo=32"  id="estudiosEntregas.id_aplicativo=32">WBOF</option>
				<option value="estudiosEntregas.id_aplicativo=33"  id="estudiosEntregas.id_aplicativo=33">WISM</option>
				<option value="estudiosEntregas.id_aplicativo=34"  id="estudiosEntregas.id_aplicativo=34">WSAO</option>
				<option value="estudiosEntregas.id_aplicativo=35"  id="estudiosEntregas.id_aplicativo=35">WSCR</option>
				<option value="estudiosEntregas.id_aplicativo=36"  id="estudiosEntregas.id_aplicativo=36">WSPX</option>
				<option value="estudiosEntregas.id_aplicativo=37"  id="estudiosEntregas.id_aplicativo=37">WSRT</option>
			</select>
			<input id="estudiosPeticiones.id_aplicativo" name="estudiosPeticiones.id_aplicativo" type="hidden" value=""/>

			<label class="infoCls"  title="Escala" id="escaladoLabel" for="escalado">&nbsp;&nbsp;Escala&nbsp;</label>
			<select onChange="javascript:vaciarOnSubmit('estudiosEntregas.id_servicio');vaciarOnSubmit('estudiosPeticiones.id_servicio');
					document.forms[0].submit();return true;" class="textInput" id="escalado" name="escalado" size="7">
					<option value="dayly" id="dayly">Diario</option>
					<option value="weekly" id="weekly">Semanal</option>
					<option value="monthly" id="monthly">Mensual</option>
					<option value="3monthly" id="3monthly">Trimestral</option>
					<option value="6monthly" id="6monthly">Semestral</option>
					<option value="anualy" id="anualy">Anual</option>
					<option value="automatic" id="automatic">Automatico</option>
			</select>

			<label class="infoCls"  title="Operación sobre datos" id="escaladoLabel" for="escalado">&nbsp;&nbsp;Escala&nbsp;</label>
			<select onChange="javascript:vaciarOnSubmit('estudiosEntregas.id_servicio');vaciarOnSubmit('estudiosPeticiones.id_servicio');
				document.forms[0].submit();return true;" class="textInput" 
				id="operation" name="operation" size="2">
					<option value="AVG" id="AVG">Promediar</option>
					<option value="SUM" id="SUM">Totalizar</option>					
			</select>						
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
					<jsp:param name="height" value="340px" />
				</jsp:include>
				<jsp:include page="${containerJSP_121}">
					<jsp:param name="idseries" value="_serie022" />
					<jsp:param name="width" value="500px" />
					<jsp:param name="height" value="140px" />
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
			if ("dayly".contentEquals(as[i])){%>
			   	document.getElementById('escalado').options[0].selected= 'selected';
		  <%}else if ("weekly".contentEquals(as[i])){%>			  
		   		document.getElementById('escalado').options[1].selected= 'selected';
		  <%}else if ("monthly".contentEquals(as[i])){%>			  
		   		document.getElementById('escalado').options[2].selected= 'selected';
		  <%}else if ("3monthly".contentEquals(as[i])){%>			  
		   		document.getElementById('escalado').options[3].selected= 'selected';
		  <%}else if ("6monthly".contentEquals(as[i])){%>			  
		   		document.getElementById('escalado').options[4].selected= 'selected';
		  <%}else if ("anualy".contentEquals(as[i])){%>			  
		   		document.getElementById('escalado').options[5].selected= 'selected';	
		  <%}else if ("automatic".contentEquals(as[i])){%>			  
		   		document.getElementById('escalado').options[6].selected= 'selected';	
		  <%}%>	
		<%}//for values
	}else{ %>
		  	document.getElementById('escalado').options[2].selected = 'selected';		  
  <%} %>
  
  <% 	
	Object operationParamValue = request.getAttribute("operation");
	if (operationParamValue != null){
		String[] as= (String[]) operationParamValue;
		for (int i=0;i<as.length;i++){
			if ("AVG".contentEquals(as[i])){%>
			   	document.getElementById('operation').options[0].selected= 'selected';
		  <%}else if ("SUM".contentEquals(as[i])){%>			  
		   		document.getElementById('operation').options[1].selected= 'selected';
		  <%}%>	
		<%}//for values
	}else{ %>
		  	document.getElementById('operation').options[0].selected = 'selected';		  
<%} %>
	
	<% 	
	Object entornoParamValues = request.getAttribute("estudiosEntregas.id_entorno");
	if (entornoParamValues != null){
		String[] as= (String[]) entornoParamValues;
		for (int i=0;i<as.length;i++){
			int selectedValue = (Integer.valueOf(as[i].split("=")[1]) - 1);
			//System.out.println("value selected:" + selectedValue);
		%>			
			document.getElementById('estudiosEntregas.id_entorno').options[<%=selectedValue%>].selected= 'selected';
		<%}//for values
	}else{//if %>
		   for (var j=0;j<5;j++){
		  	document.getElementById('estudiosEntregas.id_entorno').options[j].selected = 'selected';
		   }
  <%} %>
  
  <% 	
	Object servicioParamValues = request.getAttribute("estudiosEntregas.id_servicio");
	if (servicioParamValues != null){
		String[] as= (String[]) servicioParamValues;
		for (int i=0;i<as.length;i++){
			int selectedValue = (Integer.valueOf(as[i].split("=")[1]) - 1);
			//System.out.println("value selected:" + selectedValue);
		%>			
			document.getElementById('estudiosEntregas.id_servicio').options[<%=selectedValue%>].selected= 'selected';
		<%}//si hay seleccionado un servicio, anular la selection de aplicaciones%>		
		for (var j=0;j<37;j++){
		  	document.getElementById('estudiosEntregas.id_aplicativo').options[j].selected = '';
		}
	<%}else{//if %>
		   for (var j=0;j<3;j++){
		  	document.getElementById('estudiosEntregas.id_servicio').options[j].selected = '';
		   }
  <%} %>

  	
	<% 	
	Object aplicativoParamValues = request.getAttribute("estudiosEntregas.id_aplicativo");
	if (aplicativoParamValues != null){
		String[] as= (String[]) aplicativoParamValues;
		for (int i=0;i<as.length;i++){
			int selectedValue = (Integer.valueOf(as[i].split("=")[1]) - 1);
			//System.out.println("value selected:" + selectedValue);
		%>			
			document.getElementById('estudiosEntregas.id_aplicativo').options[<%=selectedValue%>].selected= 'selected';
		<%}%>
		for (var j=0;j<3;j++){
		  	document.getElementById('estudiosEntregas.id_servicio').options[j].selected = '';
		}
	<%}else{//if %>
		   for (var j=0;j<37;j++){
		  	document.getElementById('estudiosEntregas.id_aplicativo').options[j].selected = 'selected';
		   }
	<%} %>
	
	
	  </script>
	
	
