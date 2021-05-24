<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
String defaultMode = (String)request.getAttribute("style");
String fontColor_ = defaultMode.contentEquals("darkmode") ? "yellow" : "#203A43";
String itemColor_ = defaultMode.contentEquals("darkmode") ? "yellow" : "#859398";

%>
<div id="<%=idseries%>piechart" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<script type="text/javascript">
var fontColor = '<%=fontColor_%>';
var itemColor = '<%=fontColor_%>';
Highcharts.setOptions({
	colors: [ '#06B5CA','#64E572', '#CFCECE', '#00607E', '#FCBF0A', '#FFF263', '#B3DFF2', '#6AF9C4', '#1A3B47']
});

  Highcharts.chart('<%=idseries%>piechart', {
   chart: {	   
	   type: 'pie',
	   backgroundColor: 'transparent',
       options3d: {
           enabled: false,
           alpha: 15,
           beta: 15,
           viewDistance: 25,
           depth: 40
       },         
       borderWidth: 0,
       style: {
          fontFamily: 'Roboto, sans-serif'   	 
       }
     },
     title: {
         text: '<%=request.getAttribute(idseries+"piecharttitle")%>',
         style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
    },
    subtitle: {
        text: '<%=request.getAttribute(idseries+"piechartsubtitle")%>',
        style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
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
     
    series: <%=request.getAttribute(idseries+"piechartseries")%>
        

</script>
