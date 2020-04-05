<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
	
		Highcharts.chart('container', {
	        chart: {
	            type: 'scatter',
	            zoomType: 'xy'
	        },
	        title: {
	            text: '<%=request.getAttribute("title")%>'
	        },
	        subtitle: {
	            text: '<%=request.getAttribute("subtitle")%>'
	        },
	        xAxis: {
	            title: {
	                enabled: true,
	                text: '<%=request.getAttribute("titulo_EJE_X")%>'
	            },
	            startOnTick: true,
	            endOnTick: true,
	            showLastLabel: true
	        },
	        yAxis: {
	            title: {
	                text: '<%=request.getAttribute("titulo_EJE_Y")%>'
	            }
	        },
	        legend: {		            	
	            width: 940,
	            floating: true,
	            align: 'left',
	            x: 60,
	            y: 14,
	            itemWidth: 178,
	            itemStyle: {'color': 'black', 'font-weight': 'bold', 'font-size': '9px'},
	            itemDistance: 80,
	            borderWidth: 0
	        },
	        plotOptions: {
	            scatter: {
	                marker: {
	                    radius: 2,
	                    states: {
	                        hover: {
	                            enabled: true,
	                            lineColor: 'rgb(100,100,100)'
	                        }
	                    }
	                },
	                states: {
	                    hover: {
	                        marker: {
	                            enabled: false
	                        }
	                    }
	                },
	                tooltip: {
	                    headerFormat: '<b>{series.name}</b><br>',
	                    pointFormat: '{point.x} <%=request.getAttribute("tooltip_X")%>, {point.y} <%=request.getAttribute("tooltip_Y")%>'
	                }
	            }
	        },
	        series:  <%=request.getAttribute("series")%>
	    });
				
</script>