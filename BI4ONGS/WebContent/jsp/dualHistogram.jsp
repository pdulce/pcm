<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
	 		
 		Highcharts.chart('container', {
	        chart: {
	        	type: 'column',
	            zoomType: 'xy',
	            backgroundColor: 'transparent'
	        },
	        title: {
	            text: '<%=request.getAttribute("title")%>',
	            style: {'color': 'orange', 'font-weight': 'lighter'}
	        },
	        subtitle: {
	            text: '<%=request.getAttribute("subtitle")%>',
	            style: {'color': 'orange', 'font-weight': 'lighter'}
	        },
	        xAxis: [{
	            categories: <%=request.getAttribute("series")%>,
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
	            min: <%=request.getAttribute("minEjeRef")%>,
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
	            data: <%=request.getAttribute("frecAbsoluta")%>,
	            tooltip: {
	                valueSuffix: ' <%=request.getAttribute("units")%>'
	            }

	        }, {
	            name: 'Cumulative freq.',
	            type: 'spline',
	            yAxis: 1,
	            style: {'color': 'blue', 'font-weight': 'lighter'},
	            data: <%=request.getAttribute("frecAcum")%>,
	            tooltip: {
	                valueSuffix: '%'
	            }
	        }]
	    });
	
 </script>
