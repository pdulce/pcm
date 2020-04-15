<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<link rel="stylesheet" type="text/css" href="css/darkmode/body.css"></link>
<link rel="stylesheet" type="text/css" href="css/darkmode/form.css"></link>
<link rel="stylesheet" type="text/css" href="css/darkmode/button.css"></link>
<link rel="stylesheet" type="text/css" href="css/darkmode/grid.css"></link>
<link rel="stylesheet" type="text/css" href="css/darkmode/highchart.css"></link>
<link rel="stylesheet" type="text/css" href="css/darkmode/navigationtree.css"></link>
<link rel="stylesheet" type="text/css" href="css/darkmode/jquery-collapsible-fieldset.css"></link>

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

<%	if (request.getAttribute("mapa") != null) {	%>
		
	<script src="https://code.highcharts.com/maps/highmaps.js"></script>
	<script src="https://code.highcharts.com/maps/modules/exporting.js"></script>
	<script src="<%=request.getAttribute("mapa")%>"></script>
<%
	}else {
%>
	<script src="https://code.highcharts.com/highcharts.js"></script>
	<script src="https://code.highcharts.com/highcharts-more.js"></script>
	<script src="https://code.highcharts.com/highcharts-3d.js"></script>
	<script src="https://code.highcharts.com/modules/exporting.js"></script>
	<script src="https://code.highcharts.com/modules/export-data.js"></script>
	<script src="https://code.highcharts.com/modules/accessibility.js"></script>

<%  }%>
    