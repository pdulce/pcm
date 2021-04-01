<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
	
	Highcharts.chart('container', {
	    chart: {     	
            type: '<%=request.getAttribute("typeOfSeries")%>',
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
            title: {
                text: '<%=request.getAttribute("titulo_EJE_Y")%>',
                style: {'color': 'orange', 'font-weight': 'lighter', 'font-size': 'xx-small'}
            },
            labels: {
                style: {
                    color: 'orange',
                    fontSize:'small'
                }
            }
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'},
            verticalAlign: 'middle'
        },
        
        tooltip: {				        	
            headerFormat: '<b>{point.key}</b><br>',
            pointFormat: '<span style="font-size: xx-small; color: #3f4c6b">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute("decimals")%>}'
        },
        				       
        plotOptions: {
        	line: {
                dataLabels: {
                    enabled: true,
                    style: {'color': 'rgb(219, 202, 172)', 'font-weight': 'normal', 'font-size': 'xx-small'}
                },
                enableMouseTracking: false
            },
        	series: {
                borderWidth: 2,
                dataLabels: {
                    enabled: true,
                    style: {'color': 'white', 'font-size': 'xx-small'},
                    format: '{point.y:<%=request.getAttribute("decimals")%>}'
                },
                label: {
                    connectorAllowed: false
                }
            },
            column: {
                depth: 25,
                style: {'color': 'white', 'font-size': 'xx-small'},
                stacking: true,
                grouping: false,
                groupZPadding: 10
            }
        },
        
        series: <%=request.getAttribute("series")%>,
        
        responsive: {
            rules: [{
                condition: {
                    maxWidth: 500
                },
                chartOptions: {
                    legend: {
                        layout: 'horizontal',
                        align: 'center',
                        verticalAlign: 'bottom'
                    }
                }
            }]
        }
	 
	});						
				 				   

</script>
