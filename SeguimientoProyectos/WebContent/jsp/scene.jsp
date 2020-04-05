<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

	<div id="loadingdiv">
		<p align="center">
			<font class="small">Loading...please wait</font>
		</p>
	</div> 
	<%	if (request.getAttribute("container") != null) { %>
		<figure class="highcharts-figure">
			<div id="container"
				style='min-width: <%=request.getAttribute("width-container")%>px; max-width: 1090px; height: <%=request.getAttribute("height-container")%>px; margin: 0 auto;'>
			</div>
		</figure> 
		<jsp:include page="${container}"></jsp:include>
		<br> 
	<%	if (request.getAttribute("is3D") != null) { %>
		<div id="sliders">
			<table>
				<tr>
					<td><font class="small">transversal round angle</font></td>
					<td><input id="R0" type="range" min="0" max="45" value="15" />
						<span id="R0-value" class="value"></span></td>
				</tr>
				<tr>
					<td><font class="small">longitudinal round angle</font></td>
					<td><input id="R1" type="range" min="0" max="45" value="15" />
						<span id="R1-value" class="value"></span></td>
				</tr>
			</table>
		</div> 
	<% 	} 
 		if (request.getAttribute("addedInfo") != null) { %>
 			 <%=request.getAttribute("addedInfo")%> <%
 		}
 	}%>
 	
	<div id="principal"><%=request.getAttribute("#BODY#")%></div> 

	   <UL id="pcmUl">
		<LI><a title="Volver" href="#"
			onClick="javascript:window.history.back();"> <span>Volver</span>
		</a></LI>
		</UL>
		
	<div id="principal">&nbsp;</div>
	
	
  