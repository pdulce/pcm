<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
			
			Highcharts.chart('container', {
			    chart: {
			        type: 'bar'
			    },
			    title: {
			        text: '<%=request.getAttribute("title")%>'
			    },
			    subtitle: {
		            text: '<%=request.getAttribute("subtitle")%>'
		        },
			    xAxis: {
			        categories: <%=request.getAttribute("categories")%>
			    },
			    yAxis: {
			        min: 0,
			        title: {
			            text: '<%=request.getAttribute("titulo_EJE_X")%>'
			        }
			    },
			    legend: {
			        reversed: true
			    },
			    plotOptions: {
			        series: {
			            stacking: 'normal'
			        }
			    },
				  series: <%=request.getAttribute("barChart")%>
			    
			});
			
	</script>
