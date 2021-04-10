<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>scatter" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<script type="text/javascript">
	
		Highcharts.chart('<%=idseries%>scatter', {
	        chart: {
	            type: 'scatter',
	            backgroundColor: 'transparent',
	            zoomType: 'xy'
	        },
	        title: {
	            text: '<%=request.getAttribute(idseries+"scattertitle")%>',
	            style: {'color': 'orange', 'font-weight': 'lighter'}
	        },
	        subtitle: {
	            text: '<%=request.getAttribute(idseries+"scattersubtitle")%>',
	            style: {'color': 'orange', 'font-weight': 'lighter'}
	        },
	        xAxis: {
	            title: {	                
	                text: '<%=request.getAttribute(idseries+"scattertitulo_EJE_X")%>',
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
	                text: '<%=request.getAttribute(idseries+"scattertitulo_EJE_Y")%>',
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