<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>barchartResumenDedicaciones" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
			
			Highcharts.chart('<%=idseries%>barchartResumenDedicaciones', {
			    chart: {
			        type: 'bar',
		            backgroundColor: 'transparent'
			    },
			    title: {
			        text: 'Comparativo entre las diferentes Dedicaciones Efectivas (Promedios en jornadas)',
			        style: {'color': 'orange', 'font-weight': 'lighter', 'font-size': '11pt'}
			    },
			    xAxis: {
			        categories: ['Duración Análisis', 'Duración Desarrollo', 'Preparación Entrega', 'Duración Pruebas CD'],		
			        labels: {
		                style: {
		                    color: 'orange',
		                    fontSize:'small'
		                }
		            }
			    },
			    yAxis: {
			        min: 0,
			        itemStyle: {'color': 'orange', 'font-weight': 'lighter'},
			        title: {
			            text: 'Jornadas',
			            itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'}
			        },
			        labels: {
		                style: {
		                    color: 'orange',
		                    fontSize:'small'
		                }
		            }
			    },
			    legend: {
			        reversed: true,
			        itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'}
			    },
			    plotOptions: {
			        series: {
			            stacking: 'normal',
			            style: {'color': '#3f4c6b', 'font-weight': 'lighter', 'font-size': 'xx-small'}
			        }
			    },			    
			    series: [{
	                name: 'Mantenimiento HOST',
	                data: [6.05 , 13.16, 1.28, 4.28]
	            }, {
	                name: 'Mantenimiento Pros@',
	                data: [6.12, 16, 4.80, 3.18]
	            }, {
	                name: 'Nuevos Desarrollos Entornos Abiertos',
	                data: [31.57,18.21,13.49,8.37]
	            }]
			    
			});
			
	</script>
