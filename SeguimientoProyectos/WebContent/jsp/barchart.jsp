<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>barchart" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<jsp:include page="manejadorPaleta.jsp"></jsp:include>

<script type="text/javascript">
	Highcharts.chart('<%=idseries%>barchart', {
	    chart: {
	        type: 'bar',
            backgroundColor: 'transparent',
            options3d: {
                enabled: false,
                alpha: 15,
                beta: 15,
                viewDistance: 25,
                depth: 40
            },
            style: {
           	 fontFamily: 'Roboto, sans-serif'   	 
           }
	    },
	    title: {
	        text: '<%=request.getAttribute(idseries+"barcharttitle")%>',
	        style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
	    },
	    subtitle: {
            text: '<%=request.getAttribute(idseries+"barchartsubtitle")%>',
            style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
        },
	    xAxis: {
	        categories: <%=request.getAttribute(idseries+"barchartcategories")%>,
	        labels: {
	        	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
            }
	    },
	    yAxis: {
	        min: 0,
	        itemStyle: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'},
	        title: {
	            text: '<%=request.getAttribute(idseries+"barcharttitulo_EJE_X")%>',
	            itemStyle: {'color': itemColor, 'font-weight': 'normal', 'font-size': '10pt'}
	        },
	        labels: {
	        	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
            }
	    },
	    legend: {
	        layout: 'horizontal',
	        align: 'center',
	        y: -20,
	        itemWidth: 165,
	        itemStyle: {'color': itemColor, 'font-weight': 'normal', 'font-size': '8pt'},
	        verticalAlign: 'bottom'
	    },
	    plotOptions: {
	        series: {
	            stacking: 'normal',
	            style: {'font-weight': 'normal', 'font-size': '8pt'}
	        }
	    },
		  series: <%=request.getAttribute(idseries+"barchartseries")%>
	    
	});
	
</script>
