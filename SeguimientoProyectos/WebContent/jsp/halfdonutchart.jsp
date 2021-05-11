<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>halfdonutchart" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
Highcharts.setOptions({
	colors: [ '#B3DFF2', '#06B5CA','#00607E', '#1A3B47', '#CFCECE','#FCBF0A','#64E572', '#FFF263', '#6AF9C4']
});

  Highcharts.chart('<%=idseries%>halfdonutchart', {
   chart: {	   
	   type: 'container',
	   backgroundColor: 'transparent',            
       borderWidth: 0,
       style: {
       	 fontFamily: 'Roboto, sans-serif'   	 
       }
     },
     title: {
         text: '<%=request.getAttribute(idseries+"halfdonutcharttitle")%>',
         align: 'center',
         verticalAlign: 'middle',
         y: 60,
         style: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'}
    },
    subtitle: {
        text: '',
        style: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'}
    },
    accessibility: {
        point: {
            valueSuffix: '%'
        }
    },
    legend: {
        layout: 'horizontal',
        align: 'center',        
        y: 18,
        itemWidth: 165,
        itemStyle: {'color': '#859398', 'font-weight': 'normal', 'font-size': '8pt'},
        verticalAlign: 'bottom'
    },
    tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.2f} %</b>'
    },
    plotOptions: {
        pie: {
        	dataLabels: {
                enabled: true,
                distance: -50,
                style: {
                    fontWeight: 'bold',
                    color: 'white'
                }
            },
            startAngle: -90,
            endAngle: 90,
            center: ['50%', '75%'],
            size: '110%'
        }
    },
    series: <%=request.getAttribute(idseries+"halfdonutchartseries")%>
  });

</script>
