<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
    
	function showValues() {
	    $('#R0-value').html(chart.options.chart.options3d.alpha);
	    $('#R1-value').html(chart.options.chart.options3d.beta);
	}

	Highcharts.chart('container', {
	    chart: {      	
	            type: 'column',
	            margin: 75,
	            options3d: {
	                enabled: true,
	                alpha: 5,
	                beta: <%=request.getAttribute("profundidad")%>,
	                depth: 90,
	                viewDistance: 20
	            }
	        },
	        title: {
	            text: '<%=request.getAttribute("title")%>'
	        },
	        
	        subtitle: {
	            text: '<%=request.getAttribute("subtitle")%>'
	        },
	        
	        xAxis: {
	            categories: <%=request.getAttribute("abscisas")%>,
	            labels: {
	                style: {
	                    color: '#6E6E6E',
	                    fontSize:'small'
	                }
	            }
	        },
	        
	        yAxis: {
	            allowDecimals: true,
	            min: <%=request.getAttribute("minEjeRef")%>,
	            title: {
	                text: '<%=request.getAttribute("titulo_EJE_Y")%>'
	            }
	        },    
	        legend: {		            	
	            width: 1080,
	            floating: true,
	            align: 'center',
	            x: 95, // = marginLeft - default spacingLeft
	            y: 9,
	            itemWidth: 220,
	            itemStyle: {'color': 'black', 'font-weight': 'normal', 'font-size': '12px'},
	            itemDistance: 85,
	            borderWidth: 0
	        },
	        tooltip: {				        	
	            headerFormat: '<b>{point.key}</b><br>',
	            pointFormat: '<span style="color:{series.color}">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute("decimals")%>}'
	        },
	        				       
	        plotOptions: {
	        	series: {
	                borderWidth: 2,
	                dataLabels: {
	                    enabled: true,
	                    style: {'color': '#610B5E', 'font-weight': 'bold', 'font-size': '10px'},
	                    format: '{point.y:<%=request.getAttribute("decimals")%>}'
	                }
	            },
	            column: {
	                depth: 25,
	                stacking: true,
	                grouping: false,
	                groupZPadding: 10
	            }
	        },
	        				      
	        series: <%=request.getAttribute("series")%>
	});
    // Activate the sliders
    $('#R0').on('change', function () {
        chart.options.chart.options3d.alpha = this.value;
        showValues();
        chart.redraw(false);
    });
    $('#R1').on('change', function () {
        chart.options.chart.options3d.beta = this.value;
        showValues();
        chart.redraw(false);
    });

    showValues();
	
</script>
