<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
    
	<script type="text/javascript">
		
		$('#container').highcharts('Map', {			
		        title : {
		            text : ''
		        },
		
		        subtitle : {
		            text : ''
		        },
		        
	            legend: {
	            	title: {
	                    text: '<%=request.getAttribute("entidad")%>',
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
		        },
	        
	            colorAxis: {
	                min: 0,
	                minColor: '<%=request.getAttribute("minColor")%>',
	                maxColor: '<%=request.getAttribute("maxColor")%>'
	            },
	            
		        mapNavigation: {
		            enabled: true,
		            buttonOptions: {
		                verticalAlign: 'bottom'
		            }
		        },
		        
	        series : [{
	            data : <%=request.getAttribute("series")%>,
	            mapData: Highcharts.maps['custom/europe'],
	            joinBy: 'hc-key',
	            name: '<%=request.getAttribute("entidad")%>',
	            states: {
	                hover: {
	                    color: '#EEDD66'
	                }
	            },
	            dataLabels: {
	                enabled: true,
	                format: '<p style="font-style: italic;size=1.1em"><b>{point.name}</b>: {point.value:<%=request.getAttribute("decimals")%>} </p>',
	            }
	        }, {
		            name: 'Separators',
		            type: 'mapline',
		            data: Highcharts.geojson(Highcharts.maps['custom/europe'], 'mapline'),
		            color: 'silver',
		            showInLegend: false,
		            enableMouseTracking: false
		  }]
		        
	    });
 
 	</script>