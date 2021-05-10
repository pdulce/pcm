<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
//System.out.println("series-id: " + idseries);
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>timeseries" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<script type="text/javascript">
Highcharts.setOptions({
    colors: ['#2C5364', '#bdc3c7', '#7AA1D2', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4']
});
	Highcharts.chart('<%=idseries%>timeseries', {
	    chart: {     	
            type: '<%=request.getAttribute(idseries+"timeseriestypeOfSeries")%>',
            margin: 75,
            backgroundColor: 'transparent',
            style: {
                fontFamily: 'serif',
                fontColor: '#2c3e50'
            }
        },
        title: {
        	text: '<%=request.getAttribute(idseries+"timeseriestitle")%>',
            style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '11pt'}
        },
        subtitle: {
            text: '<%=request.getAttribute(idseries+"timeseriessubtitle")%>',
            style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '9pt'}
        },
        xAxis: {
            categories: <%=request.getAttribute(idseries+"timeseriesabscisas")%>,
            labels: {
                style: {
                    color: '#606c88',
                    fontSize:'xx-small'
                }
            }
        },
        yAxis: {
            min:  <%=request.getAttribute(idseries+"timeseriesminEjeRef")%>,
            allowDecimals: true,
            title: {
                text: '<%=request.getAttribute(idseries+"timeseriestitulo_EJE_Y")%>',
                style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': 'xx-small'}
            },
            labels: {
                style: {
                    color: '#606c88',
                    fontSize:'small'
                }
            }
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            itemStyle: {'color': '#606c88', 'font-weight': 'normal', 'font-size': '12px'},
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
