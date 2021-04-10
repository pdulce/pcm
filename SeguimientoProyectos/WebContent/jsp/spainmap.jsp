<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = (String)request.getAttribute("idseries");
String serieNumber= request.getParameter("series");
if (serieNumber!= null && !"".contentEquals(serieNumber)){
	idseries = idseries.concat(serieNumber);
}
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>spainmap" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
	 
	   Highcharts.mapChart('<%=idseries%>spainmap', {
		    chart: {
		        map: 'countries/es/es-all',
	            backgroundColor: 'transparent'
		    },
	
		   title : {
			   text : '<%=request.getAttribute(idseries+"spainmaptitle")%>'
		   },
				
		   subtitle : {
			   text : '<%=request.getAttribute(idseries+"spainmapsubtitle")%>'
		   },
		        			        
           legend: {
            	title: {
                    text: '<%=request.getAttribute(idseries+"spainmapunits")%> <%=request.getAttribute(idseries+"spainmapentidadGrafico")%>',
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
	        	data : <%=request.getAttribute(idseries+"spainmapseries")%>,
	        	name: '<%=request.getAttribute(idseries+"spainmapentidadGrafico")%>',
	            states: {
	                hover: {
	                    color: '#BADA55'
	                }
	            },
	            dataLabels: {
	                enabled: true,
	                format: '<b>{point.name}</b>: {point.value:<%=request.getAttribute(idseries+"spainmapdecimals")%>}'
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
 	