<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% String idseries = (String)request.getAttribute("idseries");
String width = (String)request.getAttribute("width");
String height = (String)request.getAttribute("height");
%>
<div id="europemap" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>
   
<script type="text/javascript">
	 
	   Highcharts.mapChart('europemap', {
		    chart: {
		        map: 'custom/europe',
	            backgroundColor: 'transparent'
		    },
	
		   title : {
			   text : '<%=request.getAttribute(idseries+"europemaptitle")%>'
		   },
				
		   subtitle : {
			   text : '<%=request.getAttribute(idseries+"europemapsubtitle")%>'
		   },
		        			        
           legend: {
            	title: {
                    text: '<%=request.getAttribute(idseries+"europemapunits")%> <%=request.getAttribute(idseries+"europemapentidadGrafico")%>',
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
	        	data : <%=request.getAttribute(idseries+"europemapseries")%>,
	        	name: '<%=request.getAttribute(idseries+"europemapentidadGrafico")%>',
	        	 states: {
	                 hover: {
	                     color: '#BADA55'
	                 }
	             },
	             dataLabels: {
	                 enabled: true,
	                 format: '<b>{point.name}</b>: {point.value:<%=request.getAttribute(idseries+"decimals")%>}'
	             }
	         }]
	            
	    });
 
 	</script>
 	