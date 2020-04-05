<%@ page language="java" contentType="text/html;charset=ISO-8859-1"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

<head>
	<title><%=request.getAttribute("#TITLE#")%></title>
	<jsp:include page="imports.jsp"></jsp:include>
</head>

<body
	onLoad="javascript:initTree(<%=(String) request.getSession().getAttribute("fID")%>, <%=(String) request.getSession().getAttribute("gPfID")%>,  
	<%=(String) request.getSession().getAttribute("gP2fID")%>);
	document.getElementById('principal').style.display='block';
			document.getElementById('loadingdiv').style.display='none'; 
			$('fieldset.collapsible').collapsible('<%=request.getAttribute("userCriteria")%>');
			initInputHighlightScript();">
			
	<table>
		<tr>
			<td class="pcmTDLeft" valign="top">
				<%=request.getAttribute("#LOGO#")%><%=request.getAttribute("#TREE#")%>
			</td>
			<td class="pcmTDRight">
				<jsp:include page="scene.jsp"></jsp:include>
			</td>
		</tr>
		<tr>
			<td class="small">
				<%=request.getAttribute("#FOOT#")%>
			</td>
			<td class="small">&nbsp;</td>
		</tr>
	</table>

</body>

</html>