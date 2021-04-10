<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
        
	<table>
		<tr>
			<td>
				<jsp:include page="${containerJSP_11}">
					<jsp:param name="series" value="01" />
					<jsp:param name="width" value="640px" />
					<jsp:param name="height" value="360px" />
				</jsp:include>
			</td>
			<td>
				<jsp:include page="${containerJSP_12}">
					<jsp:param name="series" value="01" />
					<jsp:param name="width" value="600px" />
					<jsp:param name="height" value="360px" />
				</jsp:include>
			</td>
		</tr>	
		<tr>
			<td>
				<jsp:include page="${containerJSP_21}">
					<jsp:param name="series" value="01" />
					<jsp:param name="width" value="640px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
			<td>
				<jsp:include page="${containerJSP_22}">
					<jsp:param name="series" value="01" />
					<jsp:param name="width" value="600px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
		</tr>	
		<tr>
			<td>
				<jsp:include page="${containerJSP_31}">
					<jsp:param name="series" value="02" />
					<jsp:param name="width" value="640px" />
					<jsp:param name="height" value="360px" />
				</jsp:include>
			</td>
			<td>
				<jsp:include page="${containerJSP_32}">
					<jsp:param name="series" value="02" />
					<jsp:param name="width" value="600px" />
					<jsp:param name="height" value="360px" />
				</jsp:include>
			</td>
		</tr>	
		<tr>
			<td>
				<jsp:include page="${containerJSP_41}">
					<jsp:param name="series" value="02" />
					<jsp:param name="width" value="640px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
			<td>
				<jsp:include page="${containerJSP_42}">
					<jsp:param name="series" value="02" />
					<jsp:param name="width" value="600px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
		</tr>
	</table>
