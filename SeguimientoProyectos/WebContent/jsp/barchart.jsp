<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = (String)request.getAttribute("idseries");
String serieNumber= request.getParameter("series");
if (serieNumber!= null && !"".contentEquals(serieNumber)){
	idseries = idseries.concat(serieNumber);
}
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>barchart" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
			
			Highcharts.chart('<%=idseries%>barchart', {
			    chart: {
			        type: 'bar',
		            backgroundColor: 'transparent'
			    },
			    title: {
			        text: '<%=request.getAttribute(idseries+"barcharttitle")%>',
			        style: {'color': 'orange', 'font-weight': 'lighter'}
			    },
			    subtitle: {
		            text: '<%=request.getAttribute(idseries+"barchartsubtitle")%>',
		            style: {'color': 'orange', 'font-weight': 'lighter'}
		        },
			    xAxis: {
			        categories: <%=request.getAttribute(idseries+"barchartcategories")%>,
			        labels: {
		                style: {
		                    color: 'orange',
		                    fontSize:'small'
		                }
		            }
			    },
			    yAxis: {
			        min: 0,
			        itemStyle: {'color': 'orange', 'font-weight': 'lighter'},
			        title: {
			            text: '<%=request.getAttribute(idseries+"barcharttitulo_EJE_X")%>',
			            itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'}
			        },
			        labels: {
		                style: {
		                    color: 'orange',
		                    fontSize:'small'
		                }
		            }
			    },
			    legend: {
			        reversed: true,
			        itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'}
			    },
			    plotOptions: {
			        series: {
			            stacking: 'normal',
			            style: {'color': '#3f4c6b', 'font-weight': 'lighter', 'font-size': 'xx-small'}
			        }
			    },
				  series: <%=request.getAttribute(idseries+"barchartseries")%>
			    
			});
			
	</script>
