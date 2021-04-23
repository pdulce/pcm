<%@ page language="java" contentType="text/html; charset=ISO-8859-1"  pageEncoding="UTF-8"%>

<label class="infoCls" title="Escala"> &nbsp;&nbsp;Escala&nbsp;</label>
<label class="radiogroupcontainer">&nbsp;Diario&nbsp; <input
	onChange="javascript:document.forms[0].submit();return true;"
	type="radio" class="checkmarkradio" value="dayly" id="escaladodayly"
	name="escalado"> <span class="checkmarkradio"></span>
</label>
<label class="radiogroupcontainer">&nbsp;Semanal&nbsp; <input
	onChange="javascript:document.forms[0].submit();return true;"
	type="radio" class="checkmarkradio" value="weekly" id="escaladoweekly"
	name="escalado"> <span class="checkmarkradio"></span>
</label>
<label class="radiogroupcontainer">&nbsp;Mensual&nbsp; <input
	onChange="javascript:document.forms[0].submit();return true;"
	type="radio" class="checkmarkradio" value="monthly"
	id="escaladomonthly" name="escalado"> <span
	class="checkmarkradio"></span>
</label>
<label class="radiogroupcontainer">&nbsp;Trimestral&nbsp; <input
	onChange="javascript:document.forms[0].submit();return true;"
	type="radio" class="checkmarkradio" value="3monthly"
	id="escalado3monthly" name="escalado"> <span
	class="checkmarkradio"></span>
</label>
<label class="radiogroupcontainer">&nbsp;Semestral&nbsp; <input
	onChange="javascript:document.forms[0].submit();return true;"
	type="radio" class="checkmarkradio" value="6monthly"
	id="escalado6monthly" name="escalado"> <span
	class="checkmarkradio"></span>
</label>
<label class="radiogroupcontainer">&nbsp;Anual&nbsp; <input
	onChange="javascript:document.forms[0].submit();return true;"
	type="radio" class="checkmarkradio" value="anualy" id="escaladoanualy"
	name="escalado"> <span class="checkmarkradio"></span>
</label>
<label class="radiogroupcontainer">&nbsp;Autom&aacute;tico&nbsp;
	<input onChange="javascript:document.forms[0].submit();return true;"
	type="radio" class="checkmarkradio" value="automatic"
	id="escaladoautomatic" name="escalado"> <span
	class="checkmarkradio"></span>
</label>

<label class="infoCls" title="Operación de agregación">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Operaci&oacute;n
	de agregaci&oacute;n&nbsp;</label>
<label class="radiogroupcontainer">&nbsp;Promediar&nbsp; <input
	onChange="javascript:document.forms[0].submit();return true;"
	type="radio" class="checkmarkradio" value="AVG" id="operationAVG"
	name="operation"> <span class="checkmarkradio"></span>
</label>
<label class="radiogroupcontainer">Totalizar&nbsp; <input
	onChange="javascript:document.forms[0].submit();return true;"
	type="radio" class="checkmarkradio" value="SUM" id="operationSUM"
	name="operation"> <span class="checkmarkradio"></span>
</label>


<script type="text/javascript">
		
	var escala = document.forms[0].escalado;
	<% 	
	Object escaladoParamValue = request.getAttribute("escalado");
	if (escaladoParamValue != null){
		String[] as= (String[]) escaladoParamValue;
		for (int i=0;i<as.length;i++){%>		
			for (var j = 0; j < escala.length; j++) {
				if (escala[j].value == '<%=as[i]%>'){
					escala[j].checked = true;
				}
			}
		
		<%}//for values
	}else{ %>
			//alert('iniciando valores de escala...');
		  	for (var j = 0; j < escala.length; j++) {
		  		if (escala[j].value == 'monthly'){
		  			escala[j].checked = true;	
				}else{
					escala[j].checked = false;
				}
			}
  <%} %>
  
  var oper = document.forms[0].operation;
  <% 	
	Object operationParamValue = request.getAttribute("operation");
	if (operationParamValue != null){
		String[] as= (String[]) operationParamValue;
		for (int i=0;i<as.length;i++){%>		
			for (var j = 0; j < oper.length; j++) {
				if (oper[j].value == '<%=as[i]%>'){
					oper[j].checked = true;
				}
			}
	<%}//for values
	}else{ %>
		for (var j = 0; j < oper.length; j++) {
			if (oper[j].value == 'AVG'){
				oper[j].checked = true;	
			}else{
				oper[j].checked = false;
			}
		}
  <%} %>
  
</script>