<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
boolean visionado3D = ((String) request.getParameter("visionado")).contentEquals("3D");
%>
<div id="<%=idseries%>piechart" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<jsp:include page="manejadorPaleta.jsp"></jsp:include>

<script type="text/javascript">

  Highcharts.chart('<%=idseries%>piechart', {
   chart: {	   
	   type: 'pie',
	   backgroundColor: 'transparent',
       options3d: {
    	   enabled: <%=visionado3D%>,
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
