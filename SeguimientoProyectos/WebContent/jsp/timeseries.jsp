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
<div id="<%=idseries%>timeseries" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<script type="text/javascript">
	
	Highcharts.chart('<%=idseries%>timeseries', {
	    chart: {     	
            type: '<%=request.getAttribute(idseries+"timeseriestypeOfSeries")%>',
            margin: 75,
            backgroundColor: 'transparent'
        },
        title: {
            text: '<%=request.getAttribute(idseries+"timeseriestitle")%>',
            style: {'color': 'orange', 'font-weight': 'lighter'}
        },
        subtitle: {
            text: '<%=request.getAttribute(idseries+"timeseriessubtitle")%>',
            style: {'color': 'orange', 'font-weight': 'lighter'}
        },
        xAxis: {
            categories: <%=request.getAttribute(idseries+"timeseriesabscisas")%>,
            labels: {
                style: {
                    color: 'orange',
                    fontSize:'xx-small'
                }
            }
        },
        yAxis: {
            min:  <%=request.getAttribute(idseries+"timeseriesminEjeRef")%>,
            allowDecimals: true,
            title: {
                text: '<%=request.getAttribute(idseries+"timeseriestitulo_EJE_Y")%>',
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
            layout: 'vertical',
            align: 'right',
            itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'},
            verticalAlign: 'middle'
        },
        
        tooltip: {				        	
            headerFormat: '<b>{point.key}</b><br>',
            pointFormat: '<span style="font-size: xx-small; color: #3f4c6b">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute(idseries+"timeseriesdecimals")%>}'
        },
        				       
        plotOptions: {
        	line: {
                dataLabels: {
                    enabled: true,
                    style: {'color': 'rgb(219, 202, 172)', 'font-weight': 'normal', 'font-size': 'xx-small'}
                },
                enableMouseTracking: false
            },
        	series: {
                borderWidth: 2,
                dataLabels: {
                    enabled: true,
                    style: {'color': 'white', 'font-size': 'xx-small'},
                    format: '{point.y:<%=request.getAttribute(idseries+"timeseriesdecimals")%>}'
                },
                label: {
                    connectorAllowed: false
                }
            },
            column: {
                depth: 25,
                style: {'color': 'white', 'font-size': 'xx-small'},
                stacking: true,
                grouping: false,
                groupZPadding: 10
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
                        align: 'center',
                        verticalAlign: 'bottom'
                    }
                }
            }]
        }
	 
	});						
				 				   

</script>
