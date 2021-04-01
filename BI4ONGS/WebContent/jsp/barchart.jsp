<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
			
			Highcharts.chart('container', {
			    chart: {
			        type: 'bar',
		            backgroundColor: 'transparent'
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
			        categories: <%=request.getAttribute("categories")%>,
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
			            text: '<%=request.getAttribute("titulo_EJE_X")%>',
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
				  series: <%=request.getAttribute("series")%>
			    
			});
			
	</script>
