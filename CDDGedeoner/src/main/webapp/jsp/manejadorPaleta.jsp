<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>

<% 
String defaultMode = (String)request.getAttribute("style");
String fontColor_ = defaultMode.contentEquals("darkmode") ? "#FFEFBA" : "#203A43";
String itemColor_ = defaultMode.contentEquals("darkmode") ? "#FFEFBA" : "#859398";
%>

<script type="text/javascript">
    var colores =  new Array();
	var fontColor = '<%=fontColor_%>';
	var itemColor = '<%=fontColor_%>';
	
	<%Object paletaInSession = request.getAttribute("paletaColores");
	
	if (paletaInSession != null){		
		String[] as= (String[]) paletaInSession;				
		for (int i=0;i<as.length;i++){
			//System.out.println("paleta color " + (i+1) + "-esimo:" + as[i]);
	%>		
			colores[<%=i%>] = '<%=as[i]%>';
		
	<% 	}//end for values
	}else { 
	%>
		colores = ['#647a0c', '#f6d625','#9aba42', '#132a13',  '#ecf39e', '#F0C779', '#CCB0B0', '#EBEC6B', '#1F4C5C','#1B5C5C'];
	
  <% }%>

<%if (defaultMode.contentEquals("darkmode")){ %>
		Highcharts.setOptions({
			colors: colores
		});
<%}else{%>
		Highcharts.setOptions({
			colors: colores
		});
<%}%>

/*Highcharts.setOptions({
    colors: Highcharts.map(Highcharts.getOptions().colors, function (color) {
        return {
            radialGradient: {
                cx: 0.1,
                cy: 0.1,
                r: 0.8
            },
            stops: [
                [0, color],
                [1, Highcharts.color(color).brighten(-0.15).get('rgb')] // darken
            ]
        };
    })
});*/

</script>
