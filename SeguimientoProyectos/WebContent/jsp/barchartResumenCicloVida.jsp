<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>barchartResumenCicloVida" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
			
			Highcharts.chart('<%=idseries%>barchartResumenCicloVida', {
			    chart: {
			        type: 'bar',
		            backgroundColor: 'transparent'
			    },
			    title: {
			        text: 'Resumen Dedicaciones vs Lapsos en Nuevos Desarrollos Entornos Abiertos (Promedios en jornadas)',
			        style: {'color': 'orange', 'font-weight': 'lighter', 'font-size': '11pt'}
			    },
			    xAxis: {
			        categories: ['Dedicaciones Efectivas', 'Lapsos entre dedicaciones', 'Total Duración Ciclo Vida'],			        	
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
	                name: 'Jornadas',
	                color: '#c94b4b',
	                data: [71.65,89.38,161.04]
	            }]
			    //azul personalizado: '#11998e', verde personalizado: '#64f38c', rojo personalizado: '#c94b4b'
			});
			
	</script>
