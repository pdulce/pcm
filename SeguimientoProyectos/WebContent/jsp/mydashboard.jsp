<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
    
    
    <form action="prjManager" method="POST">
    <table>
    	<tr>
    		<td>
    			<label class="infoCls" title="Entorno" id="estudiosPeticiones.id_entornoLABEL" for="estudiosPeticiones.id_entorno">
    			&nbsp;&nbsp;Entorno&nbsp;
    			</label>
				<select class="textInput" id="estudiosEntregas.id_entorno" name="estudiosEntregas.id_entorno"
				onChange="javascript:document.forms[0].submit();return true;" size="5"
				multiple>
					<option value="estudiosEntregas.id_entorno=1" id="estudiosEntregas.id_entorno=1" selected>HOST</option>
					<option value="estudiosEntregas.id_entorno=2" id="estudiosEntregas.id_entorno=2" selected>Pros@</option>
					<option value="estudiosEntregas.id_entorno=3" id="estudiosEntregas.id_entorno=3" selected>Web  Services</option>
					<option value="estudiosEntregas.id_entorno=4" id="estudiosEntregas.id_entorno=4" selected>Servicios API Rest</option>
					<option value="estudiosEntregas.id_entorno=5" id="estudiosEntregas.id_entorno=5" selected>Mobile Tech</option>
				</select>
				<input id="exec" name="exec" type="hidden" value="dashboard"/>
				<input id="event" name="event" type="hidden" value="dashboard"/>
				<input id="entities" name="entities" type="hidden" value="resumenEntregas$resumenPeticiones"/>
			</td>
		</tr>
    </table>
    </form>
    
	<table>
		<tr>
			<td>
				<jsp:include page="${containerJSP_11}">
					<jsp:param name="idseries" value="_serie01" />
					<jsp:param name="width" value="700px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
			<td>
				<jsp:include page="${containerJSP_120}">
					<jsp:param name="idseries" value="_serie021" />
					<jsp:param name="width" value="480px" />
					<jsp:param name="height" value="230px" />
				</jsp:include>
				<jsp:include page="${containerJSP_121}">
					<jsp:param name="idseries" value="_serie022" />
					<jsp:param name="width" value="480px" />
					<jsp:param name="height" value="230px" />
				</jsp:include>
			</td>
		</tr>	
		<tr>
			<td>
				<jsp:include page="${containerJSP_21}">
					<jsp:param name="idseries" value="_serie03" />
					<jsp:param name="width" value="640px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
			<td>
				<jsp:include page="${containerJSP_22}">
					<jsp:param name="idseries" value="_serie04" />
					<jsp:param name="width" value="600px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
		</tr>	
		<tr>
			<td>
				<jsp:include page="${containerJSP_31}">
					<jsp:param name="idseries" value="_serie05" />
					<jsp:param name="width" value="640px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
			<td>
				<jsp:include page="${containerJSP_32}">
					<jsp:param name="idseries" value="_serie06" />
					<jsp:param name="width" value="600px" />
					<jsp:param name="height" value="360px" />
				</jsp:include>
			</td>
		</tr>	
		<tr>
			<td>
				<jsp:include page="${containerJSP_41}">
					<jsp:param name="idseries" value="_serie07" />
					<jsp:param name="width" value="640px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
			<td>
				<jsp:include page="${containerJSP_42}">
					<jsp:param name="idseries" value="_serie08" />
					<jsp:param name="width" value="600px" />
					<jsp:param name="height" value="460px" />
				</jsp:include>
			</td>
		</tr>
	</table>
