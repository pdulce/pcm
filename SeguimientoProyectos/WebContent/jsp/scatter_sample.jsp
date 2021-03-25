<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<script type="text/javascript">

Highcharts.chart('container', {
    title: {
    	text: '<%=request.getAttribute("title")%>'
    },
    subtitle: {
        text: '<%=request.getAttribute("subtitle")%>'
    },
    xAxis: {
        min: -0.5,
        max: 5.5
    },
    yAxis: {
        min: 0
    },
    series: [{
        type: 'line',
        name: 'Regression Line',
        data: [[0, 1.11], [5, 4.51]],
        marker: {
            enabled: false
        },
        states: {
            hover: {
                lineWidth: 0
            }
        },
        enableMouseTracking: false
    }, {
        type: 'scatter',
        name: 'Observations',
        data: <%=request.getAttribute("series")%>,
        marker: {
            radius: 4
        }
    }]
});

</script>