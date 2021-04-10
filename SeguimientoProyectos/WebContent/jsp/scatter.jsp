<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<div id="scatter" style="width: 1180px; height: 690px; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
	
		Highcharts.chart('scatter', {
	        chart: {
	            type: 'scatter',
	            backgroundColor: 'transparent',
	            zoomType: 'xy'
	        },
	        title: {
	            text: '<%=request.getAttribute("scattertitle")%>',
	            style: {'color': 'orange', 'font-weight': 'lighter'}
	        },
	        subtitle: {
	            text: '<%=request.getAttribute("scattersubtitle")%>',
	            style: {'color': 'orange', 'font-weight': 'lighter'}
	        },
	        xAxis: {
	            title: {	                
	                text: '<%=request.getAttribute("scattertitulo_EJE_X")%>',
	                style: {'color': 'orange', 'font-weight': 'lighter'}	                
	            },
	            labels: {
	                style: {
	                    color: 'orange',
	                    fontSize:'small'
	                }
	            }
	        },
	        yAxis: {
	            title: {
	                text: '<%=request.getAttribute("scattertitulo_EJE_Y")%>',
	                itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'}
	            },
	            labels: {
	                style: {
	                    color: 'orange',
	                    fontSize:'small'
	                }
	            }
	        },
	        plotOptions: {
	            scatter: {
	                marker: {
	                    radius: 2
	                },	                
	                tooltip: {
	                    headerFormat: '<b>{series.name}</b><br>',
	                    pointFormat: '{point.x} <%=request.getAttribute("scattertooltip_X")%>, {point.y} <%=request.getAttribute("scattertooltip_Y")%>'
	                }
	            }
	        },
	        series: [{
	            type: 'line',
	            name: 'Regression Line',
	            data: <%=request.getAttribute("scatterline")%>,
	            marker: {
	                enabled: false
	            },
	            states: {
	                hover: {
	                    lineWidth: 0
	                }
	            },
	            enableMouseTracking: false
	        }, {
	            type: 'scatter',
	            name: 'Observations',
	            data: <%=request.getAttribute("scatterobservations")%>,
	            marker: {
	                radius: 3
	            }
	        }]
	    });
				
</script>