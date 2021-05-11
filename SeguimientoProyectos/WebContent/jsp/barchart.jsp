<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>barchart" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
Highcharts.setOptions({
    colors: ['#2C5364', '#bdc3c7', '#7AA1D2', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4']
});
			Highcharts.chart('<%=idseries%>barchart', {
			    chart: {
			        type: 'bar',
		            backgroundColor: 'transparent',
			    },
			    title: {
			        text: '<%=request.getAttribute(idseries+"barcharttitle")%>',
			        style: {'color': '#2c3e50', 'font-weight': 'lighter', 'font-size': '11pt'}
			    },
			    subtitle: {
		            text: '<%=request.getAttribute(idseries+"barchartsubtitle")%>',
		            style: {'color': '#2c3e50', 'font-weight': 'lighter', 'font-size': '9pt'}
		        },
			    xAxis: {
			        categories: <%=request.getAttribute(idseries+"barchartcategories")%>,
			        labels: {
		                style: {
		                    color: '#2c3e50',
		                    fontSize:'small'
		                }
		            }
			    },
			    yAxis: {
			        min: 0,
			        itemStyle: {'color': '#2c3e50', 'font-weight': 'lighter'},
			        title: {
			            text: '<%=request.getAttribute(idseries+"barcharttitulo_EJE_X")%>',
			            itemStyle: {'color': '#2c3e50', 'font-weight': 'normal', 'font-size': '12px'}
			        },
			        labels: {
		                style: {
		                    color: '#2c3e50',
		                    fontSize:'small'
		                }
		            }
			    },
			    legend: {
			        layout: 'horizontal',
			        align: 'center',
			        y: 18,
			        itemWidth: 150,
			        itemStyle: {'color': '#606c88', 'font-weight': 'normal', 'font-size': '8px'},
			        verticalAlign: 'bottom'
			    },
			    plotOptions: {
			        series: {
			            stacking: 'normal',
			            style: {'color': '#3f4c6b', 'font-weight': 'lighter', 'font-size': 'xx-small'}
			        }
			    },
				  series: <%=request.getAttribute(idseries+"barchartseries")%>
			    
			});
			
	</script>
