<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
 
 <% 
 String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
 String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
 String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>worldmap" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>
 
 <script type="text/javascript">
	 
   $(function () {
	    
	    $('#stateCode').on('change', function () {
	        data[99].value = Number(this.value);
	        chart = Highcharts.charts[0];
	        chart.series[0].setData(data, true);
	        chart.redraw();
	    });
	   
	    // Initiate the chart
	    Highcharts.mapChart('<%=idseries%>worldmap', {

	    	title : {
				text : '<%=request.getAttribute(idseries+"worldmaptitle")%>'
			},
					
			subtitle : {
				text : '<%=request.getAttribute(idseries+"worldmapsubtitle")%>'
			},
	        
	        legend: {
            	title: {
                    text: '<%=request.getAttribute(idseries+"worldmapunits")%> <%=request.getAttribute(idseries+"worldmapentidadGrafico")%>',
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

	        series : [{
	        	data : <%=request.getAttribute(idseries+"worldmapseries")%>,
	            mapData: Highcharts.maps['custom/world'],
	            joinBy: 'hc-key',
	            name: '<%=request.getAttribute(idseries+"worldmapentidadGrafico")%>',
	            states: {
	                hover: {
	                    color: '#BADA55'
	                }
	            },
	            dataLabels: {
	                enabled: true,
	                format: '<b>{point.name}</b>: {point.value:<%=request.getAttribute(idseries+"worldmapdecimals")%>}'
	            }
	        }]
	    });
	});
 
</script>
 	 	