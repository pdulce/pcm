<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>spainmap" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<jsp:include page="manejadorPaleta.jsp"></jsp:include>

<script type="text/javascript">
	 
	   Highcharts.mapChart('<%=idseries%>spainmap', {
		    chart: {
		        map: 'countries/es/es-all',
	            backgroundColor: 'transparent'
		    },
	
		   title : {
			   text : '<%=request.getAttribute(idseries+"spainmaptitle")%>',
			   style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '11pt'}
		   },
				
		   subtitle : {
			   text : '<%=request.getAttribute(idseries+"spainmapsubtitle")%>',
			   style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '9pt'}
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
	            enabled: false,
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
	                enabled: false,
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
 	