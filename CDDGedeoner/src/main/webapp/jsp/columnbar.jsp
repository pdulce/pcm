<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
boolean visionado3D = false;
if ((String)request.getAttribute(idseries+"columnbarvisionado") == null){
	visionado3D = request.getParameter("visionado").contentEquals("3D");
}else{
	visionado3D = ((String)request.getAttribute(idseries+"columnbarvisionado")).contentEquals("3D");
}
%>
<div id="<%=idseries%>columnbar" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<jsp:include page="manejadorPaleta.jsp"></jsp:include>

<script type="text/javascript">

Highcharts.chart('<%=idseries%>columnbar', {
    chart: {
        type: '<%=request.getAttribute(idseries+"columnbartypeOfgraph")%>',
        backgroundColor: 'transparent',        
        options3d: {
        	enabled: <%=visionado3D%>,
            alpha: 15,
            beta: 15,
            viewDistance: 25,
            depth: 40
        },
        style: {
        	 fontFamily: 'Roboto, sans-serif'   	 
        }
    },

    title: {
        text: 'Dimensiones del Ciclo de Vida agrupadas por <%=request.getAttribute(idseries+"columnbaragrupadoPor")%>',
        style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
    },

    xAxis: {
        categories: <%=request.getAttribute(idseries+"columnbarcategories")%>,       
        labels: {
            skew3d: true,
            style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
        }
    },

    yAxis: {
        allowDecimals: false,
        min: 0,
        title: {
            text: 'Jornadas',
            skew3d: true
        },
        style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
    },
    legend: {
        layout: 'horizontal',
        align: 'center',
        y: 17,
        itemWidth: 165,        
        verticalAlign: 'bottom',
        itemStyle: {'color': itemColor, 'font-weight': 'normal', 'font-size': '8pt'},
    },

    tooltip: {
        headerFormat: '<b>{point.key}</b><br>',
        pointFormat: '<span style="color:{series.color}">\u25CF</span> {series.name}: {point.y} / {point.stackTotal}'
    },
    
    plotOptions: {    	
        column: {
            stacking: 'normal',
            depth: 40
        }
    },

    series: <%=request.getAttribute(idseries+"columnbarseries")%>
    
}); 
        
</script>
