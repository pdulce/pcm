<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
   
   <script type="text/javascript">
	 
		$('#container').highcharts('Map', {
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
	            data : <%=request.getAttribute("json_countryMap")%>,
	            mapData: Highcharts.maps['countries/es/es-all'],
	            joinBy: 'hc-key',
	            name: '<%=request.getAttribute("entidadGrafico")%>',
	            states: {
	                hover: {
	                    color: '#EEDD66'
	                }
	            },
	            dataLabels: {
	                enabled: true,
	                format: '<b>{point.name}</b>: {point.value:<%=request.getAttribute("decimals")%>}',
	            }
	        }, {
	            name: 'Separators',
	            type: 'mapline',
	            data: Highcharts.geojson(Highcharts.maps['countries/es/es-all'], 'mapline'),
	            color: 'silver',
	            showInLegend: false,
	            enableMouseTracking: false
	        }]
	    });
 
 
 	</script>