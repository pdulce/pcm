<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>piechart" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<script type="text/javascript">
Highcharts.setOptions({
    colors: ['#2C5364', '#bdc3c7', '#7AA1D2', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4']
});

  Highcharts.chart('<%=idseries%>piechart', {
   chart: {	   
         backgroundColor: 'transparent',
         type: 'pie',
         borderWidth: 0,
     },
     title: {
         text: '<%=request.getAttribute(idseries+"piecharttitle")%>',
         style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '11pt'}
    },
    subtitle: {
        text: '<%=request.getAttribute(idseries+"piechartsubtitle")%>',
        style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '9pt'}
    },
    legend: {
        layout: 'horizontal',
        align: 'center',
        y: 18,
        itemWidth: 155,
        itemStyle: {'color': '#606c88', 'font-weight': 'normal', 'font-size': '8px'},
        verticalAlign: 'bottom'
    },
    tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.2f} %</b>'
    },
    series: <%=request.getAttribute(idseries+"piechartseries")%>
  });

</script>
