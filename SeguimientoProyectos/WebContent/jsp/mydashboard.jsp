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
				onChange="javascript:document.forms[0].submit();return true;" size="5" multiple>
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
			<td>
    			<label class="infoCls"  title="Servicio" id="estudiosEntregas.id_servicioLABEL" for="estudiosEntregas.id_servicio">
				&nbsp;&nbsp;Servicio&nbsp;
				</label>
				<select class="textInput" size="3" id="estudiosEntregas.id_servicio" name="estudiosEntregas.id_servicio" multiple>
				<option value="estudiosEntregas.id_servicio=1"  id="estudiosEntregas.id_servicio=1" selected>Servicio Mto. HOST</option>
				<option value="estudiosEntregas.id_servicio=2"  id="estudiosEntregas.id_servicio=2" selected>Servicio Nuevos Desarrollos Pros@</option>
				<option value="estudiosEntregas.id_servicio=3"  id="estudiosEntregas.id_servicio=3" selected>Servicio Mto. Pros@</option>
				</select>
			</td>
			<td>
			<label class="infoCls"  title="Aplicativo" id="estudiosEntregas.id_aplicativoLABEL" for="estudiosEntregas.id_aplicativo">&nbsp;&nbsp;Aplicativo&nbsp;</label>
				<select class="textInput" size="8" id="estudiosEntregas.id_aplicativo" name="estudiosEntregas.id_aplicativo" multiple>
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
			</td>
			<td>
				<label class="infoCls"  title="Rango periodo" id="estudiosEntregas.tipo_periodoLABEL" for="estudiosEntregas.tipo_periodo">&nbsp;&nbsp;Rango periodo&nbsp;</label>
				<select class="textInput" id="estudiosEntregas.tipo_periodo" name="estudiosEntregas.tipo_periodo" multiple>
				<option value="estudiosEntregas.tipo_periodo=1"  id="estudiosEntregas.tipo_periodo=1" selected>mensual</option>
				<option value="estudiosEntregas.tipo_periodo=2"  id="estudiosEntregas.tipo_periodo=2" selected>bimensual</option>
				<option value="estudiosEntregas.tipo_periodo=3"  id="estudiosEntregas.tipo_periodo=3" selected>trimestre</option>
				<option value="estudiosEntregas.tipo_periodo=4"  id="estudiosEntregas.tipo_periodo=4" selected>cuatrimestre</option>
				<option value="estudiosEntregas.tipo_periodo=5"  id="estudiosEntregas.tipo_periodo=5" selected>semestre</option>
				<option value="estudiosEntregas.tipo_periodo=6"  id="estudiosEntregas.tipo_periodo=6" selected>anual</option>
				<option value="estudiosEntregas.tipo_periodo=7"  id="estudiosEntregas.tipo_periodo=7" selected>bienio</option>
				<option value="estudiosEntregas.tipo_periodo=8"  id="estudiosEntregas.tipo_periodo=8" selected>trienio</option>
				<option value="estudiosEntregas.tipo_periodo=9"  id="estudiosEntregas.tipo_periodo=9" selected>cuatrienio</option>
				<option value="estudiosEntregas.tipo_periodo=10"  id="estudiosEntregas.tipo_periodo=10" selected>indeterminado</option>
				<option value="estudiosEntregas.tipo_periodo=11"  id="estudiosEntregas.tipo_periodo=11" selected>39 meses</option>
				<option value="estudiosEntregas.tipo_periodo=12"  id="estudiosEntregas.tipo_periodo=12" selected>41 meses</option>
				</select>
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
