<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>


<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>dualHistogram" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
Highcharts.setOptions({
    colors: ['#058DC7', '#50B432', '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4']
});
 		Highcharts.chart('<%=idseries%>dualHistogram', {
	        chart: {
	        	type: 'column',
	            zoomType: 'xy',
	            backgroundColor: 'transparent',
	        },
	        title: {
	            text: '<%=request.getAttribute(idseries+"dualHistogramtitle")%>',
	            style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '11pt'}
	        },
	        subtitle: {
	            text: '<%=request.getAttribute(idseries+"dualHistogramsubtitle")%>',
	            style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '9pt'}
	        },
	        xAxis: [{
	            categories: <%=request.getAttribute(idseries+"dualHistogramseries")%>,
	            labels: {
	                style: {
	                    color: '#606c88',
	                    fontSize:'small'
	                }
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
	            itemWidth: 155,
	            itemStyle: {'color': '#606c88', 'font-weight': 'normal', 'font-size': '8px'},
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
	            style: {'color': 'blue', 'font-weight': 'lighter'},
	            data: <%=request.getAttribute(idseries+"dualHistogramfrecAcum")%>,
	            tooltip: {
	                valueSuffix: '%'
	            }
	        }]
	    });
	
 </script>
