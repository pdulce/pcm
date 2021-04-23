<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>piechartCicloVida" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<script type="text/javascript">
	   
  Highcharts.chart('<%=idseries%>piechartCicloVida', {
   chart: {	   
         backgroundColor: 'transparent',
         type: 'pie',
         borderWidth: 0
     },
     title: {
         text: 'Dimensiones del Ciclo de Vida Peticiones Entornos Abiertos (promedios en jornadas)',
         style: {'color': 'orange', 'font-weight': 'lighter', 'font-size': '11pt'}
    },
    tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.1f} %</b>'
    },
    accessibility: {
        point: {
            valueSuffix: '%'
        }
    },
    plotOptions: {
        pie: {
            allowPointSelect: true,
            cursor: 'pointer',
            dataLabels: {
                enabled: true,
                format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                connectorColor: 'silver'
            }
        }
    },
    series: [{
        name: 'Jornadas',
        data: [
            { name: 'Análisis', y: 31.57 },
            { name: 'Desarrollo', y: 18.21  },
            { name: 'Preparación Entrega', y: 13.49 },
            { name: 'Pruebas CD', y: 8.37 },
            { name: 'Lapso Planif.DG', y: 18.44 },
            { name: 'Lapso Planif.CD', y: 28.77 },
            { name: 'Soporte CD', y: 7.47 },
            { name: 'Pruebas Resto versión', y: 15.78 },
            { name: 'Lapso Planif. Instalac.Prod.', y: 18.92 }
        ]
    }]
});

</script>
