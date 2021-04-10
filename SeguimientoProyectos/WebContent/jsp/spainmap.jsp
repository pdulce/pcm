<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>


<div id="spainmap" style="width: 1080px; height: 700px; margin: 0 auto;float:left;"></div>
   
<script type="text/javascript">
	 
	   Highcharts.mapChart('spainmap', {
		    chart: {
		        map: 'countries/es/es-all',
	            backgroundColor: 'transparent'
		    },
	
		   title : {
			   text : '<%=request.getAttribute("spainmaptitle")%>'
		   },
				
		   subtitle : {
			   text : '<%=request.getAttribute("spainmapsubtitle")%>'
		   },
		        			        
           legend: {
            	title: {
                    text: '<%=request.getAttribute("spainmapunits")%> <%=request.getAttribute("spainmapentidadGrafico")%>',
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
	        	data : <%=request.getAttribute("spainmapseries")%>,
	        	name: '<%=request.getAttribute("spainmapentidadGrafico")%>',
	            states: {
	                hover: {
	                    color: '#BADA55'
	                }
	            },
	            dataLabels: {
	                enabled: true,
	                format: '<b>{point.name}</b>: {point.value:<%=request.getAttribute("spainmapdecimals")%>}'
	            }
	        }, {
	            name: 'Separators',
	            type: 'mapline',
	            data: Highcharts.geojson(Highcharts.maps['countries/es/es-all'], 'mapline'),
	            color: 'silver',
	            nullColor: 'silver',
	            showInLegend: false,
	            enableMouseTracking: false
	        }]
	       
	    });
 
 	</script>
 	