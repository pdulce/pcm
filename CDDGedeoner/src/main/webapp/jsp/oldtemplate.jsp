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
			initInputHighlightScript();initTree(<%=(String) request.getSession().getAttribute("fID")%>, <%=(String) request.getSession().getAttribute("gPfID")%>,  
	<%=(String) request.getSession().getAttribute("gP2fID")%>);
	document.getElementById('principal').style.display='block';
			document.getElementById('loadingdiv').style.display='none';">
			
  <div class="global">
     <div class="pcmNav"> <%=request.getAttribute("#LOGO#")%><%=request.getAttribute("#TREE#")%> </div>
     <div class="pcmBody">

		<%if (request.getAttribute("container") != null) {
		  	if (request.getAttribute("addedInfo") != null) { %>
		 		 <%=request.getAttribute("addedInfo")%> 
	      <%}%>
		
			<jsp:include page="${container}"></jsp:include>
		
		<%} else { %>
			<div id="loadingdiv">
				<p align="center">
					<font class="small">Loading...please wait</font>
				</p>
			</div> 
		 	<div id="principal"><%=request.getAttribute("#BODY#")%></div> 
		 	
	  	<%}%>

	 </div>  
  </div>

</body>

</html>