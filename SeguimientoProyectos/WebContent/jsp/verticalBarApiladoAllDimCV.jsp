<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>verticalBarApiladoAllDimCV" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
Highcharts.chart('<%=idseries%>verticalBarApiladoAllDimCV', {
    chart: {
        type: 'column',
        backgroundColor: 'transparent',
        options3d: {
            enabled: true,
            alpha: 15,
            beta: 15,
            viewDistance: 25,
            depth: 40
        }
    },

    title: {
        text: 'Promedios de las dimensiones que forman el Ciclo de Vida de las Peticiones (agrupadas por cada grupo de estudio)',
        style: {'color': 'orange', 'font-weight': 'lighter', 'font-size': '11pt'}
    },

    xAxis: {
        categories: ['Análisis', 'Desarrollo', 'Preparación Entrega', 'Pruebas CD', 
        	'Lapso Planif.DG', 'Lapso Planif.CD', 'Soporte CD', 'Pruebas Resto versión', 'Lapso Planif. Instalac.Prod.' ],        
        labels: {
            skew3d: true,
            style: {                
            	 fontSize: '16px'
            }
        }
    },

    yAxis: {
        allowDecimals: false,
        min: 0,
        title: {
            text: 'Jornadas',
            skew3d: true
        }
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

     series: [{
        name: 'Estudio Mto. HOST',
        data: [6.05 , 13.16, 1.28, 4.28, 3.17, 1.38, 10.39, 0.28, 1.72]
    }, {
        name: 'Estudio Mto. Pros@',
        data: [6.12, 16, 4.80, 3.18, 8.90,10.22, 14.28, 5.95, 5.38]
    }, {
        name: 'Estudio Nuevos Desarrollos Entornos Abiertos',
        data: [31.57,18.21,13.49,8.37, 18.44, 28.77, 7.47, 15.78, 18.92]
    }]
});

</script>
