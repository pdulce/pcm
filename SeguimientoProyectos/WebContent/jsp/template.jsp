<%@ page language="java" contentType="text/html;charset=ISO-8859-1"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>
<head>

<title><%=request.getAttribute("#TITLE#")%></title>
<link rel="stylesheet" type="text/css" href="css/pcm.css"></link>
<link rel="stylesheet" type="text/css" href="css/jquery-collapsible-fieldset.css"></link>

<script type="text/javascript" src="js/pcm.js"></script>
<script type="text/javascript" src="js/PCMGeneral.js"></script>
<script type="text/javascript" src="js/PCMensajes.js"></script>
<script type="text/javascript" src="js/overlib_mini.js"></script>
<script type="text/javascript" src="js/overlib_csstyle_mini.js"></script>
<script type="text/javascript" src="js/calendario.js"></script>
<script type="text/javascript" src="js/validacore.js"></script>
<script type="text/javascript" src="js/spinner.js"></script>
<script type="text/javascript" src="js/highlight-active-input.js"></script>
<script type="text/javascript" src="js/ajax.js"></script>
<script type="text/javascript" src="js/context-menu.js"></script>
<script type="text/javascript" src="js/external/jquery-1.9.1.js"></script>
<script type="text/javascript" src="js/external/jquery.cookies.2.2.0.js"></script>
<script type="text/javascript" src="js/external/slide.js"></script>
<script type="text/javascript" src="js/folder-tree-static.js"></script>
<script type="text/javascript" src="js/external/jquery-3.3.1.min.js"></script>
<script type="text/javascript" src="js/external/jquery-collapsible-fieldset.js"></script>

<%
	if (request.getAttribute("json_countryMap") == null && request.getAttribute("json_europeMap") == null
			&& request.getAttribute("json_spainMap") == null) {
%>
<script src="https://code.highcharts.com/highcharts.js"></script>
<!-- <script src="https://code.highcharts.com/highcharts-more.js"></script>
		<script src="https://code.highcharts.com/highcharts-3d.js"></script> -->
<script src="https://code.highcharts.com/modules/exporting.js"></script>
<script src="https://code.highcharts.com/modules/export-data.js"></script>
<script src="https://code.highcharts.com/modules/accessibility.js"></script>
<%
	} else {//es un mapa
%>
		<script src="https://code.highcharts.com/highmaps.js"></script>
		<script src="https://code.highcharts.com/modules/exporting.js"></script>
		<%
			if (request.getAttribute("json_countryMap") != null) {
		%>
		<script src="https://code.highcharts.com/mapdata/custom/world.js"></script>
		<%
			} else if (request.getAttribute("json_europeMap") != null) {
		%>
		<script src="https://code.highcharts.com/mapdata/custom/europe.js"></script>
		<%
			} else if (request.getAttribute("json_spainMap") != null) {
		%>
		<script src="https://code.highcharts.com/mapdata/custom/spain.js"></script>
		<%  }
	} //end-if mapas
%>

</head>

<body
	onLoad="javascript:initTree(<%=(String) request.getSession().getAttribute("fID")%>, <%=(String) request.getSession().getAttribute("gPfID")%>,  
	<%=(String) request.getSession().getAttribute("gP2fID")%>);
	document.getElementById('principal').style.display='block';
			document.getElementById('loadingdiv').style.display='none';">

	<table>
		<tr>
			<td class="pcmTDLeft" valign="top"><%=request.getAttribute("#LOGO#")%><%=request.getAttribute("#TREE#")%></td>
			<td class="pcmTDRight">
				<div id="loadingdiv">

					<p align="center">
						<font class="small">Loading...please wait</font>
					</p>

				</div> <%
 	if (request.getAttribute("container") != null) {
 %>
				<figure class="highcharts-figure">
					<div id="container"
						style='min-width: <%=request.getAttribute("width-container")%>px; max-width: 1090px; height: <%=request.getAttribute("height-container")%>px; margin: 0 auto;'>
					</div>
				</figure> <br> <!-- layer container --> <%
 	if (request.getAttribute("is3D") != null) {
 %>
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
				</div> <%
 	}
 		if (request.getAttribute("addedInfo") != null) {
 				%> <%=request.getAttribute("addedInfo") %>
 	 <%	} %>
				<UL id="pcmUl">
					<LI><a title="Volver" href="#"
						onClick="javascript:window.history.back();"> <span>Volver</span>
					</a></LI>
				</UL>
				<div id="principal">&nbsp;</div> <%
 	    } else { %>
				<div id="principal"><%=request.getAttribute("#BODY#")%></div> <!-- layer de escenario de pcm -->
		<%
			}
		%>
			</td>
		</tr>

		<tr>
			<td class="small"><%=request.getAttribute("#FOOT#")%></td>
			<td class="small">&nbsp;</td>
		</tr>

	</table>

	<script type="text/javascript">
		$("fieldset.collapsible").collapsible('<%=request.getAttribute("userCriteria")%>');
		initInputHighlightScript();	
	</script>
	
	<%
    if (request.getAttribute("json_scatterSeries") != null) {%>
	   <jsp:include page="scatter.jsp"></jsp:include>
    <% 
    } %>
    
    <%
    if (request.getAttribute("json_pieChart") != null) {%>
	   <jsp:include page="piechart.jsp"></jsp:include>
    <% 
    } %>
    
    <%
    if (request.getAttribute("barChart") != null) {%>
	   <jsp:include page="barchart.jsp"></jsp:include>
    <% 
    } %>
    
    <%
    if (request.getAttribute("json_dualHistogram") != null) {%>
	   <jsp:include page="dualHistogram.jsp"></jsp:include>
    <% 
    } %>
    
    <%
    if (request.getAttribute("json_histogram3d") != null) {%>
	   <jsp:include page="histogram3d.jsp"></jsp:include>
    <% 
    } %>
    
    <%
    if (request.getAttribute("json_timeSeries") != null) {%>
	   <jsp:include page="timeseries.jsp"></jsp:include>
    <% 
    } %>

	<%
    if (request.getAttribute("json_spiderweb") != null) {%>
	   <jsp:include page="spiderweb.jsp"></jsp:include>
    <% 
    } %>


</body>

</html>