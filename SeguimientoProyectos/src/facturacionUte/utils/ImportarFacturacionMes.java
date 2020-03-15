/**
 * 
 */
package facturacionUte.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cdd.common.exceptions.DatabaseException;
import cdd.common.utils.CommonUtils;
import cdd.domain.component.definitions.FieldView;
import cdd.domain.component.definitions.FieldViewSet;
import cdd.domain.component.definitions.IFieldView;
import cdd.domain.entitymodel.IDataAccess;
import cdd.domain.entitymodel.definitions.IEntityLogic;
import cdd.domain.entitymodel.definitions.ILogicTypes;
import cdd.domain.entitymodel.factory.EntityLogicFactory;
import cdd.dto.Data;
import cdd.strategies.IStrategy;
import facturacionUte.common.ConstantesModelo;
import facturacionUte.strategies.TuplaMesEjercicioEntradas;
import facturacionUte.strategies.concursos.StrategyCrearAgregadosMesesAppDptoServicio;
import facturacionUte.strategies.concursos.StrategyCrearAgregadosMesesColab;
import facturacionUte.strategies.concursos.StrategyGrabarUTsMesColabyApp;
import facturacionUte.strategies.concursos.StrategyRecalculateFacturacionMes;
import facturacionUte.strategies.previsiones.StratBorrarAnualidadesPrevision;
import facturacionUte.strategies.previsiones.StratCrearAnualidadesPrevision;

/**
 * @author Pedro Dulce       
 */


public class ImportarFacturacionMes {

	private static final String ERR_FICHERO_EXCEL_FORMATO_XLS = "ERR_FICHERO_EXCEL_FORMATO_XLS";
	private static final String ERR_FICHERO_EXCEL_NO_LOCALIZADO = "ERR_FICHERO_EXCEL_NO_LOCALIZADO";
	private static final String ERR_IMPORTANDO_FICHERO_EXCEL = "ERR_IMPORTANDO_FICHERO_EXCEL";
	private static final String COLABORADOR = "COLABORADOR", EMPRESA= "EMPRESA", CATEGORIA="Categoria", UTs="UTs", APP ="Aplicativo", AREA = "Area", OBSERVACIONES= "Observaciones";

	private static final int HOJA_0_MAPEO_COLABORADOR_2_NOMBRE_COLUMN = 0, HOJA_0_MAPEO_EMPRESAUTE_2_NOMBRE_COLUMN = 1, HOJA_0_MAPEO_CATEGORIA_PROFESIONAL_2_TIPO_CATEGORIA_COLUMN = 2, HOJA_0_MAPEO_FACTURACIONMESPORCOLABORADORYAPP_5_UTS_COLUMN = 3;
	private static final int HOJA_1_MAPEO_PROYECTO_2_CODIGOE_COLUMN = 0, HOJA_1_MAPEO_SERVICIO_2_NOMBREE_COLUMN = 1, HOJA_1_MAPEO_OBSERVACIONESE_COLUMN = 2, HOJA_1_MAPEO_COLABORADOR_2_NOMBRE_COLUMN = 3;
	
	private static int columnasHOJA_0_[] = new int[]{-1,-1,-1,-1}, columnasHOJA_1_[] = new int[]{-1,-1,-1,-1};
	
	private static FieldViewSet patronRecord;
	
	private IDataAccess dataAccess;

	private static IEntityLogic tarifaPerfilEntidad, simulacionEntidad, concursoEntidad, empresaEntidad, colaboradorEntidad, facturacionMesColaboradoryAppEntidad, appEntidad, servicioEntidad, dptoEntidad, 
	facturacionMesColaborador, facturacionMesApp, categoriaProfesional, mesEntidad, appColaboradorEntidad;
	
	protected static Logger log = Logger.getLogger(ImportarFacturacionMes.class.getName());
	
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private static final String getColumName(int hoja, final int position){
		if (hoja == 0){
			switch (position){
			case 0:
				return "COLABORADOR";
			case 1:
				return "UTE";			
			case 2:
				return "CAT.";
			default:
				return "Horas + HH,99";
			}
		}else{
			switch (position){
			case 0:
				return "Aplic.";
			case 1:
				return "Area";			
			case 2:
				return "Observaciones";
			default:
				return "COLABORADOR";
			}
		}
	}
	
	private static final String getFieldnameOfColumnName(String columnName){
		if ("COLABORADOR".equals(columnName)){
			return COLABORADOR;
		}else if ("COLABORADOR".equals(columnName)){
			return COLABORADOR;
		}else if ("UTE".equals(columnName)){
			return EMPRESA;
		}else if ("CAT.".equals(columnName)){
			return CATEGORIA;
		}else if ("Horas + HH,99".equals(columnName)){
			return UTs;
		}else if ("Aplic.".equals(columnName)){
			return APP;
		}else if ("Area".equals(columnName)){
			return AREA;
		}else if ("Observaciones".equals(columnName)){
			return OBSERVACIONES;
		}
		return "NOT FOUND";
	}

	private void initEntities(final Data data_) {
		if (patronRecord == null) {
			try {
				String lang = data_.getEntitiesDictionary();
				colaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.COLABORADOR_ENTIDAD);
				concursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.CONCURSO_ENTIDAD);
				categoriaProfesional = EntityLogicFactory.getFactoryInstance().getEntityDef(data_.getEntitiesDictionary(), ConstantesModelo.CATEGORIA_PROFESIONAL_ENTIDAD);
				facturacionMesColaborador = EntityLogicFactory.getFactoryInstance().getEntityDef(data_.getEntitiesDictionary(), ConstantesModelo.FACTURACIONMESPORCOLABORADOR_ENTIDAD);
				facturacionMesColaboradoryAppEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_ENTIDAD);
				facturacionMesApp = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.FACTURACIONMESPORAPP_ENTIDAD);
				appEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.PROYECTO_ENTIDAD);
				servicioEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.SERVICIO_ENTIDAD);
				dptoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.DEPARTAMENTO_ENTIDAD);
				appColaboradorEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.APPS_COLABORADOR_ENTIDAD);
				empresaEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.EMPRESAUTE_ENTIDAD);
				simulacionEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.DATOS_PREVISION_CONTRATO_ENTIDAD);
				mesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.MES_ENTIDAD);
				tarifaPerfilEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.CATEGORIA_PROFESIONAL_ENTIDAD);
				
				Collection<IFieldView> mappings = new ArrayList<IFieldView>();
				
				String contextName = "patronExcelRecord_";
				IFieldView f1 = new FieldView(contextName);
				f1.setUserName(COLABORADOR);
				f1.setType(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_2_NOMBRE).getAbstractField().getType());
				f1.setUserDefined(true);
				
				IFieldView f2 = new FieldView(contextName);
				f2.setUserName(EMPRESA);
				f2.setType(empresaEntidad.searchField(ConstantesModelo.EMPRESAUTE_2_NOMBRE).getAbstractField().getType());
				f2.setUserDefined(true);
				
				IFieldView f3 = new FieldView(contextName);
				f3.setUserName(CATEGORIA);
				f3.setType(categoriaProfesional.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_2_TIPO_CATEGORIA).getAbstractField().getType());
				f3.setUserDefined(true);
				
				IFieldView f4 = new FieldView(contextName);
				f4.setUserName(UTs);
				f4.setType(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_5_UTS).getAbstractField().getType());
				f4.setUserDefined(true);

				IFieldView f5 = new FieldView(contextName);
				f5.setUserName(APP);
				f5.setType(appEntidad.searchField(ConstantesModelo.PROYECTO_2_CODIGO).getAbstractField().getType());
				f5.setUserDefined(true);
				
				IFieldView f6 = new FieldView(contextName);
				f6.setUserName(AREA);
				f6.setType(servicioEntidad.searchField(ConstantesModelo.SERVICIO_2_NOMBRE).getAbstractField().getType());
				f6.setUserDefined(true);
				
				IFieldView f7 = new FieldView(contextName);
				f7.setUserName(OBSERVACIONES);
				f7.setType(servicioEntidad.searchField(ConstantesModelo.SERVICIO_2_NOMBRE).getAbstractField().getType());//igual que el appname
				f7.setUserDefined(true);

				mappings.add(f1);
				mappings.add(f2);
				mappings.add(f3);
				mappings.add(f4);
				mappings.add(f5);
				mappings.add(f6);
				mappings.add(f7);
				
				//columnas en la Excel empezamos a contar desde la 0-osima la primera (celda A es la 0-oisma)
				columnasHOJA_0_[HOJA_0_MAPEO_COLABORADOR_2_NOMBRE_COLUMN]= 4;
				columnasHOJA_0_[HOJA_0_MAPEO_EMPRESAUTE_2_NOMBRE_COLUMN]= 5;
				columnasHOJA_0_[HOJA_0_MAPEO_CATEGORIA_PROFESIONAL_2_TIPO_CATEGORIA_COLUMN]= 7;
				columnasHOJA_0_[HOJA_0_MAPEO_FACTURACIONMESPORCOLABORADORYAPP_5_UTS_COLUMN]= 13;
				
				columnasHOJA_1_[HOJA_1_MAPEO_PROYECTO_2_CODIGOE_COLUMN]= 0;
				columnasHOJA_1_[HOJA_1_MAPEO_SERVICIO_2_NOMBREE_COLUMN]= 1;
				columnasHOJA_1_[HOJA_1_MAPEO_OBSERVACIONESE_COLUMN]= 2;
				columnasHOJA_1_[HOJA_1_MAPEO_COLABORADOR_2_NOMBRE_COLUMN]= 6;
				
				patronRecord = new FieldViewSet("user", contextName , mappings);
				
			} catch (Throwable exc) {
				throw new RuntimeException("Error in initEntities method: ", exc);
			}
		}
	}
	
	public ImportarFacturacionMes(IDataAccess dataAccess_, Data req) {
		this.dataAccess = dataAccess_;
		initEntities(req);
	}
	
	private static long getIdForCategoriaProf4AT(String categoria){
		if ("TSA".equals(categoria) || "TSB".equals(categoria)){
			return 1;
		}else{
			return 3;
		}
	}
	
	private static long getIdForCDINSS(){
		return 1;
	}
	
 	private int procesarFilas(final XSSFSheet sheetNewVersion_HOJA_0, final XSSFSheet sheetNewVersion_HOJA_1, final Data data, final FieldViewSet mesFSet, final int ejercicio, final Long idContrato) throws Throwable {

		int nrow = 3;//la primera de la Excel es la 0-osima al recorrer el fichero con la api
		//buscamos las columnas-posiciones que vamos a extrear al iterar cada columna
		
		boolean computadaFila = true;
		
		while (computadaFila) {// while
			
			final Row rowIEsima_Hoja_0 = sheetNewVersion_HOJA_0.getRow(nrow);
			final Row rowIEsima_Hoja_1 = sheetNewVersion_HOJA_1.getRow(nrow);
			if (rowIEsima_Hoja_0 == null || rowIEsima_Hoja_1 == null) {
				break;
			}
			
			FieldViewSet fila = patronRecord.copyOf();			
			
			/** PRIMERO: RECORREMOS LA FICHA-HOJA 0-oSIMA**/
			for (int nColum = 0; computadaFila && nColum < columnasHOJA_0_.length; nColum++) {
				try {
					final Cell cell = rowIEsima_Hoja_0.getCell(columnasHOJA_0_[nColum]);
					if (cell == null) {
						computadaFila = false;
						break;//salto a otra fila; fin en la 95-iosima
					}

					Serializable valueCell = null;
					if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						valueCell = cell.getNumericCellValue();
					} else {
						valueCell = cell.getStringCellValue();
					}
					if (valueCell == null || "".equals(valueCell.toString().trim())){
						computadaFila = false;
						break;//salto a otra fila
					}
					
					String columnName = getColumName(0, nColum);
					IFieldView fViewOfColumn = fila.getFieldView(getFieldnameOfColumnName(columnName));
					if (fViewOfColumn == null){
						continue;
					}
					
					String typeBBDDOfColumn = fViewOfColumn.getType();
					if (ILogicTypes.DATE.equals(typeBBDDOfColumn) || ILogicTypes.TIMESTAMP.equals(typeBBDDOfColumn) ) {						
						try {
							valueCell = valueCell.equals("") ? null : CommonUtils.myDateFormatter.parse(valueCell);
						}catch (ParseException parseExc) {
							valueCell = cell.getDateCellValue();
						}
					} else if (ILogicTypes.LONG.equals(typeBBDDOfColumn)) {
						valueCell = valueCell.equals("") ? null : Long.valueOf(valueCell.toString());
					} else if (ILogicTypes.INTEGER.equals(typeBBDDOfColumn) || ILogicTypes.INT.equals(typeBBDDOfColumn)) {
						valueCell = valueCell.equals("") ? null : Integer.valueOf(valueCell.toString());
					} else if (ILogicTypes.DECIMAL.equals(typeBBDDOfColumn)) {
						valueCell = valueCell.equals("") ? null : CommonUtils.numberFormatter.parse(valueCell);
					}
					fila.setValue(fViewOfColumn.getQualifiedContextName(), valueCell);					
				}
				catch (Throwable excc) {
					excc.printStackTrace();
					ImportarFacturacionMes.log.log(Level.SEVERE, "Error en columna al importar fila..." + nrow);
					return -1;
				}
			}// for columnas de la HOJA 0
			
			/** SEGUNDO: RECORREMOS LA FICHA-HOJA 1-oSIMA**/
			for (int nColum = 0; computadaFila && nColum < columnasHOJA_1_.length; nColum++) {
				try {
					final Cell cell = rowIEsima_Hoja_1.getCell(columnasHOJA_1_[nColum]);
					if (cell == null) {
						if (nColum==HOJA_0_MAPEO_FACTURACIONMESPORCOLABORADORYAPP_5_UTS_COLUMN){
							computadaFila = false;
							break;//salto a otra fila; fin en la 95-iosima						
						}else{
							continue;
						}
					}

					Serializable valueCell = null;
					if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						valueCell = cell.getNumericCellValue();
					} else {
						valueCell = cell.getStringCellValue();
					}
					if (valueCell == null || "".equals(valueCell.toString().trim())){
						computadaFila = false;
						break;//salto a otra fila
					}
					
					String columnName = getColumName(1, nColum);
					IFieldView fViewOfColumn = fila.getFieldView(getFieldnameOfColumnName(columnName));
					if (fViewOfColumn == null){
						continue;
					}
					
					String typeBBDDOfColumn = fViewOfColumn.getType();
					if (ILogicTypes.DATE.equals(typeBBDDOfColumn) || ILogicTypes.TIMESTAMP.equals(typeBBDDOfColumn) ) {					
						try {
							valueCell = valueCell.equals("") ? null : CommonUtils.myDateFormatter.parse(valueCell);
						}catch (ParseException parseExc) {
							valueCell = cell.getDateCellValue();
						}
					} else if (ILogicTypes.LONG.equals(typeBBDDOfColumn)) {
						valueCell = valueCell.equals("") ? null : Long.valueOf(valueCell.toString());
					} else if (ILogicTypes.INTEGER.equals(typeBBDDOfColumn) || ILogicTypes.INT.equals(typeBBDDOfColumn)) {
						valueCell = valueCell.equals("") ? null : Integer.valueOf(valueCell.toString());
					} else if (ILogicTypes.DECIMAL.equals(typeBBDDOfColumn)) {
						valueCell = valueCell.equals("") ? null : CommonUtils.numberFormatter.parse(valueCell);
					}
					fila.setValue(fViewOfColumn.getQualifiedContextName(), valueCell);
				}
				catch (Throwable excc) {
					excc.printStackTrace();
					ImportarFacturacionMes.log.log(Level.SEVERE, "Error en columna al importar fila..." + nrow);
					return -1;
				}			
			}// for columnas de la HOJA 1
			if (computadaFila){
				grabarEnBBDD(data, fila, mesFSet, ejercicio, idContrato);
				nrow++;
			}
			
		}//for filas
		
		//actualizo las simulaciones, una vez, al final
		FieldViewSet filterOfSimulaciones = new FieldViewSet(simulacionEntidad);
		filterOfSimulaciones.setValue(simulacionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_2_ID_CONCURSO).getName(), idContrato);
		List<FieldViewSet> previsiones = dataAccess.searchByCriteria(filterOfSimulaciones);
		for (int prev=0;prev<previsiones.size();prev++){
			FieldViewSet prevision = previsiones.get(prev);
			new StratBorrarAnualidadesPrevision().borrarAnualidadesPrevision(prevision, data, dataAccess);
			FieldViewSet previsionNew = new FieldViewSet(simulacionEntidad);
			previsionNew.setValue(simulacionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_1_ID).getName(), prevision.getValue(simulacionEntidad.searchField(ConstantesModelo.DATOS_PREVISION_CONTRATO_1_ID).getName()));
			new StratCrearAnualidadesPrevision().crearAnualidadesPrevision(previsionNew, data, dataAccess);
		}
				
		return nrow-4;
	}
	
	/*** ANTES de grabar una imputacion de un colaborador-mes-ejercicio comprobamos
	 * 1. Miramos si existe el colaborador (filtramos por idCategoria, nomre y apellidos:
	 *  	 Si esto dado de alta el colaborador: 
	 *  		     Buscamos el registro de imputacion del colaborador en ese mes
	 *  			 	entidad: FACTURACIONMESPORCOLABORADOR--filtramos por mes y ejercicio e idColaborador
	 *  
	 *               	Si existe, vamos al paso ii) 
	 *               	
	 *               	Si no existe el registro mes-idColaborador, creamos ese registro y pasamos al ii)
	 *               	
	 *                  ii) Buscamos el primer registro de imputacion en mes y app (de la entidad FACTURACIONMESPORCOLABORADORYAPP) filtrando por mes y ejercicio e idColaborador 
	 *               
	 *   					Si no existe app-mes-idColaborador, creamos ese registro con la primera app asignada al colaborador, y pasamos al iii) (solucion para una app por colaborador)
	 *   					
	 *   					iii) Tomamos ese registro, y actualizamos las UTs, y llamamos a la estrategia de actualizacion de imputaciones/facturacion del resto de entidades (StrategyRecalculateFacturacionMes)
	 *  			
	 *  
	 *  	Si no existe, continuamos con la siguiente iteracion dentro del bucle
	 */		
	private int grabarEnBBDD(final Data data, FieldViewSet registro, final FieldViewSet mesFSet, final int ejercicio, final Long idContrato) throws Throwable{
		
		this.dataAccess.setAutocommit(false);
		int numImputadas = 0;
		data.setAttribute("noUpdateSimul", "NO_UPDATE");
		Long idMes = (Long) mesFSet.getValue(mesEntidad.searchField(ConstantesModelo.MES_1_ID).getName());
		
		FieldViewSet concursoDeServicio = new FieldViewSet(concursoEntidad);
		concursoDeServicio.setValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_1_ID).getName(), idContrato);
		concursoDeServicio = this.dataAccess.searchByCriteria(concursoDeServicio).get(0);//tengo el objeto concurso		
		
		String nombreyApellidosDeColaborador = (String) registro.getValue(COLABORADOR);
		String tipoCatColaborador = (String) registro.getValue(CATEGORIA);
		tipoCatColaborador = CommonUtils.cleanWhitespaces(tipoCatColaborador);
		String empresaUTE = (String) registro.getValue(EMPRESA);
		empresaUTE = CommonUtils.cleanWhitespaces(empresaUTE);
		Double numUTsImputadas = (Double) registro.getValue(UTs);
		
		/** TRATAMIENTO DE LA PESTAoA 1 **/
		/*** OJO: Si el campo Observaciones viene consignado, entonces:
		- creamos el SERVICIO (columna Aplic.)
		- Creamos el Dpto (columna orea)
		- Creamos las apps (columna Observaciones)
		          Si el campo Observaciones NO viene consignado, entonces:
		- Creamos el SERVICIO(campo orea) si no existe
		- Creamos el Dpto(campo orea) si no existe
		- Creamos la app(campo Aplic.) si no existe			
		 ***/

		String[] appsName = CommonUtils.cleanWhitespaces((String) registro.getValue(APP)).split(",");
		String servicioName = (String) registro.getValue(AREA);
		servicioName = servicioName == null ? "" : CommonUtils.cleanWhitespaces(servicioName);		
		String observaciones = (String) registro.getValue(OBSERVACIONES);
		observaciones = observaciones==null ? "" : CommonUtils.cleanWhitespaces(observaciones);
		String dpto = servicioName;
		if (!"".equals(observaciones)){
			dpto = servicioName;
			servicioName = appsName[0];				
			appsName = observaciones.split(",");
		}
		
		try {
			String[] colaboraSplitter = nombreyApellidosDeColaborador.split(",");
			if (colaboraSplitter.length < 2){
				return 0;
			}				
			/*** EMPEZAMOS ***/
			
			/*** GRUPO DE INFORMACION ESTATICA ***/
			FieldViewSet empresa = new FieldViewSet(empresaEntidad);
			empresa.setValue(empresaEntidad.searchField(ConstantesModelo.EMPRESAUTE_2_NOMBRE).getName(), empresaUTE);
			List<FieldViewSet> listaEmpresafPorFiltro = this.dataAccess.searchByCriteria(empresa);
			if (listaEmpresafPorFiltro.isEmpty()) {
				//si no existe la empresa, la creamos
				int ok = this.dataAccess.insertEntity(empresa);
				if (ok == 1){//recupero el id-empresa
					empresa = this.dataAccess.searchByCriteria(empresa).get(0);
				}
			}else{
				empresa = listaEmpresafPorFiltro.get(0);
			}
			
			Long idEmpresa = (Long) empresa.getValue(empresaEntidad.searchField(ConstantesModelo.EMPRESAUTE_1_ID).getName());
			
			FieldViewSet catProfesional = new FieldViewSet(categoriaProfesional);
			catProfesional.setValue(categoriaProfesional.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_2_TIPO_CATEGORIA).getName(), tipoCatColaborador);
			List<FieldViewSet> listaCatProfPorFiltro = this.dataAccess.searchByCriteria(catProfesional);
			if (listaCatProfPorFiltro.isEmpty()) {
				//si no existe esa cat. profesional, no podemos crearla, levantamos la liebre
				throw new Throwable("La categoria profesional " + " debe existir en base de datos");//si no existe, no tenemos manera de imputar esas hora en euros
			}
			catProfesional = listaCatProfPorFiltro.get(0);
			Long idcatProf = (Long) catProfesional.getValue(categoriaProfesional.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_1_ID_CATEGORIA).getName());
					
			String apellidos = colaboraSplitter[0].trim(), nombre = colaboraSplitter[1];
			nombre = nombre.trim();
			
			Long idServicio = null;
			FieldViewSet servicioFset = new FieldViewSet(servicioEntidad);
			servicioFset.setValue(servicioEntidad.searchField(ConstantesModelo.SERVICIO_2_NOMBRE).getName(), servicioName);
			servicioFset.setValue(servicioEntidad.searchField(ConstantesModelo.SERVICIO_3_UNIDAD_ORG).getName(), getIdForCDINSS());
			List<FieldViewSet> listaServiciosPorFiltro2 = this.dataAccess.searchByCriteria(servicioFset);
			if (listaServiciosPorFiltro2.isEmpty()) {
				//creamos el servicio
				int ok = this.dataAccess.insertEntity(servicioFset);
				if (ok != 1){
					throw new Exception("Error creando servicio " + servicioName);
				}
				servicioFset = this.dataAccess.searchByCriteria(servicioFset).get(0);						
			}else{
				servicioFset = listaServiciosPorFiltro2.get(0);
			}							
			idServicio = (Long) servicioFset.getValue(servicioEntidad.searchField(ConstantesModelo.SERVICIO_1_ID).getName());
			
			// Creamos el dpto. en caso de no existir
			Long idDpto = null;
			FieldViewSet dptoFset = new FieldViewSet(dptoEntidad);
			dptoFset.setValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_2_NOMBRE).getName(), dpto);
			dptoFset.setValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_3_SERVICIO).getName(), idServicio);
			List<FieldViewSet> listaDptosPorFiltro2 = this.dataAccess.searchByCriteria(dptoFset);
			if (listaDptosPorFiltro2.isEmpty()) {
				//creamos el servicio
				int ok = this.dataAccess.insertEntity(dptoFset);
				if (ok != 1){
					throw new Exception("Error creando dpto " + dpto);
				}
				dptoFset = this.dataAccess.searchByCriteria(dptoFset).get(0);						
			}else{
				dptoFset = listaDptosPorFiltro2.get(0);
			}
			idDpto = (Long) dptoFset.getValue(dptoEntidad.searchField(ConstantesModelo.DEPARTAMENTO_1_ID).getName());
			
			
			FieldViewSet colaboradorExistente = new FieldViewSet(colaboradorEntidad);
			colaboradorExistente.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_2_NOMBRE).getName(), nombre);
			colaboradorExistente.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_3_APELLIDOS).getName(), apellidos);
			colaboradorExistente.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_6_ID_CATEGORIA).getName(), idcatProf);
			colaboradorExistente.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_8_ID_EMPRESA_FACTURACION).getName(), idEmpresa);
			List<FieldViewSet> listaColaboradoresPorFiltro = this.dataAccess.searchByCriteria(colaboradorExistente);
			if (listaColaboradoresPorFiltro.isEmpty()) { //si no existe ese colaborador/a, lo creamos, y llamamos a la estrategia que crea las hojas de facturacion de este colaborador en ese concurso
				
				//rellenamos el resto de informacion que nos falta					
				colaboradorExistente.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_4_RESPONSABILIDAD).getName(), getIdForCategoriaProf4AT(tipoCatColaborador));
				colaboradorExistente.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_5_RELACION_EXTINGUIDA).getName(), false);
				colaboradorExistente.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_11_EMAIL).getName(), "");
				colaboradorExistente.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_12_OBSERVACIONES).getName(), "");
				colaboradorExistente.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_13_ID_CONCURSO).getName(), idContrato);					
				int ok = dataAccess.insertEntity(colaboradorExistente);					
				if (ok != 1){
					throw new Exception("Error creando colaborador " + nombre + " " + apellidos);
				}
				
				FieldViewSet colaboradorExistente2 = new FieldViewSet(colaboradorEntidad);
				colaboradorExistente2.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_2_NOMBRE).getName(), nombre);
				colaboradorExistente2.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_3_APELLIDOS).getName(), apellidos);
				colaboradorExistente2.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_6_ID_CATEGORIA).getName(), idcatProf);
				colaboradorExistente2.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_8_ID_EMPRESA_FACTURACION).getName(), idEmpresa);
				colaboradorExistente2 = this.dataAccess.searchByCriteria(colaboradorExistente2).get(0);
				
				Long idColaborador = (Long) colaboradorExistente2.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_1_ID).getName());
				colaboradorExistente.setValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_1_ID).getName(), idColaborador);
				
				List<FieldViewSet> fset = new ArrayList<FieldViewSet>();
				fset.add(colaboradorExistente);
				IStrategy strat = new StrategyCrearAgregadosMesesColab();
				strat.doBussinessStrategy(data, this.dataAccess, fset);
				//ahora tomamos la app de este colaborador, y creamos la asignacion app-colaborador, y luego invocamos la estrategia StrategyGrabarUTsMesColabyApp
				
				// Recorremos las apps asignadas al colaborador, y comprobamos si existe la app
				for (int ap=0;ap<appsName.length;ap++){
					String appName = appsName[ap];
					if ("ISM".equals(appName)){
						continue;//imputan al ISM
					}else if (appName.length() < 4 ){
						appName += "E";
					}
					
					Long idApp = null;
					FieldViewSet aplicacion = new FieldViewSet(appEntidad);
					aplicacion.setValue(appEntidad.searchField(ConstantesModelo.PROYECTO_2_CODIGO).getName(), appName);
					aplicacion.setValue(appEntidad.searchField(ConstantesModelo.PROYECTO_5_ID_CONCURSO).getName(), idContrato);
					List<FieldViewSet> listaAppsPorFiltro2 = this.dataAccess.searchByCriteria(aplicacion);
					if (listaAppsPorFiltro2.isEmpty()) {//creamos la app; necesitamos el dpto. al que pertenece...
						aplicacion.setValue(appEntidad.searchField(ConstantesModelo.PROYECTO_3_NOMBREPROYECTO).getName(), appName);
						aplicacion.setValue(appEntidad.searchField(ConstantesModelo.PROYECTO_7_DEPARTAMENTO).getName(), idDpto);
						ok = this.dataAccess.insertEntity(aplicacion);
						if (ok != 1){
							throw new Exception("Error creando aplicacion " + appName);
						}
						aplicacion = this.dataAccess.searchByCriteria(aplicacion).get(0);
						//si se ha creado bien la app, entonces llamamos a la estrategia de creacion de las hojas de facturacion de esta app, dpto., servicio,...							
						List<FieldViewSet> fset33 = new ArrayList<FieldViewSet>();
						fset33.add(aplicacion);
						IStrategy strat33 = new StrategyCrearAgregadosMesesAppDptoServicio();
						strat33.doBussinessStrategy(data, this.dataAccess, fset33);							
					}else{
						aplicacion = listaAppsPorFiltro2.get(0);
					}
					idApp = (Long) aplicacion.getValue(appEntidad.searchField(ConstantesModelo.PROYECTO_1_ID).getName());
					
					FieldViewSet nuevaAppDeColab = new FieldViewSet(appColaboradorEntidad);
					nuevaAppDeColab.setValue(appColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_2_COLABORADOR).getName(), idColaborador);
					nuevaAppDeColab.setValue(appColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_3_APP).getName(), idApp);
					ok = this.dataAccess.insertEntity(nuevaAppDeColab);
					if (ok != 1){
						throw new Exception("Error asignando app "+ appName + " al colaborador " + apellidos + ", " + nombre);
					}										
				}//for
					
			}else{
				colaboradorExistente = listaColaboradoresPorFiltro.get(0);					
			}
			
			Long idColaborador = (Long) colaboradorExistente.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_1_ID).getName());
			
			Long idContratoColab = (Long) colaboradorExistente.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_13_ID_CONCURSO).getName());
			if (idContratoColab.longValue() != idContrato.longValue()){//si es de otro contrato...
				throw new Exception("Error: el colaborador " + nombre + " " + apellidos + " pertenece a otro contrato/concurso");
			}
							
			//obtenemos la primera app asignada a este colaborador, si no tiene, salgo sin error, o sea, continue;
			FieldViewSet filtroBusquedaAppsDeColaborador = new FieldViewSet(appColaboradorEntidad);
			filtroBusquedaAppsDeColaborador.setValue(appColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_2_COLABORADOR).getName(), idColaborador);
			List<FieldViewSet> listaAppsDeColaborador = this.dataAccess.searchByCriteria(filtroBusquedaAppsDeColaborador);
			
			int numOfApps4Colaborador = listaAppsDeColaborador.size();				
			for (int iAp=0;iAp<listaAppsDeColaborador.size();iAp++){
				FieldViewSet appDeColaborador = listaAppsDeColaborador.get(iAp);// osi tiene mos de una app asignada?
				Long idApp = (Long) appDeColaborador.getValue(appColaboradorEntidad.searchField(ConstantesModelo.APPS_COLABORADOR_3_APP).getName());
				
						/*** GRUPO DE INFORMACION DE FACTURACION ***/												 
				FieldViewSet facturaMesColaborador = new FieldViewSet(facturacionMesColaborador);
				facturaMesColaborador.setValue(facturacionMesColaborador.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_2_ANYO).getName(), ejercicio);
				facturaMesColaborador.setValue(facturacionMesColaborador.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_3_MES).getName(), idMes);
				facturaMesColaborador.setValue(facturacionMesColaborador.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_4_ID_COLABORADOR).getName(), idColaborador);					
				facturaMesColaborador = this.dataAccess.searchByCriteria(facturaMesColaborador).get(0);								
				Long idFraColaboradorMes = (Long) facturaMesColaborador.getValue(facturacionMesColaborador.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADOR_1_ID).getName());
				
				FieldViewSet facturaMesApp = new FieldViewSet(facturacionMesApp);
				facturaMesApp.setValue(facturacionMesApp.searchField(ConstantesModelo.FACTURACIONMESPORAPP_4_ID_CONCURSO).getName(), idContrato);
				facturaMesApp.setValue(facturacionMesApp.searchField(ConstantesModelo.FACTURACIONMESPORAPP_2_ANYO).getName(), ejercicio);
				facturaMesApp.setValue(facturacionMesApp.searchField(ConstantesModelo.FACTURACIONMESPORAPP_3_MES).getName(), idMes);
				facturaMesApp.setValue(facturacionMesApp.searchField(ConstantesModelo.FACTURACIONMESPORAPP_7_ID_APP).getName(), idApp);
				List<FieldViewSet> fraApp = this.dataAccess.searchByCriteria(facturaMesApp);
				if (fraApp.isEmpty()){
					continue;
				}
				facturaMesApp = new FieldViewSet(facturacionMesApp);
				facturaMesApp.setValue(facturacionMesApp.searchField(ConstantesModelo.FACTURACIONMESPORAPP_2_ANYO).getName(), ejercicio);
				facturaMesApp.setValue(facturacionMesApp.searchField(ConstantesModelo.FACTURACIONMESPORAPP_3_MES).getName(), idMes);
				facturaMesApp.setValue(facturacionMesApp.searchField(ConstantesModelo.FACTURACIONMESPORAPP_7_ID_APP).getName(), idApp);
				facturaMesApp = this.dataAccess.searchByCriteria(facturaMesApp).get(0);
				
				Long idFraAppMes = (Long) facturaMesApp.getValue(facturacionMesApp.searchField(ConstantesModelo.FACTURACIONMESPORAPP_1_ID).getName());
				
				Double fraccionImputadas = numUTsImputadas/Double.valueOf(numOfApps4Colaborador);
				Long idTarifaColaborador= (Long) colaboradorExistente.getValue(colaboradorEntidad.searchField(ConstantesModelo.COLABORADOR_6_ID_CATEGORIA).getName());
				FieldViewSet tarifaPerfil = new FieldViewSet(tarifaPerfilEntidad);
				tarifaPerfil.setValue(tarifaPerfilEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_1_ID_CATEGORIA).getName(), idTarifaColaborador);
				tarifaPerfil = dataAccess.searchByCriteria(tarifaPerfil).get(0);
				Double tarifa_colaborador= (Double)  tarifaPerfil.getValue(tarifaPerfilEntidad.searchField(ConstantesModelo.CATEGORIA_PROFESIONAL_4_IMPORTE_HORA).getName());
				Double fraccionImputadas_Euros = CommonUtils.roundWith2Decimals(fraccionImputadas*tarifa_colaborador);//euros
									
				FieldViewSet facturaMesColaboradoryApp = new FieldViewSet(facturacionMesColaboradoryAppEntidad);
				facturaMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_3_ID_COLABORADOR).getName(), idColaborador);
				facturaMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_4_ID_PROYECTO).getName(), idApp);
				facturaMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_2_IDFACTURACIONCOLAB).getName(), idFraColaboradorMes);
				facturaMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_6_ID_FACTMESAPP).getName(), idFraAppMes);
				List<FieldViewSet> listaFacturasMesColaboradoryAppPorFiltro = this.dataAccess.searchByCriteria(facturaMesColaboradoryApp);
				if (listaFacturasMesColaboradoryAppPorFiltro.isEmpty()){
					List<FieldViewSet> fset2 = new ArrayList<FieldViewSet>();
					fset2.add(appDeColaborador);
					IStrategy strat2 = new StrategyGrabarUTsMesColabyApp();//esta estrategia crea los objetos fra-mes-app, si ya existen los objetos fra-mes y fra-app, pero no los mes-app-colaborador
					strat2.doBussinessStrategy(data, this.dataAccess, fset2);
				}
				//deben existir ahora, se hayan creado antes o en este momento
				facturaMesColaboradoryApp = new FieldViewSet(facturacionMesColaboradoryAppEntidad);
				facturaMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_3_ID_COLABORADOR).getName(), idColaborador);
				facturaMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_4_ID_PROYECTO).getName(), idApp);
				facturaMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_2_IDFACTURACIONCOLAB).getName(), idFraColaboradorMes);
				facturaMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_6_ID_FACTMESAPP).getName(), idFraAppMes);
				listaFacturasMesColaboradoryAppPorFiltro = this.dataAccess.searchByCriteria(facturaMesColaboradoryApp);
				if (listaFacturasMesColaboradoryAppPorFiltro.isEmpty()){
					throw new Exception("Error: la hoja mes-app-colaborador no existe");
				}
				
				facturaMesColaboradoryApp = listaFacturasMesColaboradoryAppPorFiltro.get(0);					
				facturaMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_5_UTS).getName(), fraccionImputadas);
				facturaMesColaboradoryApp.setValue(facturacionMesColaboradoryAppEntidad.searchField(ConstantesModelo.FACTURACIONMESPORCOLABORADORYAPP_7_FACTURADO_EN_APP).getName(), fraccionImputadas_Euros);
				
				//actualizamos todo el sistema de imputaciones
				List<FieldViewSet> fset = new ArrayList<FieldViewSet>();
				fset.add(facturaMesColaboradoryApp);
				IStrategy strat = new StrategyRecalculateFacturacionMes();				
				strat.doBussinessStrategy(data, this.dataAccess, fset);
				
				int ok = this.dataAccess.modifyEntity(facturaMesColaboradoryApp);
				if (ok != 1){//
					throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
				}
				this.dataAccess.commit();
			}//for each aplication
			
			numImputadas++;
			
		} catch (Throwable excc2) {
			excc2.printStackTrace(System.err);
			throw new Throwable(excc2.getMessage());
		}
						
		return numImputadas;
	}
	
	private XSSFSheet obtenerFichaResumen(final XSSFWorkbook wb) throws IOException{
		//buscamos la ficha de este mes
		for (int i=0;i<100;i++){
			final XSSFSheet sheet = wb.getSheetAt(i);
			if (sheet.getSheetName().startsWith("Resu")){						
				return sheet;
			}			
		}
		return null;		
	}
	
	private TuplaMesEjercicioFicha obtenerFichaMes(final XSSFWorkbook wb, Long idMes_, final Integer ejercicio_) throws IOException, DatabaseException{
		
		FieldViewSet objetoMes = new FieldViewSet(mesEntidad);
		objetoMes.setValue(mesEntidad.searchField(ConstantesModelo.MES_1_ID).getName(), idMes_);
		objetoMes = dataAccess.searchEntityByPk(objetoMes);
		Integer mesPosicionEnAnyo = (Integer) objetoMes.getValue(mesEntidad.searchField(ConstantesModelo.MES_3_NUMERO).getName());
			
		//buscamos la ficha de este mes
		for (int i=0;i<100;i++){
			final XSSFSheet sheet = wb.getSheetAt(i);
			String[] splitNameOfSheet = sheet.getSheetName().split("-");
			if (sheet.getSheetName().startsWith("Ficha") && splitNameOfSheet.length == 3 &&
				splitNameOfSheet[1].equals(String.valueOf(ejercicio_).substring(2,4))
					&& splitNameOfSheet[2].equals(String.valueOf(mesPosicionEnAnyo))){
				
				TuplaMesEjercicioFicha tupla = new TuplaMesEjercicioFicha();
				tupla.setEjercicio(ejercicio_);
				tupla.setFichaMes(sheet);
				tupla.setMes(objetoMes);
				
				return tupla;				
			}		
		}
		return null;
	}
	
	private List<TuplaMesEjercicioFicha> obtenerFichaMeses(final XSSFWorkbook wb) throws IOException, DatabaseException{
		List<TuplaMesEjercicioFicha> arr = new ArrayList<TuplaMesEjercicioFicha>();
		//buscamos fichas de meses
		for (int i=0;i<100;i++){
			final XSSFSheet sheet = wb.getSheetAt(i);
			String[] splitNameOfSheet = sheet.getSheetName().split("-");
			if (sheet.getSheetName().startsWith("Ficha") && splitNameOfSheet.length == 3){
				Integer mes = Integer.valueOf(splitNameOfSheet[2]).intValue();
				Integer ejercicio= Integer.valueOf(2000 + Integer.valueOf(splitNameOfSheet[1]).intValue());
								
				FieldViewSet objetoMes = new FieldViewSet(mesEntidad);
				objetoMes.setValue(mesEntidad.searchField(ConstantesModelo.MES_3_NUMERO).getName(), mes);
				objetoMes = dataAccess.searchByCriteria(objetoMes).get(0);
				
				TuplaMesEjercicioFicha tupla = new TuplaMesEjercicioFicha();
				tupla.setEjercicio(ejercicio);
				tupla.setFichaMes(sheet);
				tupla.setMes(objetoMes);
				
				arr.add(tupla);
			}else if (sheet.getSheetName().startsWith("Resu")){
				break;
			}
		}
		return arr;
	}
	
	public TuplaMesEjercicioEntradas importar(final Data data, final String path, final FieldViewSet importacionFSet, final Long idContrato, final Long idMes_, final Integer anyo) throws Exception {
		
		long timeStart = Calendar.getInstance().getTimeInMillis();
		int numImportadas = 0;
		Long idMes = idMes_;
		Integer ejercicio = anyo;
		try {			
			InputStream in = null;
			File ficheroRecordsImport = null;
			try {
				ficheroRecordsImport = new File(path);
				if (!ficheroRecordsImport.exists()) {
					throw new Exception(ERR_FICHERO_EXCEL_NO_LOCALIZADO);
				}
				in = new FileInputStream(ficheroRecordsImport);
			} catch (Throwable excc) {
				throw new Exception(ERR_FICHERO_EXCEL_NO_LOCALIZADO);
			}
						
			List<TuplaMesEjercicioFicha> fichasMeses = new ArrayList<TuplaMesEjercicioFicha>();
			XSSFSheet sheetResumen = null;
			try {
				XSSFWorkbook wb = new XSSFWorkbook(in);
				sheetResumen = obtenerFichaResumen(wb);
				if (idMes_ != null && anyo != null){
					TuplaMesEjercicioFicha tuplaMesBuscado = obtenerFichaMes(wb, idMes_, anyo);
					if (tuplaMesBuscado == null){
						throw new Exception("Error previo a la importacion: el mes-aoo consignados en pantalla no corresponden con ninguna ficha de este fichero Excel");
					}
					fichasMeses.add(tuplaMesBuscado);
				}else{
					fichasMeses = obtenerFichaMeses(wb);
				}
			}catch (Throwable exc1) {
				throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);				
			}
			
			//recorremos la lista de fichas mensuales...
			for (int f=0;f<fichasMeses.size();f++){
				TuplaMesEjercicioFicha tupla = fichasMeses.get(f);
				numImportadas = procesarFilas(tupla.getFichaMes(), sheetResumen, data, tupla.getMes(), tupla.getEjercicio(), idContrato);
				if ((anyo==null || idMes==null || fichasMeses.size() > 1) && f==(fichasMeses.size()-1)){//si es una carga moltiple y es el oltimo inicializo ejercicio y mes
					ejercicio = tupla.getEjercicio();
					idMes = (Long) tupla.getMes().getValue(tupla.getMes().getEntityDef().searchField(ConstantesModelo.MES_1_ID).getName());
				}else if ((anyo==null || idMes==null || fichasMeses.size() > 1) && f < (fichasMeses.size()-1)){//si es una carga moltiple y NO es el oltimo, inserto el fset de importacion en la BBDD, si ya tiene ID, lo actualizo, sino, lo inserto
					FieldViewSet importacionClon = importacionFSet.copyOf();
					idMes = (Long) tupla.getMes().getValue(tupla.getMes().getEntityDef().searchField(ConstantesModelo.MES_1_ID).getName());
					ejercicio = tupla.getEjercicio();
					importacionClon.setValue(importacionClon.getEntityDef().searchField(ConstantesModelo.IMPORT_FACT_3_MES).getName(), idMes);
					importacionClon.setValue(importacionClon.getEntityDef().searchField(ConstantesModelo.IMPORT_FACT_4_ANYO).getName(), tupla.getEjercicio());
					importacionClon.setValue(importacionClon.getEntityDef().searchField(ConstantesModelo.IMPORT_FACT_6_NUM_ENTRADAS).getName(), numImportadas);
					
					this.dataAccess.setAutocommit(false);
					if (importacionFSet.getFieldView(importacionFSet.getEntityDef().searchField(ConstantesModelo.IMPORT_FACT_1_ID).getName()) ==  null){
						//insertar
						this.dataAccess.insertEntity(importacionClon);
					}else{
						//update
						this.dataAccess.modifyEntity(importacionClon);
					}
					this.dataAccess.commit();
				}
			}
			
		} catch (Throwable ex2) {
			throw new Exception(ex2.getMessage());
		}
		
		TuplaMesEjercicioEntradas tupla = new TuplaMesEjercicioEntradas();
		tupla.setEjercicio(ejercicio);
		tupla.setIdMes(idMes);
		tupla.setEntradas(numImportadas);
		
		long timeEnded = Calendar.getInstance().getTimeInMillis();
		System.err.println("TIempo consumido: " + (timeEnded - timeStart)/1000 + "segundos.");
		return tupla;
	}

}

class TuplaMesEjercicioFicha{
	
	private Integer ejercicio;
	private FieldViewSet objetoMes;
	private XSSFSheet fichaMes;
	
	public void setEjercicio(Integer ejercicio_) {
		this.ejercicio = ejercicio_;
	}
	public void setMes(FieldViewSet mes) {
		this.objetoMes = mes;
	}
	public void setFichaMes(XSSFSheet fichaMes_) {
		this.fichaMes = fichaMes_;
	}
	public Integer getEjercicio() {
		return ejercicio;
	}
	public FieldViewSet getMes() {
		return this.objetoMes;
	}
	public XSSFSheet getFichaMes() {
		return this.fichaMes;
	}
	
}

