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
            text: '<%=request.getAttribute("title")%>',
            style: {'color': 'orange', 'font-weight': 'lighter'}
        },
        subtitle: {
            text: '<%=request.getAttribute("subtitle")%>',
            style: {'color': 'orange', 'font-weight': 'lighter'}
        },
        xAxis: {
            categories: <%=request.getAttribute("abscisas")%>,
            labels: {
                style: {
                    color: 'orange',
                    fontSize:'xx-small'
                }
            }
        },
        yAxis: {
            min:  <%=request.getAttribute("minEjeRef")%>,
            allowDecimals: true,
            style: {'color': '#3f4c6b', 'font-weight': 'lighter', 'font-size': 'xx-small'},
            title: {
                text: '<%=request.getAttribute("titulo_EJE_Y")%>',
                style: {'color': 'orange', 'font-weight': 'lighter'}
            }
        },
        
        legend: {		            	
            width: 1080,
            floating: true,
            align: 'center',
            x: 80, // = marginLeft - default spacingLeft
            y: 9,
            itemWidth: 260,
            itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'},
            itemDistance: 20,
            borderWidth: 0
        },
        
        tooltip: {				        	
            headerFormat: '<b>{point.key}</b><br>',
            pointFormat: '<span style="font-size: xx-small; color: #3f4c6b">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute("decimals")%>}'
        },
        				       
        plotOptions: {
        	line: {
                dataLabels: {
                    enabled: true,
                    style: {'color': '#3f4c6b', 'font-weight': 'lighter', 'font-size': 'xx-small'}
                },
                enableMouseTracking: false
            },
        	series: {
                borderWidth: 2,
                dataLabels: {
                    enabled: true,
                    style: {'color': '#3f4c6b', 'font-weight': 'lighter', 'font-size': 'xx-small'},
                    format: '{point.y:<%=request.getAttribute("decimals")%>}'
                }
            },
            column: {
                depth: 25,
                style: {'color': '#3f4c6b', 'font-weight': 'lighter', 'font-size': 'xx-small'},
                stacking: true,
                grouping: false,
                groupZPadding: 10
            }
        },
        				      
        series: <%=request.getAttribute("series")%>
	 
	});						
				 				   

</script>
