<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
boolean visionado3D = false;
if (request.getAttribute(idseries+"histogramvisionado") == null){
	visionado3D = request.getParameter("visionado").contentEquals("3D");
}else{
	visionado3D = ((String)request.getAttribute(idseries+"histogramvisionado")).contentEquals("3D");
}
%>
<div id="<%=idseries%>histogram" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>

<%if (visionado3D){ %>
<div id="sliders">
       <table>
           <tr>
               <td><label for="alpha">Alpha Angle</label></td>
               <td><input id="alpha" type="range" min="0" max="45" value="15"/> <span id="alpha-value" class="value"></span></td>
           </tr>
           <tr>
               <td><label for="beta">Beta Angle</label></td>
               <td><input id="beta" type="range" min="-45" max="45" value="15"/> <span id="beta-value" class="value"></span></td>
           </tr>
           <tr>
               <td><label for="depth">Depth</label></td>
               <td><input id="depth" type="range" min="20" max="100" value="50"/> <span id="depth-value" class="value"></span></td>
           </tr>
       </table>
</div>
<%} %>

<script type="text/javascript">

	var chart = new Highcharts.Chart({
	    chart: {
	        renderTo: '<%=idseries%>histogram',
	        type: 'column',
            backgroundColor: 'transparent',
	        options3d: {
	        	enabled: <%=visionado3D%>,
	            alpha: 15,
	            beta: 15,	      
	            depth: 50,
	            viewDistance: 25
	        },
	        style: {
           	 fontFamily: 'Roboto, sans-serif'   	 
           }
	    },
	    title: {
	        text: '<%=request.getAttribute(idseries+"histogramtitle")%>',
	        style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
	    },
	    subtitle: {
            text: '<%=request.getAttribute(idseries+"histogramsubtitle")%>',
            style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
        },
        
        xAxis: {
            categories: <%=request.getAttribute(idseries+"histogramabscisas")%>,
            labels: {
            	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '9pt'}
            }
        },
        plotOptions: {
        	series: {
                fillOpacity: 0.4
            },
            column: {
                depth: 25
            }
        },
        yAxis: {
            allowDecimals: true,
            min: <%=request.getAttribute(idseries+"histogramminEjeRef")%>,
            title: {
                text: '<%=request.getAttribute(idseries+"histogramtitulo_EJE_Y")%>',
                style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
            },
            labels: {
            	style: {'color': fontColor, 'font-weight': 'normal', 'font-size': '10pt'}
            }
        },    
        legend: {
            layout: 'horizontal',
            align: 'center',
            y: 14,
            itemWidth: 185,
            itemStyle: {'color': itemColor, 'font-weight': 'normal', 'font-size': '8pt'},
            verticalAlign: 'bottom'
        },
        tooltip: {				        	
            headerFormat: '<b>{point.key}</b><br>',
            pointFormat: '<span style="color:{series.color}">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute(idseries+"histogramdecimals")%>}'
        },
        series: <%=request.getAttribute(idseries+"histogramseries")%>
	});
	
	function showValues() {
	    $('#alpha-value').html(chart.options.chart.options3d.alpha);
	    $('#beta-value').html(chart.options.chart.options3d.beta);
	    $('#depth-value').html(chart.options.chart.options3d.depth);
	}

	// Activate the sliders
	$('#sliders input').on('input change', function () {
	    chart.options.chart.options3d[this.id] = parseFloat(this.value);
	    showValues();
	    chart.redraw(false);
	});

	showValues();
	
</script>
