<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
boolean visionado3D = false;
if ((String)request.getAttribute(idseries+"dualHistogramvisionado") == null){
	visionado3D = request.getParameter("visionado").contentEquals("3D");
}else{
	visionado3D = ((String)request.getAttribute(idseries+"dualHistogramvisionado")).contentEquals("3D");
}
%>
<div id="<%=idseries%>dualHistogram" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<jsp:include page="manejadorPaleta.jsp"></jsp:include>

<script type="text/javascript">
 		Highcharts.chart('<%=idseries%>dualHistogram', {
	        chart: {
	        	type: 'column',
	            zoomType: 'xy',
	            backgroundColor: 'transparent',	
	            options3d: {
	            	enabled: <%=visionado3D%>,
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
