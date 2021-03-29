<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>


<script type="text/javascript">
	   
  	Highcharts.chart('container', {
   		chart: {
	        polar: true,
	        type: 'line',
            backgroundColor: 'transparent'
	    },
        title: {
        	text : '<%=request.getAttribute("title")%>',
        	style: {'color': 'orange', 'font-weight': 'lighter'},
            x: -80
        },
        pane: {
            size: '80%'
        },
        subtitle: {
            text: '<%=request.getAttribute("subtitle")%>',
            style: {'color': 'orange', 'font-weight': 'lighter'}
        },
        xAxis : {
			categories : <%=request.getAttribute("categories")%>,
			tickmarkPlacement: 'on',
		    lineWidth: 0,
		    labels: {
                style: {
                    color: 'orange',
                    fontSize:'small'
                }
            }
	    },
	    yAxis: {
	        gridLineInterpolation: 'polygon',
	        lineWidth: 0,
	        min: 0,
	        labels: {
                style: {
                    color: 'orange',
                    fontSize:'small'
                }
            }
	    },

	    tooltip: {
	        shared: true,
	        pointFormat: '<span style="color:{series.color}">{series.name}: {point.y:<%=request.getAttribute("decimals")%>}'
	    },

	    legend: {
	        align: 'right',
	        verticalAlign: 'middle',
	        layout: 'vertical',
	        itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'}
	    },
		series : <%=request.getAttribute("series")%>,
		responsive: {
	        rules: [{
	            condition: {
	                maxWidth: 500
	            },
	            chartOptions: {
	                legend: {
	                    align: 'center',
	                    verticalAlign: 'bottom',
	                    layout: 'horizontal'
	                },
	                pane: {
	                    size: '70%'
	                }
	            }
	        }]
	    }
	});
	
</script>




