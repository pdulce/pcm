<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String idseries = request.getParameter("idseries")==null?(String)request.getAttribute("idseries"): request.getParameter("idseries");
String width = request.getParameter("width") == null ? (String)request.getAttribute("width"): request.getParameter("width");
String height = request.getParameter("height") == null ? (String)request.getAttribute("height"): request.getParameter("height");
%>
<div id="<%=idseries%>histogram3d" style="width: <%=width%>; height: <%=height%>; margin: 0 auto;float:left;"></div>


<!--  div id="sliders">
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
</div>-->

<script type="text/javascript">

Highcharts.setOptions({
    colors: ['#2C5364', '#bdc3c7', '#7AA1D2', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4']
});

	var chart = new Highcharts.Chart({
	    chart: {
	        renderTo: '<%=idseries%>histogram3d',
	        type: 'column',
            backgroundColor: 'transparent',
	        options3d: {
	            enabled: false,
	            alpha: 15,
	            beta: 15,
	            depth: 50,
	            viewDistance: 25
	        }
	    },
	    title: {
	        text: '<%=request.getAttribute(idseries+"histogram3dtitle")%>',
	        style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '11pt'}
	    },
	    subtitle: {
            text: '<%=request.getAttribute(idseries+"histogram3dsubtitle")%>',
            style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': '9pt'}
        },
        
        xAxis: {
            categories: <%=request.getAttribute(idseries+"histogram3dabscisas")%>,
            labels: {
                style: {
                    color: '#606c88',
                    fontSize:'xx-small'
                }
            }
        },
        
        yAxis: {
            allowDecimals: true,
            min: <%=request.getAttribute(idseries+"histogram3dminEjeRef")%>,
            title: {
                text: '<%=request.getAttribute(idseries+"histogram3dtitulo_EJE_Y")%>',
                style: {'color': '#606c88', 'font-weight': 'lighter', 'font-size': 'xx-small'}
            },
            labels: {
                style: {
                    color: '#606c88',
                    fontSize:'small'
                }
            }
        },    
        legend: {
            layout: 'horizontal',
            align: 'center',
            y: 14,
            itemWidth: 155,
            itemStyle: {'color': '#606c88', 'font-weight': 'normal', 'font-size': '8px'},
            verticalAlign: 'bottom'
        },
        tooltip: {				        	
            headerFormat: '<b>{point.key}</b><br>',
            pointFormat: '<span style="color:{series.color}">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute(idseries+"histogram3ddecimals")%>}'
        },
        				       
        plotOptions: {
            column: {
                depth: 24,
                pointPadding: 0,
                borderWidth: 0,
                groupPadding: 0,
                shadow: false
            }
        },
        				      
        series: <%=request.getAttribute(idseries+"histogram3dseries")%>
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
