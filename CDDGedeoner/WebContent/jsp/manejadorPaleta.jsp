<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String defaultMode = (String)request.getAttribute("style");
String fontColor_ = defaultMode.contentEquals("darkmode") ? "#FFEFBA" : "#203A43";
String itemColor_ = defaultMode.contentEquals("darkmode") ? "#FFEFBA" : "#859398";

%>

<script type="text/javascript">
	var fontColor = '<%=fontColor_%>';
	var itemColor = '<%=fontColor_%>';

<%if (defaultMode.contentEquals("darkmode")){ %>
		Highcharts.setOptions({
			colors: [ '#06B5CA', '#00607E', '#B3DFF2', '#35DBC6', '#D3F9D6', '#F0C779', '#CCB0B0', '#EBEC6B', '#1F4C5C']
		});
<%}else{%>
		Highcharts.setOptions({
			colors: [ '#06B5CA', '#00607E', '#B3DFF2', '#35DBC6', '#D3F9D6', '#F0C779', '#CCB0B0', '#EBEC6B', '#1F4C5C']
		});
<%}%>

Highcharts.setOptions({
    colors: Highcharts.map(Highcharts.getOptions().colors, function (color) {
        return {
            radialGradient: {
                cx: 0.5,
                cy: 0.3,
                r: 0.7
            },
            stops: [
                [0, color],
                [1, Highcharts.color(color).brighten(-0.35).get('rgb')] // darken
            ]
        };
    })
});

</script>
