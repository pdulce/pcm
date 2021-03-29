<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
	
		Highcharts.chart('container', {
	        chart: {
	            type: 'scatter',
	            backgroundColor: 'transparent'
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
	                text: '<%=request.getAttribute("titulo_EJE_X")%>'
	            }
	        },
	        yAxis: {
	            title: {
	                text: '<%=request.getAttribute("titulo_EJE_Y")%>'
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