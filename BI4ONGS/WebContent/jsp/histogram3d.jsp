

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">
	var chart = new Highcharts.Chart({
	    chart: {
	        renderTo: 'container',
	        type: 'column',
            backgroundColor: 'transparent',
	        options3d: {
	            enabled: true,
	            alpha: 15,
	            beta: 15,
	            depth: 50,
	            viewDistance: 25
	        }
	    },
        title: {
            text: '<%=request.getAttribute("title")%>',
            style: {'color': 'orange', 'font-weight': 'lighter'}
        },
        
        subtitle: {
            text: '<%=request.getAttribute("subtitle")%>',
            style: {'color': 'orange', 'font-weight': 'lighter'}
        },
        
        xAxis: {
            categories: <%=request.getAttribute("abscisas")%>,
            labels: {
                style: {
                    color: 'orange',
                    fontSize:'xx-small'
                }
            }
        },
        
        yAxis: {
            allowDecimals: true,
            min: <%=request.getAttribute("minEjeRef")%>,
            title: {
                text: '<%=request.getAttribute("titulo_EJE_Y")%>',
                style: {'color': 'orange', 'font-weight': 'lighter', 'font-size': 'xx-small'}
            },
            labels: {
                style: {
                    color: 'orange',
                    fontSize:'small'
                }
            }
        },    
        legend: {		            	
            width: 1080,
            floating: true,
            align: 'center',
            x: 95, // = marginLeft - default spacingLeft
            y: 22,
            itemWidth: 220,
            itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'},
            itemDistance: 85,
            borderWidth: 0
        },
        tooltip: {				        	
            headerFormat: '<b>{point.key}</b><br>',
            pointFormat: '<span style="color:{series.color}">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute("decimals")%>}'
        },
        				       
        plotOptions: {
            column: {
                depth: 25
            }
        },
        				      
        series: <%=request.getAttribute("series")%>
	});
	
	function showValues() {
	    $('#alpha-value').html(chart.options.chart.options3d.alpha);
	    $('#beta-value').html(chart.options.chart.options3d.beta);
	    $('#depth-value').html(chart.options.chart.options3d.depth);
	}

	// Activate the sliders
	$('#sliders input').on('input change', function () {
	    chart.options.chart.options3d[this.id] = parseFloat(this.value);
	    showValues();
	    chart.redraw(false);
	});

	showValues();
	
</script>
