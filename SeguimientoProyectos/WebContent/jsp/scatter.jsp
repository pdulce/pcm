<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>scatter" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<script type="text/javascript">
Highcharts.setOptions({
    colors: ['#2C5364', '#bdc3c7', '#7AA1D2', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4']
});
		
		Highcharts.chart('<%=idseries%>scatter', {
	        chart: {
	            type: 'scatter',
	            backgroundColor: 'transparent',
	            zoomType: 'xy',
	        },
	        title: {
	            text: '<%=request.getAttribute(idseries+"scattertitle")%>',
	            style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '11pt'}
	        },
	        subtitle: {
	            text: '<%=request.getAttribute(idseries+"scattersubtitle")%>',
	            style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '9pt'}
	        },
	        xAxis: {
	            title: {	                
	                text: '<%=request.getAttribute(idseries+"scattertitulo_EJE_X")%>',
	                style: {'color': '#606c88', 'font-weight': 'lighter'}	                
	            },
	            labels: {
	                style: {
	                    color: '#606c88',
	                    fontSize:'small'
	                }
	            }
	        },
	        yAxis: {
	            title: {
	                text: '<%=request.getAttribute(idseries+"scattertitulo_EJE_Y")%>',
	                itemStyle: {'color': '#606c88', 'font-weight': 'normal', 'font-size': '12px'}
	            },
	            labels: {
	                style: {
	                    color: '#606c88',
	                    fontSize:'small'
	                }
	            }
	        },
	        legend: {
	            layout: 'horizontal',
	            align: 'center',
	            y: 18,
	            itemWidth: 155,
	            itemStyle: {'color': '#606c88', 'font-weight': 'normal', 'font-size': '8px'},
	            verticalAlign: 'bottom'
	        },
	        plotOptions: {
	            scatter: {
	                marker: {
	                    radius: 2
	                },	                
	                tooltip: {
	                    headerFormat: '<b>{series.name}</b><br>',
	                    pointFormat: '{point.x} <%=request.getAttribute(idseries+"scattertooltip_X")%>, {point.y} <%=request.getAttribute(idseries+"scattertooltip_Y")%>'
	                }
	            }
	        },
	        series: [{
	            type: 'line',
	            name: 'Regression Line',
	            data: <%=request.getAttribute(idseries+"scatterline")%>,
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
	            data: <%=request.getAttribute(idseries+"scatterobservations")%>,
	            marker: {
	                radius: 3
	            }
	        }]
	    });
				
</script>