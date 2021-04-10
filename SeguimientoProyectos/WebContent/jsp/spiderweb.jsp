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
<div id="<%=idseries%>spiderweb" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<script type="text/javascript">
	   
  	Highcharts.chart('<%=idseries%>spiderweb', {
   		chart: {
	        polar: true,
	        type: 'line',
            backgroundColor: 'transparent'
	    },
        title: {
        	text : '<%=request.getAttribute(idseries+"spiderwebtitle")%>',
        	style: {'color': 'orange', 'font-weight': 'lighter'},
            x: -80
        },
        pane: {
            size: '80%'
        },
        subtitle: {
            text: '<%=request.getAttribute(idseries+"spiderwebsubtitle")%>',
            style: {'color': 'orange', 'font-weight': 'lighter'}
        },
        xAxis : {
			categories : <%=request.getAttribute(idseries+"spiderwebcategories")%>,
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
	        pointFormat: '<span style="color:{series.color}">{series.name}: {point.y:<%=request.getAttribute(idseries+"spiderwebdecimals")%>}'
	    },

	    legend: {
	        align: 'right',
	        verticalAlign: 'middle',
	        layout: 'vertical',
	        itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'}
	    },
		series : <%=request.getAttribute(idseries+"spiderwebseries")%>,
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




