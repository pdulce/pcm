<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
	
	Highcharts.chart('container', {
	    chart: {     	
            type: 'line',
            margin: 75,
            backgroundColor: 'transparent'
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
                    fontSize:'xx-small'
                }
            }
        },
        yAxis: {
            min:  <%=request.getAttribute("minEjeRef")%>,
            allowDecimals: true,
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
            pointFormat: '<span style="font-size: xx-small; color:{series.color}">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute("decimals")%>}'
        },
        				       
        plotOptions: {
        	line: {
                dataLabels: {
                    enabled: true
                },
                enableMouseTracking: false
            },
        	series: {
                borderWidth: 2,
                dataLabels: {
                    enabled: true,
                    style: {'color': '#e6e6ff', 'font-weight': 'lighter', 'font-size': 'xx-small'},
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
				 				   

</script>
