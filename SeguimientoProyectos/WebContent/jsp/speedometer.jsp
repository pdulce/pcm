<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
String dimensionName = (String) request.getAttribute(idseries+"speedometerdimension");
String entidad = (String) request.getAttribute(idseries+"speedometerentidad");
String agregacion = (String) request.getAttribute(idseries+"speedometeragregacion");
%>
<div id="<%=idseries%>speedometer" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<jsp:include page="manejadorPaleta.jsp"></jsp:include>

<script type="text/javascript">

Highcharts.chart('<%=idseries%>speedometer', {
    chart: {
    	backgroundColor: 'transparent',
        type: 'gauge',
        plotBackgroundColor: null,
        plotBackgroundImage: null,
        plotBorderWidth: 0,
        borderWidth: 0,
        plotShadow: false,
        style: {
       	 fontFamily: 'Roboto, sans-serif'   	 
       },
       options3d: {
           enabled: false,
           alpha: 15,
           beta: 15,
           viewDistance: 25,
           depth: 40
       }
    },

    title: {
    	text: '<%=dimensionName%> <%=agregacion%> entre todas las <%=entidad%>',
    	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
    },
    
    subtitle: {
        text: '',
        style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
    },
    
    pane: {
        startAngle: -90,
        endAngle: 90,
        background: [{
            backgroundColor: {
                linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
                stops: [
                    [0, '#FFF'],
                    [1, '#333']
                ]
            },
            borderWidth: 0,
            outerRadius: '109%'
        }, {
            backgroundColor: {
                linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
                stops: [
                    [0, '#333'],
                    [1, '#FFF']
                ]
            },
            borderWidth: 1,
            outerRadius: '107%'
        }, {
            // default background
        }, {
            backgroundColor: '#DDD',
            borderWidth: 0,
            outerRadius: '105%',
            innerRadius: '103%'
        }]
    },

    // the value axis
    yAxis: {
        min: 0,
        max: 100,

        minorTickInterval: 'auto',
        minorTickWidth: 1,
        minorTickLength: 10,
        minorTickPosition: 'inside',
        minorTickColor: '#666',

        tickPixelInterval: 30,
        tickWidth: 2,
        tickPosition: 'inside',
        tickLength: 10,
        tickColor: '#666',
        labels: {
            step: 2,
            rotation: 'auto'
        },
        title: {
            text: '<%=request.getAttribute(idseries+"speedometerdato")%>',
            style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
        },
        plotBands: [{
            from: 0,
            to: 20,
            color: '#55BF3B' // green
        }, {
            from: 20,
            to: 50,
            color: '#DDDF0D' // #FFEFBA
        }, {
            from: 50,
            to: 100,
            color: '#DF5353' // red
        }]
    },
	
    series:  [{"data":[<%=request.getAttribute(idseries+"speedometerdato")%>],"name":"<%=dimensionName%>","tooltip":{"valueSuffix":" "}}]

},
// Add some life
function (chart) {
    if (!chart.renderer.forExport) {
        setInterval(function () {
            var point = chart.series[0].points[0],
                newVal,
                inc = 0;//Math.round((Math.random() - 0.5) * 20);

            newVal = point.y + inc;
            if (newVal < 0 || newVal > 200) {
                newVal = point.y - inc;
            }

            point.update(newVal);

        }, 30000);//ms de refresco
    }
});


</script>

