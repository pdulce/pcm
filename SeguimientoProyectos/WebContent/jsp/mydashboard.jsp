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
		<input id="entities" name="entities" type="hidden" value="<%=request.getParameter("entities") %>"/>
							    
  	
  	<div id="stats">
		<table>
			<tr>
				<td>
					<jsp:include page="${containerJSP_10}">
						<jsp:param name="idseries" value="_serie10" />
						<jsp:param name="width" value="680px" />
						<jsp:param name="height" value="460px" />
					</jsp:include>
				</td>
				<td>
					<jsp:include page="${containerJSP_11}">
						<jsp:param name="idseries" value="_serie11" />
						<jsp:param name="width" value="680px" />
						<jsp:param name="height" value="420px" />
					</jsp:include>
					<div>
						<br><br>&nbsp;<br>
					</div>
					<jsp:include page="${containerJSP_12}">
						<jsp:param name="idseries" value="_serie12" />
						<jsp:param name="width" value="680px" />
						<jsp:param name="height" value="260px" />
					</jsp:include>
				</td>
				<td>	 			
					<fieldset class="collapsible"><legend>&nbsp;</legend>
					<div class="pcmBody">
						<br><br>					
						<label class="infoCls" title="Operación de agregación">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Operaci&oacute;n	de agregaci&oacute;n&nbsp;</label>
						<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<label class="radiogroupcontainer">&nbsp;Promediar&nbsp; <input
							onChange="javascript:document.forms[0].submit();return true;"
							type="radio" class="checkmarkradio" value="AVG" id="operationAVG"
							name="operation"> <span class="checkmarkradio"></span>
						</label>
						<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<label class="radiogroupcontainer">Totalizar&nbsp; <input
							onChange="javascript:document.forms[0].submit();return true;"
							type="radio" class="checkmarkradio" value="SUM" id="operationSUM"
							name="operation"> <span class="checkmarkradio"></span>
						</label>
						<br><br><br><br>
						&nbsp;&nbsp;&nbsp;&nbsp;
						<label class="infoCls" title="Escala"> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Escala&nbsp;</label>
						<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<label class="radiogroupcontainer">&nbsp;Diario&nbsp; <input
							onChange="javascript:document.forms[0].submit();return true;"
							type="radio" class="checkmarkradio" value="dayly" id="escaladodayly"
							name="escalado"> <span class="checkmarkradio"></span>
						</label>
						<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<label class="radiogroupcontainer">&nbsp;Semanal&nbsp; <input
							onChange="javascript:document.forms[0].submit();return true;"
							type="radio" class="checkmarkradio" value="weekly" id="escaladoweekly"
							name="escalado"> <span class="checkmarkradio"></span>
						</label>
						<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<label class="radiogroupcontainer">&nbsp;Mensual&nbsp; <input
							onChange="javascript:document.forms[0].submit();return true;"
							type="radio" class="checkmarkradio" value="monthly"
							id="escaladomonthly" name="escalado"> <span
							class="checkmarkradio"></span>
						</label>
						<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<label class="radiogroupcontainer">&nbsp;Trimestral&nbsp; <input
							onChange="javascript:document.forms[0].submit();return true;"
							type="radio" class="checkmarkradio" value="3monthly"
							id="escalado3monthly" name="escalado"> <span
							class="checkmarkradio"></span>
						</label>
						<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<label class="radiogroupcontainer">&nbsp;Semestral&nbsp; <input
							onChange="javascript:document.forms[0].submit();return true;"
							type="radio" class="checkmarkradio" value="6monthly"
							id="escalado6monthly" name="escalado"> <span
							class="checkmarkradio"></span>
						</label>
						<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<label class="radiogroupcontainer">&nbsp;Anual&nbsp; <input
							onChange="javascript:document.forms[0].submit();return true;"
							type="radio" class="checkmarkradio" value="anualy" id="escaladoanualy"
							name="escalado"> <span class="checkmarkradio"></span>
						</label>
						<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<label class="radiogroupcontainer">&nbsp;Autom&aacute;tico&nbsp;
							<input onChange="javascript:document.forms[0].submit();return true;"
							type="radio" class="checkmarkradio" value="automatic"
							id="escaladoautomatic" name="escalado"> <span
							class="checkmarkradio"></span>
						</label>
						<br><br>
					  </div>
					</fieldset>			  			
	  			</td>
			</tr>
			<tr>
				<td>
					<jsp:include page="${containerJSP_20}">
						<jsp:param name="idseries" value="_serie20" />
						<jsp:param name="width" value="1200px" />
						<jsp:param name="height" value="460px" />
					</jsp:include>
				</td>
				<td>
					&nbsp;			
				</td>
				<td>
					<fieldset class="collapsible"><legend>Filtrar por criterios generales</legend>					    	
				    	<div class="pcmBody">
				    		<% if (request.getAttribute("estudio_all") != null){%>
				    		
				    		<br><br>				
				    		<label class="infoCls"  title="Estudio" id="estudios.idLABEL" for="estudios.id">
							&nbsp;&nbsp;Estudio&nbsp;
							</label>
							<select class="textInput" size="6" id="estudios.id" name="estudios.id" 
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
										  <option value="estudios.id=<%=idestudio%>" 
										  	id="estudios.id=<%=idestudio%>"><%=nombreestudio%></option>
									<%
									  }//while
								%>
				
							</select>
							
							<%}//if%>	
							<br><br>
							
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
				    		<br><br>
				    		
				    		<label class="infoCls"  title="Aplicativo" id="aplicativo.idLABEL" for="aplicativo.id">
							&nbsp;&nbsp;Aplicativo&nbsp;</label>
							<select class="textInput" size="30" id="aplicativo.id" name="aplicativo.id"
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
							<br><br>
						
							<label class="infoCls"  title="Fase Ciclo Vida" id="dimensionLabel" for="dimension">
							&nbsp;&nbsp;Dimensión(es) de agregación (series 1 y 2)&nbsp;</label>
							<select class="textInput" size="6" id="dimension" name="dimension"
								onChange="javascript:document.forms[0].submit();return true;" multiple>
								<%				  
								
								  Map<Integer,String> dimensiones = (Map<Integer,String>) request.getAttribute("dimensionesAll");
								  Iterator<Map.Entry<Integer,String>> iteDimensiones = dimensiones.entrySet().iterator();
								  while (iteDimensiones.hasNext()){					  
									  Map.Entry<Integer,String> entry = iteDimensiones.next();					  
									  Integer dimensionIesima = entry.getKey();
									  String nombreDimension = entry.getValue();
								%>
									  <option value="<%=dimensionIesima%>" id="<%=dimensionIesima%>"><%=nombreDimension%></option>
								<%
								  }//while
								%>
							</select>
							<br><br>										
						</div>						
					</fieldset>
				
				</td>
			</tr>
			<tr>
				<td>
					<jsp:include page="${containerJSP_21}">
						<jsp:param name="idseries" value="_serie21" />
						<jsp:param name="width" value="1200px" />
						<jsp:param name="height" value="460px" />
					</jsp:include>				
				</td>
				<td>
				&nbsp;
				</td>
			</tr>	
			<tr>
				<td>
					<jsp:include page="${containerJSP_30}">
						<jsp:param name="idseries" value="_serie30" />
						<jsp:param name="width" value="680px" />
						<jsp:param name="height" value="460px" />
					</jsp:include>
				</td>
				<td>
					<jsp:include page="${containerJSP_31}">
						<jsp:param name="idseries" value="_serie31" />
						<jsp:param name="width" value="680px" />
						<jsp:param name="height" value="460px" />
					</jsp:include>
				</td>
			</tr>
			<tr>
				<td>
					<jsp:include page="${containerJSP_40}">
						<jsp:param name="idseries" value="_serie40" />
						<jsp:param name="width" value="680px" />
						<jsp:param name="height" value="680px" />
					</jsp:include>
					<div>
						<br><br>&nbsp;<br>
					</div>
					
				</td>
				<td>
					<jsp:include page="${containerJSP_41}">
						<jsp:param name="idseries" value="_serie41" />
						<jsp:param name="width" value="680px" />
						<jsp:param name="height" value="680px" />
					</jsp:include>
					<div>
						<br><br>&nbsp;<br>
					</div>
					
				</td>
				<td>&nbsp;</td>
			</tr>	
		</table>
		
   </div>
	  		  	
   </form>
	    

<script type="text/javascript">
	
	var escala = document.forms[0].escalado;
	<% 	
	Object escaladoParamValue = request.getAttribute("escalado");
	if (escaladoParamValue != null){
		String[] as= (String[]) escaladoParamValue;
		for (int i=0;i<as.length;i++){
			//System.out.println("escalado: " + as[i]);
		%>		
			for (var j = 0; j < escala.length; j++) {
				if (escala[j].value == '<%=as[i]%>'){
					escala[j].checked = true;
				}
			}
		
		<%}//for values
	}else{ %>
			//alert('iniciando valores de escala...');
		  	for (var j = 0; j < escala.length; j++) {
		  		if (escala[j].value == 'monthly'){
		  			escala[j].checked = true;	
				}else{
					escala[j].checked = false;
				}
			}
	<%} %>
	
	var oper = document.forms[0].operation;
	<% 	
	Object operationParamValue = request.getAttribute("operation");
	if (operationParamValue != null){
		String[] as= (String[]) operationParamValue;
		for (int i=0;i<as.length;i++){
			//System.out.println("operation: " + as[i]);
		%>		
			for (var j = 0; j < oper.length; j++) {
				if (oper[j].value == '<%=as[i]%>'){
					oper[j].checked = true;
				}
			}
	<%}//for values
	}else{ %>
		for (var j = 0; j < oper.length; j++) {
			if (oper[j].value == 'AVG'){
				oper[j].checked = true;	
			}else{
				oper[j].checked = false;
			}
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
	   <%} else {//if %>
		 	for (var j=0;j<aplic.options.length;j++){
		 		aplic.options[j].selected = '';
			}
	   <%} %>
	
	   var dimens = document.getElementById('dimension');
		<% 	
		Object dimension_ParamValue = request.getAttribute("dimension");	
		if (dimension_ParamValue != null){		
			String[] as= (String[]) dimension_ParamValue;
			for (int i=0;i<as.length;i++){
				//System.out.println("dimension: " + as[i]);
			%>
			for (var j = 0; j < dimens.options.length; ++j) {
				//alert('¿dimension selected? '+ dimens.options[j].id);			
		    	if (dimens.options[j].id == '<%=as[i]%>'){
		    		//alert('¡¡dimension selected!! '+ dimens.options[j].id);
	   				dimens.options[j].selected= 'selected';
		    	}//if
			}//for
		<%}//for	
		}else{//if %>
			dimens.options[0].selected = 'selected';
			for (var j=1;j<dimens.options.length;j++){
				dimens.options[j].selected = '';
			}
		<%} %>
		
		var estu = document.getElementById('estudios.id');
		//alert("estu: " + estu);
		<% 	
			Object estudio2ParamValues = request.getAttribute("estudios.id");	     
			if (estudio2ParamValues != null){
				String[] as= (String[]) estudio2ParamValues;
				for (int i=0;i<as.length;i++){
					//System.out.println("estudios: " + as[i]);
				%>	
					for (var j = 0; j < estu.options.length; ++j) {
						//alert('¿estudios selected? '+ estu.options[j].id);			
				    	if (estu.options[j].value == '<%=as[i]%>'){
				    		//alert('¡¡estudios selected!! '+ estu.options[j].id);	
			    			estu.options[j].selected= 'selected';
				    	}//if
					}//for
				<%}%>
		<%}else{ %>	  	  	
			    for (var j=0;estu != null && j<estu.options.length;j++){
			        estu.options[j].selected = '';
				}
		<%}%>
  
</script>
