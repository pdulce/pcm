<%@page import="java.util.Collection"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="facturacionUte.common.ConstantesModelo"%>
<%@page import="domain.service.dataccess.definitions.IEntityLogic"%>
<%@page import="domain.service.component.definitions.FieldViewSet"%>
<%@page import="java.util.Iterator"%>
<%@page import="domain.service.component.definitions.FieldViewSetCollection"%>
<%@page import="java.util.Enumeration"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"  pageEncoding="UTF-8"%>

<form class="pcmForm" action="prjManager" method="POST">
    
    <input id="exec" name="exec" type="hidden" value="dashboard"/>
	<input id="event" name="event" type="hidden" value="dashboard"/>
	<input id="agrupacion" name="agrupacion" type="hidden" value="3"/> <!--3: id_aplicacion, 2:id_estudio -->
	<input id="entities" name="entities" type="hidden" value="resumenPeticiones"/>
    
    <fieldset class="collapsible"><legend>Filtrar por criterios generales</legend>	
    	
    	<div>    	
    		<label class="infoCls"  title="Aplicativo" id="aplicativo.idLABEL" for="aplicativo.id">
			&nbsp;&nbsp;Aplicativo&nbsp;</label>
			<select class="textInput" size="4" id="aplicativo.id" name="aplicativo.id"
				onChange="javascript:document.forms[0].submit();return true;"  multiple>
				<%				  
				  Collection<FieldViewSetCollection> listaApps = (Collection<FieldViewSetCollection>) request.getAttribute("aplicativo_all");
				  Iterator<FieldViewSetCollection> iteFieldViewSet = listaApps.iterator();
				  while (iteFieldViewSet.hasNext()){
					  Iterator<FieldViewSet> ite = iteFieldViewSet.next().getFieldViewSets().iterator();
					  FieldViewSet fieldViewSet = ite.next();
					  Long idAplicativo = (Long) fieldViewSet.getValue(fieldViewSet.getEntityDef().searchField(ConstantesModelo.APLICATIVO_1_ID).getName());
					  String nombreApp = (String) fieldViewSet.getValue(fieldViewSet.getEntityDef().searchField(ConstantesModelo.APLICATIVO_2_ROCHADE).getName());
				%>
					  <option value="aplicativo.id=<%=idAplicativo%>" id="aplicativo.id=<%=idAplicativo%>"><%=nombreApp%></option>
				<%
				  }//while
				%>
			</select>
					
			<label class="infoCls"  title="Fase Ciclo Vida" id="dimensionPLabel" for="dimensionP">
			&nbsp;&nbsp;Fase Ciclo Vida Peticiones&nbsp;</label>
			<select class="textInput" size="11" id="dimensionP" name="dimensionP" 
				onChange="javascript:document.forms[0].submit();return true;">
				<%				  
				  Map<Integer,String> dimensionesP = (Map<Integer,String>) request.getAttribute("dimensiones4Peticiones");
				  Iterator<Map.Entry<Integer,String>> iteDimensionesP = dimensionesP.entrySet().iterator();
				  while (iteDimensionesP.hasNext()){					  
					  Map.Entry<Integer,String> entry = iteDimensionesP.next();					  
					  Integer dimensionIesima = entry.getKey();
					  String nombreDimension = entry.getValue();
				%>
					  <option value="<%=dimensionIesima%>" id="<%=dimensionIesima%>"><%=nombreDimension%></option>
				<%
				  }//while
				%>
			</select>
		</div>
			
		<div>
			
			<label class="infoCls" title="Entorno" id="aplicativo.id_tecnologiaLABEL" for="aplicativo.id_tecnologia">
    		&nbsp;&nbsp;Entorno&nbsp;
    		</label>
			<select class="textInput" id="aplicativo.id_tecnologia" name="aplicativo.id_tecnologia"
				onChange="javascript:document.forms[0].submit();return true;" size="5" multiple>
				<%
				  Collection<FieldViewSetCollection> listatecnologias = (Collection<FieldViewSetCollection>) request.getAttribute("tecnologia_all");
				  Iterator<FieldViewSetCollection> itelistatecnologias = listatecnologias.iterator();
				  while (itelistatecnologias.hasNext()){					  
					  Iterator<FieldViewSet> ite = itelistatecnologias.next().getFieldViewSets().iterator();
					  FieldViewSet fieldViewSet = ite.next();
					  Long idtecnologia = (Long) fieldViewSet.getValue(fieldViewSet.getEntityDef().searchField(ConstantesModelo.TECHNOLOGY_1_ID).getName());
					  String nombretecnologia = (String) fieldViewSet.getValue(fieldViewSet.getEntityDef().searchField(ConstantesModelo.TECHNOLOGY_2_NOMBRE).getName());					  
				%>
					  <option value="aplicativo.id_tecnologia=<%=idtecnologia%>" 
					  	id="aplicativo.id_tecnologia=<%=idtecnologia%>"><%=nombretecnologia%></option>
				<%
				  }//while
				 
				%>				
			</select>
   			
			<label class="infoCls"  title="Servicio Graficos Peticiones" id="resumenPeticiones.id_estudioLABEL" for="resumenPeticiones.id_estudio">
			&nbsp;&nbsp;Estudio Gráficos Peticiones&nbsp;
			</label>
			<select class="textInput" size="3" id="resumenPeticiones.id_estudio" name="resumenPeticiones.id_estudio" 
				onChange="javascript:document.forms[0].submit();return true;"
				 multiple>
				 
				 <%				  
				 Collection<FieldViewSetCollection> listaestudios = (Collection<FieldViewSetCollection>) request.getAttribute("estudio_all");
				 Iterator<FieldViewSetCollection> itelistaestudios = listaestudios.iterator();
				 while (itelistaestudios.hasNext()){
					  Iterator<FieldViewSet> ite = itelistaestudios.next().getFieldViewSets().iterator();
					  FieldViewSet fieldViewSet = ite.next();
					  Long idestudio = (Long) fieldViewSet.getValue(fieldViewSet.getEntityDef().searchField(ConstantesModelo.ESTUDIOS_1_ID).getName());
					  String nombreestudio = (String) fieldViewSet.getValue(fieldViewSet.getEntityDef().searchField(ConstantesModelo.ESTUDIOS_2_TITULO).getName());
				%>
					  <option value="resumenPeticiones.id_estudio=<%=idestudio%>" 
					  	id="resumenPeticiones.id_estudio=<%=idestudio%>"><%=nombreestudio%></option>
				<%
				  }//while
				%>

			</select>
		</div>
				
		<div>
			<jsp:include page="commonGraphFilters.jsp"/>
		</div>

			
	</fieldset>
</form>


<script type="text/javascript">

	var estu = document.getElementById('resumenPeticiones.id_estudio');
	<% 	
		Object servicio2ParamValues = request.getAttribute("resumenPeticiones.id_estudio");
		if (servicio2ParamValues != null){
			String[] as= (String[]) servicio2ParamValues;
			for (int i=0;i<as.length;i++){
			%>	
				for (var j = 0; j < estu.options.length; ++j) {
			    	if (estu.options[j].value == '<%=as[i]%>'){	    			
		    			estu.options[j].selected= 'selected';
			    	}//if
				}//for
			<%}%>
			document.getElementById('agrupacion').value = '2';
	<%}else{//if %>	  	  	
		    for (var j=0;j<estu.options.length;j++){
		        estu.options[j].selected = '';
			}
		    document.getElementById('agrupacion').value = '3';
	<%}%>
	
	var dimP = document.getElementById('dimensionP');
	<% 	
	Object dimensionP_ParamValue = request.getAttribute("dimensionP");
	if (dimensionP_ParamValue != null){
		String[] as= (String[]) dimensionP_ParamValue;
		for (int i=0;i<as.length;i++){%>
		for (var j = 0; j < dimE.options.length; ++j) {		    	
	    	if (dimP.options[j].id == '<%=as[i]%>'){
   				dimP.options[j].selected= 'selected';
	    	}//if
		}//for
	<%}//for	
	}else{//if %>
		dimP.options[0].selected = 'selected';
		for (var j=1;j<dimP.options.length;j++){
			dimP.options[j].selected = '';
		}
	<%} %>
	
	  var tecn = document.getElementById('aplicativo.id_tecnologia');
	  <% 	
		Object entornoParamValues = request.getAttribute("aplicativo.id_tecnologia");
		if (entornoParamValues != null){
			String[] as= (String[]) entornoParamValues;
			for (int i=0;i<as.length;i++){
			%>			
			//alert('id_tecnologia seleccionada...');
			for (var j = 0; j < tecn.options.length; ++j) {		    	
		    	if (tecn.options[j].id == '<%=as[i]%>'){
		    		//alert('selected value: '+ tecn.options[j].id);
	    			tecn.options[j].selected= 'selected';
		    	}//if
			}//for
			<%}//for values
		}else{//if %>
			for (var j=0;j<tecn.options.length;j++){
				tecn.options[j].selected = '';
			}
		<%} %>
	  	
		var aplic = document.getElementById('aplicativo.id');
		<% 	
		Object aplicativoParamValues = request.getAttribute("aplicativo.id");
		if (aplicativoParamValues != null){
			String[] as= (String[]) aplicativoParamValues;
			for (int i=0;i<as.length;i++){
		   %>
			   for (var j = 0; j < aplic.options.length; ++j) {		    	
			    	if (aplic.options[j].id == '<%=as[i]%>'){
		   				aplic.options[j].selected= 'selected';
			    	}//if
				}//for
		  <%}//for%>
			document.getElementById('agrupacion').value = '3';
	   <%} else {//if %>
		 	for (var j=0;j<aplic.options.length;j++){
		 		aplic.options[j].selected = '';
			}
			document.getElementById('agrupacion').value = '2';
	   <%} %>
	
</script>
