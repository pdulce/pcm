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
<div id="<%=idseries%>dualHistogram" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
var fontColor = '<%=fontColor_%>';
var itemColor = '<%=fontColor_%>';
Highcharts.setOptions({
	colors: [ '#06B5CA','#64E572', '#CFCECE', '#00607E', '#FCBF0A', '#FFF263', '#B3DFF2', '#6AF9C4', '#1A3B47']
});
 		Highcharts.chart('<%=idseries%>dualHistogram', {
	        chart: {
	        	type: 'column',
	            zoomType: 'xy',
	            backgroundColor: 'transparent',	
	            options3d: {
	                enabled: false,
	                alpha: 15,
	                beta: 15,
	                viewDistance: 25,
	                depth: 40
	            },
	            style: {
	            	 fontFamily: 'Roboto, sans-serif'   	 
	            }
	        },
	        title: {
	            text: '<%=request.getAttribute(idseries+"dualHistogramtitle")%>',
	            style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
	        },
	        subtitle: {
	            text: '<%=request.getAttribute(idseries+"dualHistogramsubtitle")%>',
	            style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
	        },
	        xAxis: [{
	            categories: <%=request.getAttribute(idseries+"dualHistogramseries")%>,
	            labels: {
	            	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
	            },
	            crosshair: true
	        }],
	        yAxis: [{ // Primary yAxis
	            labels: {
	                format: '{value} ',
	                style: {
	                    color: Highcharts.getOptions().colors[0]
	                }
	            },
	            allowDecimals: true,
	            min: <%=request.getAttribute(idseries+"dualHistogramminEjeRef")%>,
	            title: {
	                text: 'Frecuencia relativa',
	                style: {
	                    color: Highcharts.getOptions().colors[0]
	                }
	            }
	        }, { // Secondary yAxis
	            title: {
	                text: 'Frecuencia acumulada',
	                style: {
	                    color: Highcharts.getOptions().colors[1]
	                }
	            },
	            labels: {
	                format: '{value}%',
	                style: {
	                    color: Highcharts.getOptions().colors[1]
	                }
	            },
	            opposite: true
	        }],
	        tooltip: {
	            shared: true
	        },
	        legend: {
	            layout: 'horizontal',
	            align: 'center',
	            y: 18,
	            itemWidth: 165,
	            itemStyle: {'color': itemColor, 'font-weight': 'normal', 'font-size': '12px'},
	            verticalAlign: 'bottom'
	        },

	        series: [{
	        	name: 'Relative freq.',
	            type: 'column',
	            data: <%=request.getAttribute(idseries+"dualHistogramfrecAbsoluta")%>,
	            tooltip: {
	                valueSuffix: ' <%=request.getAttribute(idseries+"dualHistogramunits")%>'
	            }

	        }, {
	            name: 'Cumulative freq.',
	            type: 'spline',
	            yAxis: 1,
	            style: {'font-weight': 'normal', 'font-size': '12px'},
	            data: <%=request.getAttribute(idseries+"dualHistogramfrecAcum")%>,
	            tooltip: {
	                valueSuffix: '%'
	            }
	        }]
	    });
	
 </script>
