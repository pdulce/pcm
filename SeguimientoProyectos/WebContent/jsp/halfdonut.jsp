<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>halfdonutchart" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<script type="text/javascript">
	   
  Highcharts.chart('<%=idseries%>halfdonutchart', {
   chart: {	   
         backgroundColor: 'transparent',
         type: 'container',
         borderWidth: 0
     },
     title: {
         text: '<%=request.getAttribute(idseries+"halfdonutcharttitle")%>',
         style: {'color': 'orange', 'font-weight': 'lighter', 'font-size': '11pt'}
    },
    subtitle: {
        text: '<%=request.getAttribute(idseries+"halfdonutchartsubtitle")%>',
        style: {'color': 'orange', 'font-weight': 'lighter', 'font-size': '9pt'}
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
