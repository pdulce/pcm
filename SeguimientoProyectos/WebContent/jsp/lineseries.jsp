<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
	var chart = new Highcharts.Chart({
	    chart: {
	        renderTo: 'container',
	        type: 'line'
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
                    color: 'red',/*'#6E6E6E',*/
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
            itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'},
            itemDistance: 85,
            borderWidth: 0
        },
        tooltip: {				        	
            headerFormat: '<b>{point.key}</b><br>',
            pointFormat: '<span style="color:{series.color}">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute("decimals")%>}'
        },		
        plotOptions: {
            line: {
                dataLabels: {
                    enabled: true
                },
                enableMouseTracking: false
            }
        },
        series: <%=request.getAttribute("series")%>
	});
	
	
</script>
