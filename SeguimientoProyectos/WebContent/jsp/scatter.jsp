<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
	
		Highcharts.chart('container', {
	        chart: {
	            type: 'scatter',
	            backgroundColor: 'transparent',
	            zoomType: 'xy'
	        },
	        title: {
	            text: '<%=request.getAttribute("title")%>',
	            style: {'color': 'orange', 'font-weight': 'lighter'}
	        },
	        subtitle: {
	            text: '<%=request.getAttribute("subtitle")%>',
	            style: {'color': 'orange', 'font-weight': 'lighter'}
	        },
	        xAxis: {
	            title: {	                
	                text: '<%=request.getAttribute("titulo_EJE_X")%>'	                
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
	                text: '<%=request.getAttribute("titulo_EJE_Y")%>',
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
	                    pointFormat: '{point.x} <%=request.getAttribute("tooltip_X")%>, {point.y} <%=request.getAttribute("tooltip_Y")%>'
	                }
	            }
	        },
	        series:  <%=request.getAttribute("series")%>
	    });
				
</script>