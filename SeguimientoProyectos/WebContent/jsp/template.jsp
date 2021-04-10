<%@ page language="java" contentType="text/html;charset=ISO-8859-1"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

<head>
	<title><%=request.getAttribute("#TITLE#")%></title>
	<jsp:include page="imports.jsp"></jsp:include>
</head>

<body
	onLoad="javascript:$('fieldset.collapsible').collapsible('<%=request.getAttribute("userCriteria")%>');
			initInputHighlightScript();initTree(<%=(String) request.getSession().getAttribute("fID")%>, <%=(String) request.getSession().getAttribute("gPfID")%>,  
	<%=(String) request.getSession().getAttribute("gP2fID")%>);
	document.getElementById('principal').style.display='block';
			document.getElementById('loadingdiv').style.display='none';">
			
	
  <div style="position: relative;">
     <div class="pcmTDLeft"> <%=request.getAttribute("#LOGO#")%><%=request.getAttribute("#TREE#")%> </div>
     <div class="pcmTDRight"> <jsp:include page="scene.jsp"></jsp:include> </div>  
  </div>

</body>

</html>