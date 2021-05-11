<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>barchart" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
Highcharts.setOptions({
	colors: [ '#B3DFF2', '#06B5CA','#00607E', '#1A3B47', '#CFCECE','#FCBF0A','#64E572', '#FFF263', '#6AF9C4']
});
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
			        style: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'}
			    },
			    subtitle: {
		            text: '<%=request.getAttribute(idseries+"barchartsubtitle")%>',
		            style: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'}
		        },
			    xAxis: {
			        categories: <%=request.getAttribute(idseries+"barchartcategories")%>,
			        labels: {
			        	style: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'}
		            }
			    },
			    yAxis: {
			        min: 0,
			        itemStyle: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'},
			        title: {
			            text: '<%=request.getAttribute(idseries+"barcharttitulo_EJE_X")%>',
			            itemStyle: {'color': '#859398', 'font-weight': 'normal', 'font-size': '10pt'}
			        },
			        labels: {
			        	style: {'color': '#203A43', 'font-weight': 'normal', 'font-size': '10pt'}
		            }
			    },
			    legend: {
			        layout: 'horizontal',
			        align: 'center',
			        y: -20,
			        itemWidth: 165,
			        itemStyle: {'color': '#859398', 'font-weight': 'normal', 'font-size': '8pt'},
			        verticalAlign: 'bottom'
			    },
			    plotOptions: {
			        series: {
			            stacking: 'normal',
			            style: {'color': '#bdc3c7', 'font-weight': 'normal', 'font-size': '8pt'}
			        }
			    },
				  series: <%=request.getAttribute(idseries+"barchartseries")%>
			    
			});
			
	</script>
