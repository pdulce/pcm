<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
//System.out.println("series-id: " + idseries);
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");

String defaultMode = (String)request.getAttribute("style");
String fontColor_ = defaultMode.contentEquals("darkmode") ? "yellow" : "#203A43";
String itemColor_ = defaultMode.contentEquals("darkmode") ? "yellow" : "#859398";

%>
<div id="<%=idseries%>timeseries" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<script type="text/javascript">
var fontColor = '<%=fontColor_%>';
var itemColor = '<%=fontColor_%>';
Highcharts.setOptions({
	colors: [ '#06B5CA','#64E572', '#CFCECE', '#00607E', '#FCBF0A', '#FFF263', '#B3DFF2', '#6AF9C4', '#1A3B47']
});
	Highcharts.chart('<%=idseries%>timeseries', {
	    chart: {     	
            type: '<%=request.getAttribute(idseries+"timeseriestypeOfSeries")%>',
            margin: 75,
            backgroundColor: 'transparent',
            options3d: {
                enabled: false,
                alpha: 15,
                beta: 15,
                viewDistance: 25,
                depth: 40
            },
            style: {
           	 fontFamily: 'Roboto, sans-serif'   	 
           }
        },
        title: {
        	text: '<%=request.getAttribute(idseries+"timeseriestitle")%>',
        	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
        },
        subtitle: {
            text: '<%=request.getAttribute(idseries+"timeseriessubtitle")%>',
            style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
        },
        xAxis: {
            categories: <%=request.getAttribute(idseries+"timeseriesabscisas")%>,
            labels: {
            	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '9pt'}
            }
        },
        yAxis: {
            min:  <%=request.getAttribute(idseries+"timeseriesminEjeRef")%>,
            allowDecimals: true,
            title: {
                text: '<%=request.getAttribute(idseries+"timeseriestitulo_EJE_Y")%>',
                style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
            },
            labels: {
            	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
            }
        },
        legend: {
	        layout: 'horizontal',
	        align: 'center',
	        y: 18,
	        itemWidth: 240,
	        itemStyle: {'color': itemColor, 'font-weight': 'normal', 'font-size': '8pt'},
	        verticalAlign: 'bottom'
	    },
        tooltip: {				        	
            headerFormat: '<b>{point.key}</b><br>',
            pointFormat: '<span style="font-size: xx-small;">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute(idseries+"timeseriesdecimals")%>}'
        },

        plotOptions: {
            area: {                
                marker: {
                    radius: 2
                },
                lineWidth: 1,
                states: {
                    hover: {
                        lineWidth: 1
                    }
                },
                threshold: null
            }
        },

        series: <%=request.getAttribute(idseries+"timeseriesseries")%>,
        
        responsive: {
            rules: [{
                condition: {
                    maxWidth: 500
                },
                chartOptions: {
                	legend: {
                        layout: 'horizontal',
                        align: 'bottom',
                        x: 20,
                        y: 8,
                        itemWidth: 165,
                        itemStyle: {'color': itemColor, 'font-weight': 'normal', 'font-size': '8pt'},
                        verticalAlign: 'bottom'
                    },
                }
            }]
        }
	 
	});						
				 				   

</script>
