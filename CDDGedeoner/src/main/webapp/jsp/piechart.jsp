<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
boolean visionado3D = false;
if (request.getAttribute(idseries+"piechartvisionado") == null){
	visionado3D = request.getParameter("visionado").contentEquals("3D");
}else{
	visionado3D = ((String)request.getAttribute(idseries+"piechartvisionado")).contentEquals("3D");
}
%>
<div id="<%=idseries%>piechart" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">

  Highcharts.chart('<%=idseries%>piechart', {
   chart: {
	   plotBorderWidth: 1,
       plotShadow: true,
	   type: 'pie',
	   backgroundColor: 'transparent',
       borderWidth: 0,
       options3d: {
           enabled: <%=visionado3D%>,
           alpha: 45,
           beta: 0
       },
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
    accessibility: {
        point: {
            valueSuffix: '%'
        }
    },    
    plotOptions: {
    	series: {
            fillOpacity: 0.4
        },
	    pie: {
	        allowPointSelect: true,
	        cursor: 'pointer',
	        depth: 35,
	        dataLabels: {
	            enabled: true,
                format: '<b>{point.name}</b>: {point.percentage:.2f} %'
	        }
	    }
    },     
    series: <%=request.getAttribute(idseries+"piechartseries")%>
  });

</script>
