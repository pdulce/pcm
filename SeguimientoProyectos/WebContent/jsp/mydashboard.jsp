<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
    
    
    <form action="prjManager" method="POST">
    	<input id="exec" name="exec" type="hidden" value="dashboard"/>
		<input id="event" name="event" type="hidden" value="dashboard"/>
		<input id="entities" name="entities" type="hidden" value="resumenEntregas$resumenPeticiones"/>
    
    <fieldset class="collapsible"><legend>Filtrar por criterios generales</legend>	
    	<div>
    		<label class="infoCls" title="Entorno" id="estudiosPeticiones.id_entornoLABEL" for="estudiosPeticiones.id_entorno">
    			&nbsp;&nbsp;Entorno&nbsp;
    		</label>
			<select class="textInput" id="estudiosEntregas.id_entorno" name="estudiosEntregas.id_entorno"
				onChange="javascript:submitDashboard('estudiosEntregas.id_entorno', 'estudiosPeticiones.id_entorno');" size="7" multiple>
					<option value="estudiosEntregas.id_entorno=1" id="estudiosEntregas.id_entorno=1" selected>HOST</option>
					<option value="estudiosEntregas.id_entorno=2" id="estudiosEntregas.id_entorno=2" selected>Pros@</option>
					<option value="estudiosEntregas.id_entorno=3" id="estudiosEntregas.id_entorno=3" selected>Web  Services</option>
					<option value="estudiosEntregas.id_entorno=4" id="estudiosEntregas.id_entorno=4" selected>Servicios API Rest</option>
					<option value="estudiosEntregas.id_entorno=5" id="estudiosEntregas.id_entorno=5" selected>Mobile Tech</option>
				</select>
				<input id="estudiosPeticiones.id_entorno" name="estudiosPeticiones.id_entorno" type="hidden" value=""/>
	
    			<label class="infoCls"  title="Servicio" id="estudiosEntregas.id_servicioLABEL" for="estudiosEntregas.id_servicio">
				&nbsp;&nbsp;Servicio&nbsp;
				</label>
			<select class="textInput" size="3" id="estudiosEntregas.id_servicio" name="estudiosEntregas.id_servicio" 
				onChange="javascript:vaciarOnSubmit('estudiosEntregas.id_aplicativo');
						vaciarOnSubmit('estudiosPeticiones.id_aplicativo');
						submitDashboard('estudiosEntregas.id_servicio', 'estudiosPeticiones.id_servicio');"
				 multiple>
				<option value="estudiosEntregas.id_servicio=1"  id="estudiosEntregas.id_servicio=1" selected>Servicio Mto. HOST</option>
				<option value="estudiosEntregas.id_servicio=2"  id="estudiosEntregas.id_servicio=2" selected>Servicio Nuevos Desarrollos Pros@</option>
				<option value="estudiosEntregas.id_servicio=3"  id="estudiosEntregas.id_servicio=3" selected>Servicio Mto. Pros@</option>
			</select>
			<input id="estudiosPeticiones.id_servicio" name="estudiosPeticiones.id_servicio" type="hidden" value=""/>

			<label class="infoCls"  title="Aplicativo" id="estudiosEntregas.id_aplicativoLABEL" for="estudiosEntregas.id_aplicativo">&nbsp;&nbsp;Aplicativo&nbsp;</label>
			<select class="textInput" size="7" id="estudiosEntregas.id_aplicativo" name="estudiosEntregas.id_aplicativo"
				onChange="javascript:vaciarOnSubmit('estudiosEntregas.id_servicio');vaciarOnSubmit('estudiosPeticiones.id_servicio');
						submitDashboard('estudiosEntregas.id_aplicativo', 'estudiosPeticiones.id_aplicativo');" 
				multiple>
				<option value="estudiosEntregas.id_aplicativo=1"  id="estudiosEntregas.id_aplicativo=1" selected>AFLO</option>
				<option value="estudiosEntregas.id_aplicativo=2"  id="estudiosEntregas.id_aplicativo=2" selected>APRO</option>
				<option value="estudiosEntregas.id_aplicativo=3"  id="estudiosEntregas.id_aplicativo=3" selected>AYFL</option>
				<option value="estudiosEntregas.id_aplicativo=4"  id="estudiosEntregas.id_aplicativo=4" selected>BISM</option>
				<option value="estudiosEntregas.id_aplicativo=5"  id="estudiosEntregas.id_aplicativo=5" selected>CMAR</option>
				<option value="estudiosEntregas.id_aplicativo=6"  id="estudiosEntregas.id_aplicativo=6" selected>CONT</option>
				<option value="estudiosEntregas.id_aplicativo=7"  id="estudiosEntregas.id_aplicativo=7" selected>FAM2</option>
				<option value="estudiosEntregas.id_aplicativo=8"  id="estudiosEntregas.id_aplicativo=8" selected>FAMA</option>
				<option value="estudiosEntregas.id_aplicativo=9"  id="estudiosEntregas.id_aplicativo=9" selected>FARM</option>
				<option value="estudiosEntregas.id_aplicativo=10"  id="estudiosEntregas.id_aplicativo=10" selected>FMAR</option>
				<option value="estudiosEntregas.id_aplicativo=11"  id="estudiosEntregas.id_aplicativo=11" selected>FOM2</option>
				<option value="estudiosEntregas.id_aplicativo=12"  id="estudiosEntregas.id_aplicativo=12" selected>FOMA</option>
				<option value="estudiosEntregas.id_aplicativo=13"  id="estudiosEntregas.id_aplicativo=13" selected>FRMA</option>
				<option value="estudiosEntregas.id_aplicativo=14"  id="estudiosEntregas.id_aplicativo=14" selected>GFOA</option>
				<option value="estudiosEntregas.id_aplicativo=15"  id="estudiosEntregas.id_aplicativo=15" selected>IMAG</option>
				<option value="estudiosEntregas.id_aplicativo=16"  id="estudiosEntregas.id_aplicativo=16" selected>INBU</option>
				<option value="estudiosEntregas.id_aplicativo=17"  id="estudiosEntregas.id_aplicativo=17" selected>INCA</option>
				<option value="estudiosEntregas.id_aplicativo=18"  id="estudiosEntregas.id_aplicativo=18" selected>INVE</option>
				<option value="estudiosEntregas.id_aplicativo=19"  id="estudiosEntregas.id_aplicativo=19" selected>ISMW</option>
				<option value="estudiosEntregas.id_aplicativo=20"  id="estudiosEntregas.id_aplicativo=20" selected>MEJP</option>
				<option value="estudiosEntregas.id_aplicativo=21"  id="estudiosEntregas.id_aplicativo=21" selected>MGEN</option>
				<option value="estudiosEntregas.id_aplicativo=22"  id="estudiosEntregas.id_aplicativo=22" selected>MIND</option>
				<option value="estudiosEntregas.id_aplicativo=23"  id="estudiosEntregas.id_aplicativo=23" selected>MOVI</option>
				<option value="estudiosEntregas.id_aplicativo=24"  id="estudiosEntregas.id_aplicativo=24" selected>OBIS</option>
				<option value="estudiosEntregas.id_aplicativo=25"  id="estudiosEntregas.id_aplicativo=25" selected>PAGO</option>
				<option value="estudiosEntregas.id_aplicativo=26"  id="estudiosEntregas.id_aplicativo=26" selected>PRES</option>
				<option value="estudiosEntregas.id_aplicativo=27"  id="estudiosEntregas.id_aplicativo=27" selected>SANI</option>
				<option value="estudiosEntregas.id_aplicativo=28"  id="estudiosEntregas.id_aplicativo=28" selected>SBOT</option>
				<option value="estudiosEntregas.id_aplicativo=29"  id="estudiosEntregas.id_aplicativo=29" selected>SIEB</option>
				<option value="estudiosEntregas.id_aplicativo=30"  id="estudiosEntregas.id_aplicativo=30" selected>TASA</option>
				<option value="estudiosEntregas.id_aplicativo=31"  id="estudiosEntregas.id_aplicativo=31" selected>TISM</option>
				<option value="estudiosEntregas.id_aplicativo=32"  id="estudiosEntregas.id_aplicativo=32" selected>WBOF</option>
				<option value="estudiosEntregas.id_aplicativo=33"  id="estudiosEntregas.id_aplicativo=33" selected>WISM</option>
				<option value="estudiosEntregas.id_aplicativo=34"  id="estudiosEntregas.id_aplicativo=34" selected>WSAO</option>
				<option value="estudiosEntregas.id_aplicativo=35"  id="estudiosEntregas.id_aplicativo=35" selected>WSCR</option>
				<option value="estudiosEntregas.id_aplicativo=36"  id="estudiosEntregas.id_aplicativo=36" selected>WSPX</option>
				<option value="estudiosEntregas.id_aplicativo=37"  id="estudiosEntregas.id_aplicativo=37" selected>WSRT</option>
			</select>
			<input id="estudiosPeticiones.id_aplicativo" name="estudiosPeticiones.id_aplicativo" type="hidden" value=""/>

			<label class="infoCls"  title="Escala" id="escaladoLabel" for="escalado">&nbsp;&nbsp;Escala&nbsp;</label>
			<select onChange="javascript:document.forms[0].submit();return true;" class="textInput" id="escalado" name="escalado" size="7">
					<option value="dayly" id="dayly">Diario</option>
					<option value="weekly" id="weekly">Semanal</option>
					<option value="monthly" id="monthly" selected>Mensual</option>
					<option value="3monthly" id="3monthly">Trimestral</option>
					<option value="6monthly" id="6monthly">Semestral</option>
					<option value="anualy" id="anualy">Anual</option>
					<option value="automatic" id="automatic">Automatico</option>
			</select>

			<label class="infoCls"  title="Operación sobre datos" id="escaladoLabel" for="escalado">&nbsp;&nbsp;Escala&nbsp;</label>
			<select onChange="javascript:document.forms[0].submit();return true;" class="textInput" 
				id="operation" name="operation" size="2">
					<option value="SUM" id="dayly">Totalizar</option>
					<option value="AVG" id="weekly" selected="selected">Promediar</option>
			</select>						
			</div>
		</fieldset>
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
	
	<script type="text/javascript">
		var val = document.getElementById('estudiosEntregas.id_entorno').value;
   		alert('cargando combos selected por defecto...id_entorno': + val);
	</script>
