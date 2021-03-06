<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>spiderweb" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<jsp:include page="manejadorPaleta.jsp"></jsp:include>

<script type="text/javascript">
  	Highcharts.chart('<%=idseries%>spiderweb', {
   		chart: {
	        polar: true,
	        type: 'line',
            backgroundColor: 'transparent',
            style: {
           	 fontFamily: 'Roboto, sans-serif'   	 
           }
	    },
        title: {
        	text: '<%=request.getAttribute(idseries+"spiderwebtitle")%>',
        	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'},
            x: -80
        },
        pane: {
            size: '80%'
        },
        subtitle: {
            text: '<%=request.getAttribute(idseries+"spiderwebsubtitle")%>',
            style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
        },
        xAxis : {
			categories : <%=request.getAttribute(idseries+"spiderwebcategories")%>,
			tickmarkPlacement: 'on',
		    lineWidth: 0,
		    labels: {
		    	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
            }
	    },
	    yAxis: {
	        gridLineInterpolation: 'polygon',
	        lineWidth: 0,
	        min: 0,
	        labels: {
	        	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
            }
	    },

	    tooltip: {
	        shared: true,
	        pointFormat: '<span style="color:{series.color}">{series.name}: {point.y:<%=request.getAttribute(idseries+"spiderwebdecimals")%>}'
	    },	    
		series : <%=request.getAttribute(idseries+"spiderwebseries")%>,
		responsive: {
	        rules: [{
	            condition: {
	                maxWidth: 500
	            },
	            chartOptions: {
	            	legend: {
	        	        layout: 'horizontal',
	        	        align: 'center',
	        	        y: 18,
	        	        itemWidth: 165,
	        	        itemStyle: {'color': itemColor, 'font-weight': 'normal', 'font-size': '8pt'},
	        	        verticalAlign: 'bottom'
	        	    },
	                pane: {
	                    size: '70%'
	                }
	            }
	        }]
	    }
	});
	
</script>




