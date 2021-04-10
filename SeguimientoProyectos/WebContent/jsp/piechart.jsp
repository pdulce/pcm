<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>piechart" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<script type="text/javascript">
	   
  Highcharts.chart('<%=idseries%>piechart', {
   chart: {	   
         backgroundColor: 'transparent',
         plotBackgroundColor: null,
         plotBorderWidth: null,
         plotShadow: true,
         type: 'pie'
     },
     title: {
         text: '<%=request.getAttribute(idseries+"piecharttitle")%>',
         style: {'color': 'orange', 'font-weight': 'lighter'}
    },
    subtitle: {
        text: '<%=request.getAttribute(idseries+"piechartsubtitle")%>',
        style: {'color': 'orange', 'font-weight': 'lighter'}
    },
    tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.2f} %</b>'
    },
    plotOptions: {
        pie: {
            allowPointSelect: true,
            cursor: 'pointer',
            dataLabels: {
                enabled: true,
                format: '<b>{point.name}: {point.percentage:.2f} %</b>',
                distance: -80,
                filter: {
                	property: 'percentage',
                	operator: '>',
                	value: '4'},
                style: {
                    color: 'orange',
                    fontSize: '14px',                        
                    fontStyle: 'normal'                                      
                },
                connectorColor: 'silver'
            }
        }
    },
    series: <%=request.getAttribute(idseries+"piechartseries")%>,        
 	legend: {
	 	enabled: true,
        	title: {
               text: '<%=request.getAttribute(idseries+"piechartentidadGrafico")%>',
               style: {
               color: (Highcharts.theme && Highcharts.theme.textColor) || 'orange'
                }
         },
        width: 840,
        floating: true,
        align: 'left',
        x: 90,
        y: 52,
        itemWidth: 50,
        borderWidth: 1
    }  			
  });

</script>
