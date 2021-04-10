<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>


<div id="dualHistogram" style="width: 1080px; height: 700px; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
	 		
 		Highcharts.chart('dualHistogram', {
	        chart: {
	        	type: 'column',
	            zoomType: 'xy',
	            backgroundColor: 'transparent'
	        },
	        title: {
	            text: '<%=request.getAttribute("dualHistogramtitle")%>',
	            style: {'color': 'orange', 'font-weight': 'lighter'}
	        },
	        subtitle: {
	            text: '<%=request.getAttribute("dualHistogramsubtitle")%>',
	            style: {'color': 'orange', 'font-weight': 'lighter'}
	        },
	        xAxis: [{
	            categories: <%=request.getAttribute("dualHistogramseries")%>,
	            labels: {
	                style: {
	                    color: 'orange',
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
	            min: <%=request.getAttribute("dualHistogramminEjeRef")%>,
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
	            width: 580,
	            floating: true,
	            align: 'left',
	            x: 110, // = marginLeft - default spacingLeft
	            y: 22,
	            itemWidth: 220,
	            itemStyle: {'color': 'orange', 'font-weight': 'light', 'font-size': '12px'},
	            itemDistance: 80,
	            borderWidth: 0
	        },

	        series: [{
	        	name: 'Relative freq.',
	            type: 'column',
	            data: <%=request.getAttribute("dualHistogramfrecAbsoluta")%>,
	            tooltip: {
	                valueSuffix: ' <%=request.getAttribute("dualHistogramunits")%>'
	            }

	        }, {
	            name: 'Cumulative freq.',
	            type: 'spline',
	            yAxis: 1,
	            style: {'color': 'blue', 'font-weight': 'lighter'},
	            data: <%=request.getAttribute("dualHistogramfrecAcum")%>,
	            tooltip: {
	                valueSuffix: '%'
	            }
	        }]
	    });
	
 </script>
