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
	colors: [ '#B3DFF2', '#06B5CA','#00607E', '#1A3B47', '#CFCECE','#FCBF0A','#64E572', '#FFF263', '#6AF9C4']
});
		
		Highcharts.chart('<%=idseries%>scatter', {
	        chart: {
	            type: 'scatter',
	            backgroundColor: 'transparent',
	            options3d: {
	                enabled: false,
	                alpha: 15,
	                beta: 15,
	                viewDistance: 25,
	                depth: 40
	            },
	            zoomType: 'xy',
	            style: {
	            	 fontFamily: 'Roboto, sans-serif'   	 
	            }
	        },
	        title: {
	            text: '<%=request.getAttribute(idseries+"scattertitle")%>',
	            style: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'}
	        },
	        subtitle: {
	            text: '<%=request.getAttribute(idseries+"scattersubtitle")%>',
	            style: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'}
	        },
	        xAxis: {
	            title: {	                
	                text: '<%=request.getAttribute(idseries+"scattertitulo_EJE_X")%>',
	                style: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'}	                
	            },
	            labels: {
	            	style: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'}
	            }
	        },
	        yAxis: {
	            title: {
	                text: '<%=request.getAttribute(idseries+"scattertitulo_EJE_Y")%>',
	                itemStyle: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'}
	            },
	            labels: {
	                style: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'}
	            }
	        },
	        legend: {
	            layout: 'horizontal',
	            align: 'center',
	            y: 18,
	            itemWidth: 165,
	            itemStyle: {'color': '#859398', 'font-weight': 'normal', 'font-size': '8pt'},
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