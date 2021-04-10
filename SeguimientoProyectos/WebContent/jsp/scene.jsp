<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
	
 	
    <%if (request.getAttribute("container") != null) { %>
	
  		<%
		  if (request.getAttribute("addedInfo") != null) { %>
		 		 <%=request.getAttribute("addedInfo")%> 
	   <%} %>
		
		<jsp:include page="${container}"></jsp:include>
		
	 <%} else { %>
		<div id="loadingdiv">
			<p align="center">
				<font class="small">Loading...please wait</font>
			</p>
		</div> 
	 	<div id="principal"><%=request.getAttribute("#BODY#")%></div> 
	 	
  <% } %>
	

