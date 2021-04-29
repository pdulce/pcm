<%@ page language="java" contentType="text/html;charset=ISO-8859-1"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

<head>
	<title><%=request.getAttribute("#TITLE#")%></title>
	<jsp:include page="imports.jsp"></jsp:include>
</head>

<body
	onLoad="javascript:$('fieldset.collapsible').collapsible('<%=request.getAttribute("userCriteria")%>');
			initInputHighlightScript();
			document.getElementById('principal').style.display='block';
			document.getElementById('loadingdiv').style.display='none';">
			
<div class="global">

  <div class="pcmNav">
  	
  	  <%if (!"".contentEquals((String)request.getAttribute("#MENU_ITEMS#")) ) { %>
  	 	<div style="position: relative;top: 0px;left: 15px;" id="menuSup">
  	 		
  	 		<%String defaultMode = (String)request.getAttribute("style");
  	 		   if (defaultMode.startsWith("dark")){ %>
  	 		 	 <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
  	 		<%}else {%>
	  	 		 <nav class="navbar navbar-expand-lg navbar-light bg-light">
		     <%}%>
		     
		    	<div class="container-fluid">
				       <span class="navbar-brand mb-0 h1">Portal CDISM</span>				      
				       <div class="collapse navbar-collapse" id="navbarSupportedContent">
				         <ul class="navbar-nav me-auto mb-lg-0">
				           <li class="nav-item">   
				                 <a class="nav-link active" href="prjManager">
				               <i class="fas fa-home"></i>&nbsp;</a>
				           </li>
				          <li class="nav-item dropdown">
				              <a
				               class="nav-link dropdown-toggle"
				               href="#"
				               id="navbarDropdown"
				               role="button"
				               data-bs-toggle="dropdown"
				               aria-expanded="false"
				             >
				             <i class="fas fa-anchor"></i>&nbsp;Estructura
				             </a>
				             <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
				               <li><a class="dropdown-item" href="prjManager?event=GestionEntidades.query&entityName=unidadOrg">Organismos</a></li>
				               <li><a class="dropdown-item" href="prjManager?event=GestionEntidades.query&entityName=subdireccion">Subdirecciones</a></li>
				               <li><a class="dropdown-item" href="prjManager?event=GestionServiciosUTE.query">Servicios UTE</a></li>
				               <li><a class="dropdown-item" href="prjManager?event=GestionApps.query">Aplicativos</a></li>				               
				             </ul>
				           </li>	           
				           <li class="nav-item dropdown">
				              <a
				               class="nav-link dropdown-toggle"
				               href="#"
				               id="navbarDropdown"
				               role="button"
				               data-bs-toggle="dropdown"
				               aria-expanded="false"
				             >
				             <i class="fas fa-edit"></i>&nbsp;Seguimiento
				             </a>
				             <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
				               <li><a class="dropdown-item" href="prjManager?event=GestionTech.query">Tecnologías</a></li>
				               <li><a class="dropdown-item" href="prjManager?event=GestionEntidades.query&entityName=tiposPeticiones">Tipos Peticiones</a></li>
				               <li><a class="dropdown-item" href="prjManager?event=ConsultaPeticionesGEDEON.query">Peticiones importadas</a></li>
				               <li><a class="dropdown-item" href="prjManager?event=EstudioPeticiones.query">Estudios Peticiones</a></li>
				               <li><a class="dropdown-item" href="prjManager?event=EstudioEntregas.query">Estudios Entregas</a></li>
				               <li><a class="dropdown-item" href="http://localhost:9080/reporting"><i class="fas fa-file-export"></i>&nbsp;Reporting</a></li>
				             </ul>
				           </li>
				           
				            <li class="nav-item dropdown">
				              <a
				               class="nav-link dropdown-toggle"
				               href="#"
				               id="navbarDropdown"
				               role="button"
				               data-bs-toggle="dropdown"
				               aria-expanded="false"
				             >
				             <i class="fas fa-chart-line"></i>&nbsp;Dashboards
				             </a>
				             <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
				               <li><a class="dropdown-item" href="prjManager?exec=dashboard&event=dashboard&entities=resumenPeticiones"><i class="fas fa-chart-bar"></i>&nbsp;Estudios Peticiones</a></li>
				               <li><a class="dropdown-item" href="prjManager?exec=dashboard&event=dashboard&entities=resumenEntregas"><i class="fas fa-chart-bar"></i>&nbsp;Estudios Entregas</a></li>
				               <li><a class="dropdown-item" href="prjManager?exec=dashboard&event=dashboard&entities=peticiones"><i class="fas fa-chart-bar"></i>&nbsp;Peticiones (en construcción)</a></li>
				               <li>&nbsp;&nbsp;&nbsp;&nbsp;Demos</li>
				               <li><hr class="dropdown-divider" /></li>
				               <li><a class="dropdown-item" href="prjManager?event=Demos.query"><i class="fas fa-globe"></i>&nbsp;Mapas</a></li>
				             </ul>
				           </li>				          
				           <li class="nav-item dropdown">
				              <a
				               class="nav-link dropdown-toggle"
				               href="#"
				               id="navbarDropdown"
				               role="button"
				               data-bs-toggle="dropdown"
				               aria-expanded="false"
				             >
				             <i class="fas fa-satellite-dish"></i>&nbsp;Demos
				             </a>
				             <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
				               <li><a class="dropdown-item" href="http://localhost:9080/discover"><i class="fas fa-atlas"></i>&nbsp;Investment world research</a></li>
				               <li><a class="dropdown-item" href="http://localhost:9080/uploadForm"><i class="far fa-file-audio"></i>&nbsp;PDF To Audio File</a></li>
				             </ul>
				           </li>
				           <li class="nav-item dropdown">
				              <a
				               class="nav-link dropdown-toggle"
				               href="#"
				               id="navbarDropdown"
				               role="button"
				               data-bs-toggle="dropdown"
				               aria-expanded="false"
				             >
				             <i class="fas fa-cogs"></i>&nbsp;Configuración
				             </a>
				             <ul class="dropdown-menu" aria-labelledby="navbarDropdown">
				               <li><a class="dropdown-item" href="prjManager?exec=Configuration">Configuración site</a></li>
				               <li><a class="dropdown-item" href="prjManager?event=GestionEntidades.query&entityName=rol">Roles</a></li>
				               <li><a class="dropdown-item" href="prjManager?event=GestionUsuarios.query">Usuarios</a></li>
				               <li><a class="dropdown-item" href="prjManager?event=GestionEntidades.query&entityName=mes">Meses</a></li>
				             </ul>
				           </li>
				         </ul>				         
				       </div>
				     </div>
				   </nav>
		   </div>
		   
		  <%} %>
		   
  	 </div>
  	 
     <div class="pcmBody">

		<%if (request.getAttribute("container") != null) {	%>
		<div style="position: relative;top: 2px;left: 1px;">
			<jsp:include page="${container}"></jsp:include>
		</div>
		
		<%} else { %>
		
		<div style="position: relative;top: 2px;left: 15px;" id="loadingdiv">
			<p align="center">
				<font class="small">Loading...please wait</font>
			</p>
		</div> 
	 	<div style="position: relative;top: 0px;left: 15px;" id="principal"><%=request.getAttribute("#BODY#")%></div> 

	  	<%}%>
	 	 
  	</div>

</div>

</body>

</html>