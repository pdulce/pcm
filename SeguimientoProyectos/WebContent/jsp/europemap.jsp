<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
   
   <script type="text/javascript">
	 
	   Highcharts.mapChart('container', {
		    chart: {
		        map: 'custom/europe'
		    },
	
		   title : {
			   text : '<%=request.getAttribute("title")%>'
		   },
				
		   subtitle : {
			   text : '<%=request.getAttribute("subtitle")%>'
		   },
		        			        
           legend: {
            	title: {
                    text: '<%=request.getAttribute("units")%> <%=request.getAttribute("entidadGrafico")%>',
                    style: {
                        color: (Highcharts.theme && Highcharts.theme.textColor) || 'blue'
                    }
                },
	            width: 840,
	            floating: true,
	            align: 'left',
	            x: 90, // = marginLeft - default spacingLeft
	            y: 2,
	            itemWidth: 50,
	            borderWidth: 1
	        },
	            
	        mapNavigation: {
	            enabled: true,
	            buttonOptions: {
	                verticalAlign: 'bottom'
	            }
	        },

	        colorAxis: {
	            min: 0
	        },
	        
	        series: [{
	        	data : <%=request.getAttribute("series")%>,
	        	name: '<%=request.getAttribute("entidadGrafico")%>',
	        	 states: {
	                 hover: {
	                     color: '#BADA55'
	                 }
	             },
	             dataLabels: {
	                 enabled: true,
	                 format: '<b>{point.name}</b>: {point.value:<%=request.getAttribute("decimals")%>}'
	             }
	         }]
	            
	    });
 
 	</script>
 	