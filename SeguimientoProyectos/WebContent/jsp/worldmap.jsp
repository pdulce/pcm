<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
 
 
 <div id="worldmap"></div>
   
 <script type="text/javascript">
	 
   $(function () {
	    
	    $('#stateCode').on('change', function () {
	        data[99].value = Number(this.value);
	        chart = Highcharts.charts[0];
	        chart.series[0].setData(data, true);
	        chart.redraw();
	    });
	   
	    // Initiate the chart
	    Highcharts.mapChart('worldmap', {

	    	title : {
				text : '<%=request.getAttribute("worldmaptitle")%>'
			},
					
			subtitle : {
				text : '<%=request.getAttribute("worldmapsubtitle")%>'
			},
	        
	        legend: {
            	title: {
                    text: '<%=request.getAttribute("worldmapunits")%> <%=request.getAttribute("worldmapentidadGrafico")%>',
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
	        	data : <%=request.getAttribute("worldmapseries")%>,
	            mapData: Highcharts.maps['custom/world'],
	            joinBy: 'hc-key',
	            name: '<%=request.getAttribute("worldmapentidadGrafico")%>',
	            states: {
	                hover: {
	                    color: '#BADA55'
	                }
	            },
	            dataLabels: {
	                enabled: true,
	                format: '<b>{point.name}</b>: {point.value:<%=request.getAttribute("worldmapdecimals")%>}'
	            }
	        }]
	    });
	});
 
</script>
 	 	