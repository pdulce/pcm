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
<div id="<%=idseries%>columnbar" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<script type="text/javascript">
var fontColor = '<%=fontColor_%>';
var itemColor = '<%=fontColor_%>';

Highcharts.setOptions({
	colors: [ '#06B5CA','#64E572', '#CFCECE', '#00607E', '#FCBF0A', '#FFF263', '#B3DFF2', '#6AF9C4', '#1A3B47']
});

//new palette: '#a4a4a4', '#04b4cc', '#0484ac', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4'


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
        },
        style: {
        	 fontFamily: 'Roboto, sans-serif'   	 
        }
    },

    title: {
        text: 'Dimensiones del Ciclo de Vida agrupadas por <%=request.getAttribute(idseries+"columnbaragrupadoPor")%>',
        style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
    },

    xAxis: {
        categories: <%=request.getAttribute(idseries+"columnbarcategories")%>,       
        labels: {
            skew3d: true,
            style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
        }
    },

    yAxis: {
        allowDecimals: false,
        min: 0,
        title: {
            text: 'Jornadas',
            skew3d: true
        },
        style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
    },
    legend: {
        layout: 'horizontal',
        align: 'center',
        y: 17,
        itemWidth: 165,        
        verticalAlign: 'bottom',
        itemStyle: {'color': itemColor, 'font-weight': 'normal', 'font-size': '8pt'},
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
