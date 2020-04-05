<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>


<script type="text/javascript">
	   
   Highcharts.getOptions().colors = Highcharts.map(Highcharts.getOptions().colors, 
   		function (color) {
	        return {
	            radialGradient: {
	                cx: 0.5,
	                cy: 0.3,
	                r: 0.7
	            },
	            stops: [
	                [0, color],
	                [1, Highcharts.Color(color).brighten(-0.3).get('rgb')] // darken
	            ]
	        };
   	});
   		
  Highcharts.chart('container', {
   chart: {
         plotBackgroundColor: null,
         plotBorderWidth: null,
         plotShadow: true,
         type: 'pie'
     },
     title: {
         text: '<%=request.getAttribute("title")%>'
    },
    subtitle: {
        text: '<%=request.getAttribute("subtitle")%>'
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
                    color: 'black',
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
               color: (Highcharts.theme && Highcharts.theme.textColor) || 'black'
                }
         },
        width: 840,
        floating: true,
        align: 'left',
        x: 90, // = marginLeft - default spacingLeft
        y: 2,
        itemWidth: 50,
        borderWidth: 1
    }  			
  });

</script>
