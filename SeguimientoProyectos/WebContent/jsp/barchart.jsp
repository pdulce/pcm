<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<div id="barchart" style="width: 1080px; height: 700px; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
			
			Highcharts.chart('barchart', {
			    chart: {
			        type: 'bar',
		            backgroundColor: 'transparent'
			    },
			    title: {
			        text: '<%=request.getAttribute("barcharttitle")%>',
			        style: {'color': 'orange', 'font-weight': 'lighter'}
			    },
			    subtitle: {
		            text: '<%=request.getAttribute("barchartsubtitle")%>',
		            style: {'color': 'orange', 'font-weight': 'lighter'}
		        },
			    xAxis: {
			        categories: <%=request.getAttribute("barchartcategories")%>,
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
			            text: '<%=request.getAttribute("barcharttitulo_EJE_X")%>',
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
				  series: <%=request.getAttribute("barchartseries")%>
			    
			});
			
	</script>
