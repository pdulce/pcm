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
	var chart = new Highcharts.Chart({
	    chart: {
	        renderTo: '<%=idseries%>histogram3d',
	        type: 'column',
            backgroundColor: 'transparent',
	        options3d: {
	            enabled: true,
	            alpha: 15,
	            beta: 15,
	            depth: 50,
	            viewDistance: 25
	        }
	    },
	    title: {
	        text: '<%=request.getAttribute(idseries+"histogram3dtitle")%>',
	        style: {'color': 'orange', 'font-weight': 'lighter', 'font-size': '11pt'}
	    },
	    subtitle: {
            text: '<%=request.getAttribute(idseries+"histogram3dsubtitle")%>',
            style: {'color': 'orange', 'font-weight': 'lighter', 'font-size': '9pt'}
        },
        
        xAxis: {
            categories: <%=request.getAttribute(idseries+"histogram3dabscisas")%>,
            labels: {
                style: {
                    color: 'orange',
                    fontSize:'xx-small'
                }
            }
        },
        
        yAxis: {
            allowDecimals: true,
            min: <%=request.getAttribute(idseries+"histogram3dminEjeRef")%>,
            title: {
                text: '<%=request.getAttribute(idseries+"histogram3dtitulo_EJE_Y")%>',
                style: {'color': 'orange', 'font-weight': 'lighter', 'font-size': 'xx-small'}
            },
            labels: {
                style: {
                    color: 'orange',
                    fontSize:'small'
                }
            }
        },    
        legend: {		            	
            width: 1080,
            floating: true,
            align: 'center',
            x: 95, // = marginLeft - default spacingLeft
            y: 22,
            itemWidth: 220,
            itemStyle: {'color': 'orange', 'font-weight': 'normal', 'font-size': '12px'},
            itemDistance: 85,
            borderWidth: 0
        },
        tooltip: {				        	
            headerFormat: '<b>{point.key}</b><br>',
            pointFormat: '<span style="color:{series.color}">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute(idseries+"histogram3ddecimals")%>}'
        },
        				       
        plotOptions: {
            column: {
                depth: 25
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
