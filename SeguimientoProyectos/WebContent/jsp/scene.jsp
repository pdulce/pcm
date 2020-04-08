<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

	<div id="loadingdiv">
		<p align="center">
			<font class="small">Loading...please wait</font>
		</p>
	</div> 
	<%if (request.getAttribute("container") != null) { %>
		<figure class="highcharts-figure">
			<div id="container"></div>
			<%if (request.getAttribute("is3D") != null) { %>
				<div id="sliders">
			        <table>
			            <tr>
			                <td><label for="alpha">Alpha Angle</label></td>
			                <td><input id="alpha" type="range" min="0" max="45" value="15"/> <span id="alpha-value" class="value"></span></td>
			            </tr>
			            <tr>
			                <td><label for="beta">Beta Angle</label></td>
			                <td><input id="beta" type="range" min="-45" max="45" value="15"/> <span id="beta-value" class="value"></span></td>
			            </tr>
			            <tr>
			                <td><label for="depth">Depth</label></td>
			                <td><input id="depth" type="range" min="20" max="100" value="50"/> <span id="depth-value" class="value"></span></td>
			            </tr>
			        </table>
	   			</div>
   			<%} %>
		</figure> 
		
		<jsp:include page="${container}"></jsp:include>
		
		<br> 
			
		<% 	
		  if (request.getAttribute("addedInfo") != null) { %>
		 		 <%=request.getAttribute("addedInfo")%> <%
		  }
	 }//if graphic with highcharts %>
 	
	<div id="principal"><%=request.getAttribute("#BODY#")%></div> 

	<UL id="pcmUl">
	  <LI><a title="Volver" href="#"
		     onClick="javascript:window.history.back();"> <span>Volver</span>
	      </a>
	  </LI>
	</UL>
		
	<!-- div id="principal">&nbsp;</div> -->
