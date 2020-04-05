<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
	 	$(function () {
	 		
	 		Highcharts.chart('container', {
		        chart: {
		        	type: 'column',
		            zoomType: 'xy'
		        },
		        title: {
		            text: '<%=request.getAttribute("title")%>'
		        },
		        subtitle: {
		            text: '<%=request.getAttribute("subtitle")%>'
		        },
		        xAxis: [{
		            categories: <%=request.getAttribute("json_dualHistogram")%>,
		            labels: {
		                style: {
		                    color: '#6E6E6E',
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
		            width: 560,
		            floating: true,
		            align: 'left',
		            x: 170, // = marginLeft - default spacingLeft
		            y: 17,
		            itemWidth: 180,
		            itemStyle: {'color': 'black', 'font-weight': 'bold', 'font-size': '12px'},
		            itemDistance: 80,
		            borderWidth: 0
		        },

		        series: [{
		        	name: 'Frecuencia relativa',
		            type: 'column',
		            data: <%=request.getAttribute("frecAbsoluta")%>,
		            tooltip: {
		                valueSuffix: ' <%=request.getAttribute("units")%>'
		            }

		        }, {
		            name: 'Frecuencia acumulada',
		            type: 'spline',
		            yAxis: 1,
		            data: <%=request.getAttribute("frecAcum")%>,
		            tooltip: {
		                valueSuffix: '%'
		            }
		        }]
		    });
		}); 
	 </script>
