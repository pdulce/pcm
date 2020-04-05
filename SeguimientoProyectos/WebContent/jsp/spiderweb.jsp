<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type='text/javascript'>//<![CDATA[ 
	$(function () {
		Highcharts.setOptions({
		    lang: {
		        decimalPoint: ',',
		        thousandsSep: '.'
		    }
		});
		
		Highcharts.chart('container', {
		    chart: {
      			polar: true,
      			type : 'line'
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
			labels: {
                style: {
                    color: '#6E6E6E',
                    fontSize:'xx-small'
                }
            },
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
            pointFormat: '<span style="color:{series.color}">{series.name}: <b>{point.y} </b><br/>'
					},
			legend: {		            	
	            width: 1080,
	            floating: true,
	            align: 'center',
	            x: 95, // = marginLeft - default spacingLeft
	            y: 9,
	            itemWidth: 220,
	            itemStyle: {'color': 'black', 'font-weight': 'normal', 'font-size': '12px'},
	            itemDistance: 85,
	            borderWidth: 0
	        },

	series : <%=request.getAttribute("json_spiderweb")%>

    });
});
 			//]]>  	
</script>

