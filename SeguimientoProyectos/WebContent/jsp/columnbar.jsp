<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
//System.out.println("seriestype: " +  idseries);
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>columnbar" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">

Highcharts.setOptions({
    colors: ['#2C5364', '#bdc3c7', '#7AA1D2', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4']
});


Highcharts.chart('<%=idseries%>columnbar', {
    chart: {
        type: '<%=request.getAttribute(idseries+"columnbartypeOfgraph")%>',
        backgroundColor: 'transparent',        
        options3d: {
            enabled: false,
            alpha: 15,
            beta: 15,
            viewDistance: 25,
            depth: 40
        }
    },

    title: {
        text: 'Dimensiones del Ciclo de Vida agrupadas por <%=request.getAttribute(idseries+"columnbaragrupadoPor")%>',
        style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '11pt'}
    },

    xAxis: {
        categories: <%=request.getAttribute(idseries+"columnbarcategories")%>,       
        labels: {
            skew3d: true,
            style: {                
            	 fontSize: '12px'
            }
        }
    },

    yAxis: {
        allowDecimals: false,
        min: 0,
        title: {
            text: 'Jornadas',
            skew3d: true
        }
    },

    tooltip: {
        headerFormat: '<b>{point.key}</b><br>',
        pointFormat: '<span style="color:{series.color}">\u25CF</span> {series.name}: {point.y} / {point.stackTotal}'
    },
    
    plotOptions: {    	
        column: {
            stacking: 'normal',
            depth: 40
        }
    },

    series: <%=request.getAttribute(idseries+"columnbarseries")%>
    
}); 
        
</script>
