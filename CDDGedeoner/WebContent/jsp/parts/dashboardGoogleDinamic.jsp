<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<html>
<head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <title>Tráfico mensual</title>
        
<script type="text/javascript">
$(function () {
    $('#linea').highcharts({
        chart: {
            type: 'line',  // tipo de gráfica
            borderWidth: 0 // ancho del borde de la gráfica
        },
        title: {
            text: 'Tráfico mensual de lo principales motores de búsqueda', // título
            x: -20 
        },
        subtitle: {
            text: 'Año 2013', // subtítulo
            x: -20
        },
        xAxis: {
            categories: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun',
                'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'] // categorías
        },
        yAxis: {
            title: {
                text: 'Tráfico (millones)' // nombre del eje de Y
            },
            plotLines: [{
                color: '#808080' 
            }]
        },
        tooltip: {
            valueSuffix: ' Millones' // el sufijo de la información presente en el "tooltip"
        },
        legend: { // configuración de la leyenda
            layout: 'horizontal',
            align: 'center',
            verticalAlign: 'bottom',
            borderWidth: 1
        },
        series: [{ // configuración de las series
            name: 'Google.com',
            data: [50, 55, 49, 66, 78, 87, 94, 99, 95, 90, 100, 96]
        }, {
            name: 'Yahoo.com',
            data: [35, 40, 41, 39, 52, 48, 55, 57, 60, 48, 53, 47]
        }, {
            name: 'Bing.com',
            data: [23, 25, 32, 31, 39, 44, 38, 42, 51, 43, 52, 55]
        }]
    });
         
        $('#pie').highcharts({
            chart: {
                type: 'pie',
                borderWidth: 0
            },
            title: {
                text: 'Cantidad de usuarios por motor de búsqueda',
            },
            subtitle: {
                text: 'Año 2013',
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            },
            series: [{
                name: 'Usuarios',
                data: [
                    {
                        name: 'Google.com',
                        y: 71.0,
                        sliced: true,
                        selected: true
                    },
                    ['Yahoo.com',       14.0],
                    ['Bing.com',     15.0]
                ]
            }]
        });
         
        $(document).ready(function() {
            Highcharts.setOptions({
                global: {
                    useUTC: false
                }
            });
         
            var chart;
            $('#tiempoReal').highcharts({
                chart: {
                    type: 'spline',
                    animation: Highcharts.svg, 
                    marginRight: 10,
                    events: {
                        load: function() {
         
                            // set up the updating of the chart each second
                            var google = this.series[0];
                            setInterval(function() {
                                var x = (new Date()).getTime(), // current time
                                    y = Math.floor((Math.random() * 1000000) + 1);
                                google.addPoint([x, y], true, true);
                            }, 2000);
                        }
                    }
                },
                title: {
                    text: 'Usuarios activos en Google.com'
                },
                subtitle:{
                    text: 'Tiempo Real'
                },
                xAxis: {
                    type: 'datetime',
                    tickPixelInterval: 150
                },
                yAxis: {
                    title: {
                        text: 'Usuarios Activos'
                    },
                    plotLines: [{
                        value: 0,
                        width: 1,
                        color: '#808080'
                    }]
                },
                tooltip: {
                    formatter: function() {
                            return '<b>'+ this.series.name +'</b><br/>'+
                            Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) +'<br/>'+
                            Highcharts.numberFormat(this.y, 2);
                    }
                },
                legend: {
                    enabled: false
                },
                exporting: {
                    enabled: false
                },
                series: [{
                    name: 'Google.com',
                    data: (function() {
                        // generate an array of random data
                        var data = [],
                            time = (new Date()).getTime(),
                            i;
         
                        for (i = -19; i <= 0; i++) {
                            data.push({
                                x: time + i * 1000,
                                y: Math.floor((Math.random() * 1000000) + 1)
                            });
                        }
                        return data;
                    })()
                }]
            });
        });
    });
     
 
 
        
</script>
    </head>
    <body>
        <!-- div que contendrá la gráfica lienal -->
        <div id="linea" style="width: 50%; height: 350px; margin: 0 auto;float:left;"></div>
        <!-- div que contendrá la gráfica circular -->
        <div id="pie" style="width: 50%; height: 350px; margin: 0 auto;float:left;"></div>
         
        <div style="border-top:1px solid #CDCDCD;margin:10px;padding:0;clear:both;"></div>
 
        <!-- div que contendrá la gráfica a tiempo real -->
        <div id="tiempoReal" style="height: 400px; margin: 0 auto;"></div>
    </body>
</html>