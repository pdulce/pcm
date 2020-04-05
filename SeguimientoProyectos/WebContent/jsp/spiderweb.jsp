<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
	
	Highcharts.chart('container', {
	    chart: {
	        polar: true,
	        type: 'line'
	    },
        title: {
        	text : '<%=request.getAttribute("title")%>',
            x: -80
        },
        pane: {
            size: '80%'
        },
        subtitle: {
            text: '<%=request.getAttribute("subtitle")%>'
        },
        xAxis : {
			categories : <%=request.getAttribute("categories")%>,
			tickmarkPlacement: 'on',
		    lineWidth: 0
	    },
	    yAxis: {
	        gridLineInterpolation: 'polygon',
	        lineWidth: 0,
	        min: 0
	    },
	    tooltip: {
	        shared: true,
	        pointFormat: '<span style="color:{series.color}">{series.name}: <b>${point.y:,.0f}</b><br/>'
	    },
	    legend: {
	        align: 'right',
	        verticalAlign: 'middle',
	        layout: 'vertical'
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

