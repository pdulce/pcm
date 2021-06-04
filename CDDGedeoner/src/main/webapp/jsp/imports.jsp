<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%String defaultMode = (String)request.getAttribute("style");%>
<link rel="stylesheet" type="text/css" href="css/<%=defaultMode%>/body.css"></link>
<link rel="stylesheet" type="text/css" href="css/<%=defaultMode%>/form.css"></link>
<link rel="stylesheet" type="text/css" href="css/<%=defaultMode%>/button.css"></link>
<link rel="stylesheet" type="text/css" href="css/<%=defaultMode%>/checkbox.css"></link>
<link rel="stylesheet" type="text/css" href="css/<%=defaultMode%>/radio.css"></link>
<link rel="stylesheet" type="text/css" href="css/<%=defaultMode%>/grid.css"></link>
<link rel="stylesheet" type="text/css" href="css/<%=defaultMode%>/highchart.css"></link>
<link rel="stylesheet" type="text/css" href="css/<%=defaultMode%>/navigationtree.css"></link>
<link rel="stylesheet" type="text/css" href="css/<%=defaultMode%>/jquery-collapsible-fieldset.css"></link>

<!--bootstrap CSS-->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.1/dist/css/bootstrap.min.css" 
rel="stylesheet" integrity="sha384-+0n0xVW2eSR5OomGNYDnhzAbDsOXxcvSN1TPprVMTNDbiYZCxYbOOl7+AMvyTG2x" 
crossorigin="anonymous">

<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.8.1/css/all.css" 
integrity="sha384-50oBUHEmvpQ+1lW4y57PTFmhCaXp0ML5d60M1M7uH2+nqUivzIebhndOJK28anvf" crossOrigin="anonymous">
<link rel="preconnect" href="https://fonts.gstatic.com">
<link href="https://fonts.googleapis.com/css2?family=Assistant&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Spicy+Rice&display=swap" rel="stylesheet">


<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<!-- BOOTSTRAP.JS FOR MENU HANDLER, DATES AND OTHER THINGS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.1/dist/js/bootstrap.bundle.min.js" 
integrity="sha384-gtEjrD/SeCtmISkJkNUaaKMoLD0//ElJ19smozuHV6z3Iehds+3Ulb9Bn9Plx0x4" crossorigin="anonymous">
</script>


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
<script type="text/javascript" src="https://code.highcharts.com/maps/highmaps.js"></script>
<script type="text/javascript" src="https://code.highcharts.com/maps/modules/exporting.js"></script>
<script type="text/javascript" src="<%=request.getAttribute("mapa")%>"></script>	
<%}else {%>
<script type="text/javascript" src="https://code.highcharts.com/highcharts.js"></script>
<script type="text/javascript" src="https://code.highcharts.com/highcharts-more.js"></script>
<script type="text/javascript" src="https://code.highcharts.com/highcharts-3d.js"></script>
<script type="text/javascript" src="https://code.highcharts.com/modules/exporting.js"></script>
<script type="text/javascript" src="https://code.highcharts.com/modules/export-data.js"></script>
<script type="text/javascript" src="https://code.highcharts.com/modules/accessibility.js"></script>
<%}%>
<%if (defaultMode.contentEquals("darkmode")){%>
<script type="text/javascript" src="https://code.highcharts.com/themes/dark-unica.js"></script>	
<%}%>
