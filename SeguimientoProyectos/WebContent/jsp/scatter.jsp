<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
String defaultMode = (String)request.getAttribute("style");
String fontColor_ = defaultMode.contentEquals("darkmode") ? "#FFEFBA" : "#203A43";
String itemColor_ = defaultMode.contentEquals("darkmode") ? "#FFEFBA" : "#859398";

%>
<div id="<%=idseries%>scatter" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<script type="text/javascript">
var fontColor = '<%=fontColor_%>';
var itemColor = '<%=fontColor_%>';
Highcharts.setOptions({
	colors: [ '#06B5CA','#64E572', '#CFCECE', '#00607E', '#FCBF0A', '#FFF263', '#B3DFF2', '#6AF9C4', '#1A3B47']
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
	            style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
	        },
	        subtitle: {
	            text: '<%=request.getAttribute(idseries+"scattersubtitle")%>',
	            style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
	        },
	        xAxis: {
	            title: {	                
	                text: '<%=request.getAttribute(idseries+"scattertitulo_EJE_X")%>',
	                style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}	                
	            },
	            labels: {
	            	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
	            }
	        },
	        yAxis: {
	            title: {
	                text: '<%=request.getAttribute(idseries+"scattertitulo_EJE_Y")%>',
	                itemStyle: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
	            },
	            labels: {
	                style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
	            }
	        },
	        legend: {
	            layout: 'horizontal',
	            align: 'center',
	            y: 18,
	            itemWidth: 165,
	            itemStyle: {'color': itemColor, 'font-weight': 'normal', 'font-size': '8pt'},
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