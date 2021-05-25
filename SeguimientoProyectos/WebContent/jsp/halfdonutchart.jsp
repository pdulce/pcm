<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
String defaultMode = (String)request.getAttribute("style");
String fontColor_ = defaultMode.contentEquals("darkmode") ? "#FFEFBA" : "#203A43";
String itemColor_ = defaultMode.contentEquals("darkmode") ? "#FFEFBA" : "#859398";

%>
<div id="<%=idseries%>halfdonutchart" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
var fontColor = '<%=fontColor_%>';
var itemColor = '<%=fontColor_%>';
Highcharts.setOptions({
	colors: [ '#06B5CA','#64E572', '#CFCECE', '#00607E', '#FCBF0A', '#FFF263', '#B3DFF2', '#6AF9C4', '#1A3B47']
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
         style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
    },
    subtitle: {
        text: '',
        style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
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
        itemStyle: {'color': itemColor, 'font-weight': 'normal', 'font-size': '8pt'},
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
                    fontWeight: 'bold'
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
