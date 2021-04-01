<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>


<script type="text/javascript">
	   
  Highcharts.chart('container', {
   chart: {	   
         backgroundColor: 'transparent',
         plotBackgroundColor: null,
         plotBorderWidth: null,
         plotShadow: true,
         type: 'pie'
     },
     title: {
         text: '<%=request.getAttribute("title")%>',
         style: {'color': 'orange', 'font-weight': 'lighter'}
    },
    subtitle: {
        text: '<%=request.getAttribute("subtitle")%>',
        style: {'color': 'orange', 'font-weight': 'lighter'}
    },
    tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.2f} %</b>'
    },
    plotOptions: {
        pie: {
            allowPointSelect: true,
            cursor: 'pointer',
            dataLabels: {
                enabled: true,
                format: '<b>{point.name}: {point.percentage:.2f} %</b>',
                distance: -80,
                filter: {
                	property: 'percentage',
                	operator: '>',
                	value: '4'},
                style: {
                    color: 'orange',
                    fontSize: '14px',                        
                    fontStyle: 'normal'                                      
                },
                connectorColor: 'silver'
            }
        }
    },
    series: <%=request.getAttribute("series")%>,        
 	legend: {
	 	enabled: true,
        	title: {
               text: '<%=request.getAttribute("units")%> <%=request.getAttribute("entidadGrafico")%>',
               style: {
               color: (Highcharts.theme && Highcharts.theme.textColor) || 'orange'
                }
         },
        width: 840,
        floating: true,
        align: 'left',
        x: 90,
        y: 2,
        itemWidth: 50,
        borderWidth: 1
    }  			
  });

</script>
