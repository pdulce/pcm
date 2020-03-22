<%@ page language="java" contentType="text/html;charset=ISO-8859-15"
	pageEncoding="ISO-8859-15"%>

<!DOCTYPE html>

<html>
<head>

<title><%=request.getAttribute("#TITLE#")%></title>
<link rel="stylesheet" type="text/css" href="css/pcm.css"></link>
<link rel="stylesheet" type="text/css"
	href="css/jquery-collapsible-fieldset.css"></link>

<script type="text/javascript" src="js/pcm.js"></script>
<script type="text/javascript" src="js/PCMGeneral.js"></script>
<script type="text/javascript" src="js/PCMensajes.js"></script>
<script type="text/javascript" src="js/overlib_mini.js"></script>
<script type="text/javascript" src="js/overlib_csstyle_mini.js"></script>
<script type="text/javascript" src="js/calendario.js"></script>
<script type="text/javascript" src="js/validacore.js"></script>
<script type="text/javascript" src="js/spinner.js"></script>
<script type="text/javascript" src="js/highlight-active-input.js"></script>
<script type="text/javascript" src="js/ajax.js"></script>
<script type="text/javascript" src="js/context-menu.js"></script>
<script type="text/javascript" src="js/external/jquery-1.9.1.js"></script>
<script type="text/javascript" src="js/external/jquery.cookies.2.2.0.js"></script>
<script type="text/javascript" src="js/external/slide.js"></script>
<script type="text/javascript" src="js/folder-tree-static.js"></script>
<script type="text/javascript"
	src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
<script type="text/javascript"
	src="js/external/jquery-collapsible-fieldset.js"></script>

<%
	if (request.getAttribute("json_histogram3d") != null
			|| request.getAttribute("json_pieChart") != null
			|| request.getAttribute("barChart") != null
			|| request.getAttribute("json_spiderweb") != null
			|| request.getAttribute("json_scatterSeries") != null
			|| request.getAttribute("json_dualHistogram") != null
			|| request.getAttribute("json_timeSeries") != null) {
%>
<script src="js/highcharts/highcharts.js"></script>
<script src="js/highcharts/highcharts-more.js"></script>
<script src="js/highcharts/highcharts-3d.js"></script>
<script src="js/highcharts/modules/exporting.js"></script>
<%
	}
	if (request.getAttribute("json_scatterSeries") != null) {
%>
<script src="js/highcharts/highcharts.js"></script>
<script src="js/highcharts/modules/exporting.js"></script>
<%
	}
	if (request.getAttribute("json_countryMap") != null) {
%>
<script src="js/highcharts/highmaps.js"></script>
<script src="js/highcharts/modules/exporting.js"></script>
<script src="js/highcharts/countries/es-all.js"></script>
<%
	}

	if (request.getAttribute("json_europeMap") != null) {
%>
<script src="js/highcharts/highmaps.js"></script>
<script src="js/highcharts/modules/exporting.js"></script>
<script src="js/highcharts/countries/europe.js"></script>
<%
	}
%>

</head>

<body
	onLoad="javascript:initTree(<%=(String) request.getSession().getAttribute("fID")%>, <%=(String) request.getSession().getAttribute("gPfID")%>,  
	<%=(String) request.getSession().getAttribute("gP2fID")%>);
	document.getElementById('principal').style.display='block';
			document.getElementById('loadingdiv').style.display='none';">

	<%
		if (request.getAttribute("json_countryMap") != null) {
	%>

	<script type="text/javascript">
		$(function () {		    
			$('#<%=request.getAttribute("container")%>').highcharts('Map', {
			
			        title : {
			            text : '<%=request.getAttribute("title")%>'
			        },
			
			        subtitle : {
			            text : '<%=request.getAttribute("subtitle")%>'
			        },
			        			        
		            legend: {
		            	title: {
		                    text: '<%=request.getAttribute("units")%> <%=request.getAttribute("entidadGrafico")%>',
		                    style: {
		                        color: (Highcharts.theme && Highcharts.theme.textColor) || 'black'
		                    }
		                },
			            width: 840,
			            floating: true,
			            align: 'left',
			            x: 90, // = marginLeft - default spacingLeft
			            y: 2,
			            itemWidth: 50,
			            borderWidth: 1
			        },
		            
		        
		            colorAxis: {
		                min: 0,
		                minColor: '<%=request.getAttribute("minColor")%>',
		                maxColor: '<%=request.getAttribute("maxColor")%>'
		            },
		            
			        mapNavigation: {
			            enabled: true,
			            buttonOptions: {
			                verticalAlign: 'bottom'
			            }
			        },
			        
		        series : [{
		            data : <%=request.getAttribute("json_countryMap")%>,
		            mapData: Highcharts.maps['countries/es/es-all'],
		            joinBy: 'hc-key',
		            name: '<%=request.getAttribute("entidadGrafico")%>',
		            states: {
		                hover: {
		                    color: '#EEDD66'
		                }
		            },
		            dataLabels: {
		                enabled: true,
		                format: '<b>{point.name}</b>: {point.value:<%=request.getAttribute("decimals")%>}',
		            }
		        }, {
		            name: 'Separators',
		            type: 'mapline',
		            data: Highcharts.geojson(Highcharts.maps['countries/es/es-all'], 'mapline'),
		            color: 'silver',
		            showInLegend: false,
		            enableMouseTracking: false
		        }]
		    });
		});
 
 
 	</script>

	<%
		} else if (request.getAttribute("json_europeMap") != null) {
	%>

	<script type="text/javascript">
		$(function () {		    
			$('#<%=request.getAttribute("container")%>').highcharts('Map', {
			
			        title : {
			            text : ''
			        },
			
			        subtitle : {
			            text : ''
			        },
			        
		            legend: {
		            	title: {
		                    text: '<%=request.getAttribute("entidad")%>',
		                    style: {
		                        color: (Highcharts.theme && Highcharts.theme.textColor) || 'black'
		                    }
		                },
			            width: 840,
			            floating: true,
			            align: 'left',
			            x: 90, // = marginLeft - default spacingLeft
			            y: 2,
			            itemWidth: 50,
			            borderWidth: 1
			        },
		        
		            colorAxis: {
		                min: 0,
		                minColor: '<%=request.getAttribute("minColor")%>',
		                maxColor: '<%=request.getAttribute("maxColor")%>'
		            },
		            
			        mapNavigation: {
			            enabled: true,
			            buttonOptions: {
			                verticalAlign: 'bottom'
			            }
			        },
			        
		        series : [{
		            data : <%=request.getAttribute("json_europeMap")%>,
		            mapData: Highcharts.maps['custom/europe'],
		            joinBy: 'hc-key',
		            name: '<%=request.getAttribute("entidad")%>',
		            states: {
		                hover: {
		                    color: '#EEDD66'
		                }
		            },
		            dataLabels: {
		                enabled: true,
		                format: '<p style="font-style: italic;size=1.1em"><b>{point.name}</b>: {point.value:<%=request.getAttribute("decimals")%>} </p>',
		            }
		        }, {
			            name: 'Separators',
			            type: 'mapline',
			            data: Highcharts.geojson(Highcharts.maps['custom/europe'], 'mapline'),
			            color: 'silver',
			            showInLegend: false,
			            enableMouseTracking: false
			  }]
			        
		    });
		});
 
 
 	</script>

	<%
		} else if (request.getAttribute("json_scatterSeries") != null) {
	%>

	<script type="text/javascript">
			$(function () {
				$('#<%=request.getAttribute("container")%>').highcharts({
			        chart: {
			            type: 'scatter',
			            zoomType: 'xy'
			        },
			        title: {
			            text: '<%=request.getAttribute("title")%>'
			        },
			        subtitle: {
			            text: '<%=request.getAttribute("subtitle")%>'
			        },
			        xAxis: {
			            title: {
			                enabled: true,
			                text: '<%=request.getAttribute("titulo_EJE_X")%>'
			            },
			            startOnTick: true,
			            endOnTick: true,
			            showLastLabel: true
			        },
			        yAxis: {
			            title: {
			                text: '<%=request.getAttribute("titulo_EJE_Y")%>'
			            }
			        },
			        legend: {		            	
			            width: 940,
			            floating: true,
			            align: 'left',
			            x: 60,
			            y: 14,
			            itemWidth: 178,
			            itemStyle: {'color': 'black', 'font-weight': 'bold', 'font-size': '9px'},
			            itemDistance: 80,
			            borderWidth: 0
			        },
			        plotOptions: {
			            scatter: {
			                marker: {
			                    radius: 2,
			                    states: {
			                        hover: {
			                            enabled: true,
			                            lineColor: 'rgb(100,100,100)'
			                        }
			                    }
			                },
			                states: {
			                    hover: {
			                        marker: {
			                            enabled: false
			                        }
			                    }
			                },
			                tooltip: {
			                    headerFormat: '<b>{series.name}</b><br>',
			                    pointFormat: '{point.x} <%=request.getAttribute("tooltip_X")%>, {point.y} <%=request.getAttribute("tooltip_Y")%>'
			                }
			            }
			        },
			        series:  <%=request.getAttribute("json_scatterSeries")%>
			    });
			});
				
		</script>

	<%
		} else if (request.getAttribute("json_pieChart") != null) {
	%>

	<script type="text/javascript">
	    // Radialize the colors
	    Highcharts.getOptions().colors = Highcharts.map(Highcharts.getOptions().colors, function (color) {
	        return {
	            radialGradient: {
	                cx: 0.5,
	                cy: 0.3,
	                r: 0.7
	            },
	            stops: [
	                [0, color],
	                [1, Highcharts.Color(color).brighten(-0.3).get('rgb')] // darken
	            ]
	        };
	    });
    
	 	$(function () {
		    $('#<%=request.getAttribute("container")%>').highcharts({
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: true,
            type: '<%=request.getAttribute("typeOfChart")%>'
        },
        title: {
            text: '<%=request.getAttribute("title")%>'
        },
        subtitle: {
            text: '<%=request.getAttribute("subtitle")%>'
        },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.percentage:.2f} %</b>'
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    format: '<b>{point.name}: {point.percentage:.2f} %</b>',
                    distance: -80,
                    filter: {
                    	property: 'percentage',
                    	operator: '>',
                    	value: '4'},
                    style: {
                        color: 'black',
                        fontSize: '14px',                        
                        fontStyle: 'normal'                                      
                    },
                    connectorColor: 'silver'
                }
            }
        },
        series: <%=request.getAttribute("json_pieChart")%>,        
  		 legend: {
	  		 	enabled: true,
	           	title: {
	                   text: '<%=request.getAttribute("units")%> <%=request.getAttribute("entidadGrafico")%>',
	                   style: {
	                   color: (Highcharts.theme && Highcharts.theme.textColor) || 'black'
	                    }
	             },
	            width: 840,
	            floating: true,
	            align: 'left',
	            x: 90, // = marginLeft - default spacingLeft
	            y: 2,
	            itemWidth: 50,
	            borderWidth: 1
        }  			
	    });
	  });
	 </script>

	<%
		} else if (request.getAttribute("barChart") != null) {
	%>
	<script type="text/javascript">
			$(function () {
			    $('#<%=request.getAttribute("container")%>').highcharts({
			        chart: {
			            type: 'bar'
			        },
			        title: {
			            text: '<%=request.getAttribute("title")%>'
			        },
			        subtitle: {
			            text: '<%=request.getAttribute("subtitle")%>'
			        },
			        xAxis: {
			            categories: <%=request.getAttribute("categories")%>,			            
			            labels: {
			                style: {
			                    color: '#6E6E6E',
			                    fontSize:'small'
			                }
			            },
			            title: {
			                text: ''
			            }
			        },
			        yAxis: {
			        	min: <%=request.getAttribute("minEjeRef")%>,
			            title: {
			                text: '<b><%=request.getAttribute("titulo_EJE_X")%></b>',
			                align: 'high'
			            },
			            labels: {
			                overflow: 'justify'
			            },
			            stackLabels: {
			                enabled: true,
			                style: {
			                    fontWeight: 'bold',
			                    color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
			                }
			            }
			        },
			        tooltip: {
			        	pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b><br/>',
			            shared: true,
			            valueSuffix: ' <%=request.getAttribute("units")%>'
			        },
			        
		            legend: {		            	
			            width: 1300,
			            floating: true,
			            align: 'center',
			            x: 10, // = marginLeft - default spacingLeft
			            y: 17,
			            itemWidth: 85,
			            itemStyle: {'color': 'black', 'font-weight': 'normal', 'font-size': '10px'},
			            itemDistance: 55,
			            borderWidth: 0
			        },
			        
			        plotOptions: {
			            bar: {
			            	dataLabels: {
			                    enabled: true,
			                    color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white',
			                    style: {
			                    	fontWeight: 'bold',
			                        textShadow: '0 0 5px black'
			                    }
			                },
			                borderWidth: 4,
			                edgeWidth: 4
			            },
			            series: {
			                stacking: 'normal'
			            }
			        },
			        
			        credits: {
			            enabled: false
			        },
			        series: <%=request.getAttribute("barChart")%>
			    });
			});
	</script>

	<%
		} else if (request.getAttribute("json_dualHistogram") != null) {
	%>

	<script type="text/javascript">
	 	$(function () {
		    $('#<%=request.getAttribute("container")%>').highcharts({
		        chart: {
		            zoomType: 'xy'
		        },
		        title: {
		            text: '<%=request.getAttribute("title")%>'
		        },
		        subtitle: {
		            text: '<%=request.getAttribute("subtitle")%>'
		        },
		        xAxis: [{
		            categories: <%=request.getAttribute("json_dualHistogram")%>,
		            labels: {
		                style: {
		                    color: '#6E6E6E',
		                    fontSize:'small'
		                }
		            },
		            crosshair: true
		        }],
		        yAxis: [{ // Primary yAxis
		            labels: {
		                format: '{value} ',
		                style: {
		                    color: Highcharts.getOptions().colors[0]
		                }
		            },
		            allowDecimals: true,
		            min: <%=request.getAttribute("minEjeRef")%>,
		            title: {
		                text: 'Frecuencia relativa',
		                style: {
		                    color: Highcharts.getOptions().colors[0]
		                }
		            }
		        }, { // Secondary yAxis
		            title: {
		                text: 'Frecuencia acumulada',
		                style: {
		                    color: Highcharts.getOptions().colors[1]
		                }
		            },
		            labels: {
		                format: '{value}%',
		                style: {
		                    color: Highcharts.getOptions().colors[1]
		                }
		            },
		            opposite: true
		        }],
		        tooltip: {
		            shared: true
		        },
		        legend: {		            	
		            width: 560,
		            floating: true,
		            align: 'left',
		            x: 170, // = marginLeft - default spacingLeft
		            y: 17,
		            itemWidth: 180,
		            itemStyle: {'color': 'black', 'font-weight': 'bold', 'font-size': '12px'},
		            itemDistance: 80,
		            borderWidth: 0
		        },

		        series: [{
		        	name: 'Frecuencia relativa',
		            type: 'column',
		            data: <%=request.getAttribute("frecAbsoluta")%>,
		            tooltip: {
		                valueSuffix: ' <%=request.getAttribute("units")%>'
		            }

		        }, {
		            name: 'Frecuencia acumulada',
		            type: 'spline',
		            yAxis: 1,
		            data: <%=request.getAttribute("frecAcum")%>,
		            tooltip: {
		                valueSuffix: '%'
		            }
		        }]
		    });
		}); 
	 </script>

	<%
		} else if (request.getAttribute("json_histogram3d") != null) {
	%>

	<script type="text/javascript">
			
			$(function () {
			    
				Highcharts.setOptions({
				    lang: {
				        decimalPoint: ',',
				        thousandsSep: '.'
				    }
				});
				
				var chart = new Highcharts.Chart({
			        	chart: {
			            	renderTo: '<%=request.getAttribute("container")%>',       	
				            type: 'column',
				            margin: 75,
				            options3d: {
				                enabled: true,
				                alpha: 5,
				                beta: <%=request.getAttribute("profundidad")%>,
				                depth: 90,
				                viewDistance: 20
				            }
				        },
				        title: {
				            text: '<%=request.getAttribute("title")%>'
				        },
				        
				        subtitle: {
				            text: '<%=request.getAttribute("subtitle")%>'
				        },
				        
				        xAxis: {
				            categories: <%=request.getAttribute("abscisas")%>,
				            labels: {
				                style: {
				                    color: '#6E6E6E',
				                    fontSize:'small'
				                }
				            }
				        },
				        
				        yAxis: {
				            allowDecimals: true,
				            min: <%=request.getAttribute("minEjeRef")%>,
				            title: {
				                text: '<%=request.getAttribute("titulo_EJE_Y")%>'
				            }
				        },
				        				        
				        legend: {		            	
				            width: 1080,
				            floating: true,
				            align: 'center',
				            x: 95, // = marginLeft - default spacingLeft
				            y: 9,
				            itemWidth: 220,
				            itemStyle: {'color': 'black', 'font-weight': 'normal', 'font-size': '12px'},
				            itemDistance: 85,
				            borderWidth: 0
				        },
				        
				        
				        tooltip: {				        	
				            headerFormat: '<b>{point.key}</b><br>',
				            pointFormat: '<span style="color:{series.color}">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute("decimals")%>}'
				        },
				        				       
				        plotOptions: {
				        	series: {
				                borderWidth: 2,
				                dataLabels: {
				                    enabled: true,
				                    style: {'color': '#610B5E', 'font-weight': 'bold', 'font-size': '10px'},
				                    format: '{point.y:<%=request.getAttribute("decimals")%>}'
				                }
				            },
				            column: {
				                depth: 25,
				                stacking: true,
				                grouping: false,
				                groupZPadding: 10
				            }
				        },
				        				      
				        series: <%=request.getAttribute("json_histogram3d")%>
				 
				});
				
				function showValues() {
			        $('#R0-value').html(chart.options.chart.options3d.alpha);
			        $('#R1-value').html(chart.options.chart.options3d.beta);
			    }

			    // Activate the sliders
			    $('#R0').on('change', function () {
			        chart.options.chart.options3d.alpha = this.value;
			        showValues();
			        chart.redraw(false);
			    });
			    $('#R1').on('change', function () {
			        chart.options.chart.options3d.beta = this.value;
			        showValues();
			        chart.redraw(false);
			    });

			    showValues();
							 				   
			});
				
			</script>

	<%
		} else if (request.getAttribute("json_timeSeries") != null) {
	%>

	<script type="text/javascript">
			
			$(function () {
			    
				Highcharts.setOptions({
				    lang: {
				        decimalPoint: ',',
				        thousandsSep: '.'
				    }
				});
				
				$('#<%=request.getAttribute("container")%>').highcharts({
				
			        	chart: {
			            	renderTo: '<%=request.getAttribute("container")%>',       	
				            type: 'line',
				            margin: 75				           
				        },
				        title: {
				            text: '<%=request.getAttribute("title")%>'
				        },
				        
				        subtitle: {
				            text: '<%=request.getAttribute("subtitle")%>'
				        },
				        
				        xAxis: {
				            categories: <%=request.getAttribute("abscisas")%>,
				            labels: {
				                style: {
				                    color: '#6E6E6E',
				                    fontSize:'xx-small'
				                }
				            }
				        },
				        
				        yAxis: {
				            min:  <%=request.getAttribute("minEjeRef")%>,
				            allowDecimals: true,
				            title: {
				                text: '<%=request.getAttribute("titulo_EJE_Y")%>'
				            }
				        },
				        
				        legend: {		            	
				            width: 1080,
				            floating: true,
				            align: 'center',
				            x: 95, // = marginLeft - default spacingLeft
				            y: 9,
				            itemWidth: 220,
				            itemStyle: {'color': 'black', 'font-weight': 'normal', 'font-size': '12px'},
				            itemDistance: 85,
				            borderWidth: 0
				        },
				        
				        tooltip: {				        	
				            headerFormat: '<b>{point.key}</b><br>',
				            pointFormat: '<span style="font-size: xx-small; color:{series.color}">\u25CF</span> {series.name}: {point.y:<%=request.getAttribute("decimals")%>}'
				        },
				        				       
				        plotOptions: {
				        	line: {
				                dataLabels: {
				                    enabled: true
				                },
				                enableMouseTracking: false
				            },
				        	series: {
				                borderWidth: 2,
				                dataLabels: {
				                    enabled: true,
				                    style: {'color': '#e6e6ff', 'font-weight': 'lighter', 'font-size': 'xx-small'},
				                    format: '{point.y:<%=request.getAttribute("decimals")%>}'
				                }
				            },
				            column: {
				                depth: 25,
				                stacking: true,
				                grouping: false,
				                groupZPadding: 10
				            }
				        },
				        				      
				        series: <%=request.getAttribute("json_timeSeries")%>
				 
				});						
							 				   
			});
				
			</script>

	<%
		} else if (request.getAttribute("json_spiderweb") != null) {
	%>

	<script type='text/javascript'>//<![CDATA[ 
			$(function () {
	
				$('#<%=request.getAttribute("container")%>').highcharts({
	
					chart: {
	        			polar: true,
	        			type : '<%=request.getAttribute("typeOfChart")%>'
	    			},
	
			        title: {
			        	text : '<%=request.getAttribute("title")%>',
			            x: -80
			        },
	
			        pane: {
			            size: '80%'
			        },
			        subtitle: {
			            text: '<%=request.getAttribute("subtitle")%>'
			        },
			        xAxis : {
						categories : <%=request.getAttribute("categories")%>,
						labels: {
			                style: {
			                    color: '#6E6E6E',
			                    fontSize:'xx-small'
			                }
			            },
						tickmarkPlacement: 'on',
			        	lineWidth: 0
				    },
	
			        yAxis: {
			            gridLineInterpolation: 'polygon',
			            lineWidth: 0,
			            min: 0
			        },
	
			        tooltip: {
			        	shared: true,
			            pointFormat: '<span style="color:{series.color}">{series.name}: <b>{point.y} </b><br/>'
								},
						legend: {		            	
				            width: 1080,
				            floating: true,
				            align: 'center',
				            x: 95, // = marginLeft - default spacingLeft
				            y: 9,
				            itemWidth: 220,
				            itemStyle: {'color': 'black', 'font-weight': 'normal', 'font-size': '12px'},
				            itemDistance: 85,
				            borderWidth: 0
				        },

				series : <%=request.getAttribute("json_spiderweb")%>
	
			    });
			});
	   			//]]>  	
			</script>

	<%
		}
	%>

	<table>
		<!-- style="border: 5px bevel; elevation: 2;"> -->
		<tr>
			<!--style="border: 4px bevel; elevation: -2;"> -->
			<td class="pcmTDLeft" valign="top"><%=request.getAttribute("#LOGO#")%><%=request.getAttribute("#TREE#")%></td>
			<td class="pcmTDRight">
				<div id="loadingdiv">
					<%
						if (request.getAttribute("container") != null) {
					%>
					<p align="center">
						<font class="verysmall">Image loaded</font>
					</p>
					<%
						} else {
					%>
					<p align="center">
						<font class="small">Loading...please wait</font>
					</p>
					<%
						}
					%>
				</div> <%
 	if (request.getAttribute("container") != null) {
 %>
				<div id="<%=request.getAttribute("container")%>"
					style='min-width: <%=request.getAttribute("width-container")%>px; max-width: 1090px; height: <%=request.getAttribute("height-container")%>px; margin: 0 auto;'></div>
				<br> <!-- layer container --> <%
 	if (request.getAttribute("is3D") != null) {
 %>
				<div id="sliders">
					<table>
						<tr>
							<td><font class="small">angulo de giro transversal</font>
							</td>
							<td><input id="R0" type="range" min="0" max="45" value="15" />
								<span id="R0-value" class="value"></span>
							</td>
						</tr>
						<tr>
							<td><font class="small">angulo de giro longitudinal</font>
							</td>
							<td><input id="R1" type="range" min="0" max="45" value="15" />
								<span id="R1-value" class="value"></span>
							</td>
						</tr>
					</table>
				</div> <%
 	}
 		if (request.getAttribute("addedInfo") != null) {
 %> <%=request.getAttribute("addedInfo")%>
				<%
					}
				%>
				<UL id="pcmUl">
					<LI><a title="Volver" href="#"
						onClick="javascript:window.history.back();"> <span>Volver</span>
					</a></LI>
				</UL>
				<div id="principal">&nbsp;</div> <%
 	} else {
 %>
				<div id="principal"><%=request.getAttribute("#BODY#")%></div> <!-- layer de escenario de pcm -->
				<%
					}
				%>
			</td>
		</tr>

		<tr>
			<td class="small"><%=request.getAttribute("#FOOT#")%></td>
			<td class="small">&nbsp;</td>
		</tr>

	</table>

	<script type="text/javascript">
		$("fieldset.collapsible").collapsible('<%=request.getAttribute("userCriteria")%>');
		initInputHighlightScript();	
	</script>


</body>

</html>