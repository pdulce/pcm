/**
 * 
 */
package facturacionUte.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import pcm.common.utils.CommonUtils;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.logicmodel.definitions.IEntityLogic;
import pcm.context.logicmodel.definitions.IFieldLogic;
import pcm.context.logicmodel.factory.EntityLogicFactory;
import pcm.context.viewmodel.definitions.FieldViewSet;
import facturacionUte.common.ConstantesModelo;


/**
 * @author 99GU3997
 *         Esta clase, sea el nomero de columnas que sea, leero una Excel y cargaro al menos los
 *         siguientes campos en una tabla SQLite:
 *         *********************
 *         Fecha	oltimo	Apert.	%Dif.	Mox.	Mon.	Volumen

 *         *********************
 *         Excel resource file: C:\jboss-4.0.2\server\default\data\excel
 *         Usaremos la extension .xls, org.apache.poi.hssf.usermodel.*.
 *         Paara extensiones .xlsx se usa la libreroa org.apache.poi.xssf.usermodel.*;
 */
public class ImportarCotizacionesBolsa {

	protected static IEntityLogic cotizacionesEntidad;

	private static final String ERR_FICHERO_EXCEL_FORMATO_XLS = "ERR_FICHERO_EXCEL_FORMATO_XLS";

	private static final String ERR_FICHERO_EXCEL_NO_LOCALIZADO = "ERR_FICHERO_EXCEL_NO_LOCALIZADO";

	private static final String ERR_IMPORTANDO_FICHERO_EXCEL = "ERR_IMPORTANDO_FICHERO_EXCEL";

	private static Map<String, Integer> MAPEOSCOLUMNASEXCEL2BBDDTABLE = new HashMap<String, Integer>();
	static {
		MAPEOSCOLUMNASEXCEL2BBDDTABLE.put("Fecha", Integer.valueOf(ConstantesModelo.INVERTIA_DATA_3_FECHA));
		MAPEOSCOLUMNASEXCEL2BBDDTABLE.put("oltimo", Integer.valueOf(ConstantesModelo.INVERTIA_DATA_4_LAST_PUNTAJE));
		MAPEOSCOLUMNASEXCEL2BBDDTABLE.put("Apert.", Integer.valueOf(ConstantesModelo.INVERTIA_DATA_5_INICIAL_PUNTAJE));
		MAPEOSCOLUMNASEXCEL2BBDDTABLE.put("%Dif.", Integer.valueOf(ConstantesModelo.INVERTIA_DATA_6_PORCENTAJE_DIF));
		MAPEOSCOLUMNASEXCEL2BBDDTABLE.put("Mox.", Integer.valueOf(ConstantesModelo.INVERTIA_DATA_7_MAX_PUNTAJE));
		MAPEOSCOLUMNASEXCEL2BBDDTABLE.put("Mon.", Integer.valueOf(ConstantesModelo.INVERTIA_DATA_8_MIN_PUNTAJE));
		MAPEOSCOLUMNASEXCEL2BBDDTABLE.put("Volumen", Integer.valueOf(ConstantesModelo.INVERTIA_DATA_9_VOLUMEN));
	}

	private IDataAccess dataAccess;

	protected void initEntities(final RequestWrapper request_) {
		if (cotizacionesEntidad == null) {
			try {
				cotizacionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(CommonUtils.getEntitiesDictionary(request_),
						ConstantesModelo.INVERTIA_DATA_ENTIDAD);
			}
			catch (Throwable exc) {
				throw new RuntimeException("Error in initEntities method: ", exc);
			}
		}
	}

	public ImportarCotizacionesBolsa(IDataAccess dataAccess_, RequestWrapper req) {
		this.dataAccess = dataAccess_;
		initEntities(req);
	}

	public int importar(final String path, final FieldViewSet importacionFSet4Insert) throws Exception {

		InputStream in = null;
		File ficheroCotizacionesImport = null;
		Calendar dateOfExport = Calendar.getInstance();
		try {
			// nombre esperado LEXs_2015_01_09DPs
			ficheroCotizacionesImport = new File(path);
			if (!ficheroCotizacionesImport.exists()) {
				throw new Exception(ERR_FICHERO_EXCEL_NO_LOCALIZADO);
			}

			dateOfExport.setTimeInMillis(ficheroCotizacionesImport.lastModified());
			dateOfExport.set(Calendar.HOUR, 0);
			dateOfExport.set(Calendar.MINUTE, 0);
			dateOfExport.set(Calendar.SECOND, 0);
			dateOfExport.set(Calendar.MILLISECOND, 0);
			in = new FileInputStream(ficheroCotizacionesImport);
		}
		catch (Throwable excc) {
			throw new Exception(ERR_FICHERO_EXCEL_NO_LOCALIZADO);
		}

		int numImportadas = 0;
		/** intentamos con el formato .xls y con el .xlsx **/
		try {
			XSSFWorkbook wb = new XSSFWorkbook(in);
			final XSSFSheet xss_Sheet = wb.getSheetAt(0);
			if (xss_Sheet == null) {
				throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
			}
			numImportadas = leerYGrabarFilas(xss_Sheet, null /*hssShet*/, dateOfExport.getTime(), importacionFSet4Insert);
		}
		catch (Throwable exc) {
			try {
				in = new FileInputStream(ficheroCotizacionesImport);
				HSSFWorkbook wb2 = new HSSFWorkbook(in);
				final HSSFSheet hss_sheet = wb2.getSheetAt(0);
				if (hss_sheet == null) {
					throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
				}
				numImportadas = leerYGrabarFilas(null /*xss_sheet*/, hss_sheet, dateOfExport.getTime(), importacionFSet4Insert);
			}
			catch (Throwable exc2) {
				throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
			}
		}
		
		
		return numImportadas;

	}

	private int leerYGrabarFilas(final XSSFSheet xss_sheet, final HSSFSheet hss_sheet, final Date timeOfSheet, final FieldViewSet registroImportacion) throws Throwable {

		int numImportadas = 0, yaImportadas=0, nrow = 0;
		this.dataAccess.setAutocommit(false);
		Row rowTitle = xss_sheet!=null?xss_sheet.getRow(nrow++): hss_sheet.getRow(nrow++);
		while (rowTitle != null) {
			try {
				final Row rowIEsima = xss_sheet!=null?xss_sheet.getRow(nrow++): hss_sheet.getRow(nrow++);
				if (rowIEsima == null) {
					break;// si localiza una fila completamente sin informacion, salimos
				}
				try {
					Date d = rowIEsima.getCell(0).getDateCellValue();
					if (d == null) {
						continue;
					}
				}
				catch (Throwable exc) {
					continue;// pasar a la siguiente fila
				}
				
				// si llega a aqui, que lea cada columna de esta fila
				final FieldViewSet registro = new FieldViewSet(cotizacionesEntidad);
				for (int nColum = 0; nColum < 9; nColum++) {// hay 8 columnas siempre

					final Cell columnTitle = rowTitle.getCell(nColum);
					if (columnTitle == null) {
						continue;
					}
					String columnName = columnTitle.getStringCellValue();
					if (columnName == null || columnName.equals("")) {
						continue;
					}

					final Cell cell = rowIEsima.getCell(nColum);
					Serializable valueCell = null;

					Integer positionOfEntityField = null;
					Iterator<String> iteMapeos = MAPEOSCOLUMNASEXCEL2BBDDTABLE.keySet().iterator();
					while (iteMapeos.hasNext()) {
						String clave = iteMapeos.next();
						if (clave.contains(columnName)) {
							positionOfEntityField = MAPEOSCOLUMNASEXCEL2BBDDTABLE.get(clave);
							break;
						}
					}
					if (positionOfEntityField == null) {
						continue;
					}

					IFieldLogic fLogic = cotizacionesEntidad.searchField(positionOfEntityField.intValue());
					if (fLogic.getAbstractField().isDate()) {
						valueCell = cell.getDateCellValue();						
					}else if (fLogic.getAbstractField().isLong()) {
						valueCell = cell.getNumericCellValue();						
					} else if (fLogic.getAbstractField().isDecimal()) {
						try{
							valueCell = cell.getNumericCellValue();
						}catch (Throwable excFormato){
							if (cell.getStringCellValue().equals("n.d.")){
								valueCell = Double.valueOf (0);
							}
						}
					} else {
						valueCell = cell.getStringCellValue();
					}
					registro.setValue(cotizacionesEntidad.searchField(positionOfEntityField.intValue()).getName(), valueCell);
				}// for columnas
				
				
				registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_2_GRUPO).getName(), registroImportacion.getValue(registroImportacion.getEntityDef().searchField(ConstantesModelo.INVERTIA_IMPORT_2_GRUPO).getName()));
				registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_10_FEC_IMPORTACION).getName(), registroImportacion.getValue(registroImportacion.getEntityDef().searchField(ConstantesModelo.INVERTIA_IMPORT_4_FEC_IMPORTACION).getName()));
				
				FieldViewSet duplicado = new FieldViewSet(cotizacionesEntidad);
				duplicado.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_3_FECHA).getName(),
						registro.getValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_3_FECHA).getName()));
				duplicado.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_2_GRUPO).getName(),
						registro.getValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_2_GRUPO).getName()));
				if (this.dataAccess.searchByCriteria(duplicado).isEmpty()) {
					
					// el mes y aoo para poder explotarlo en Histogramas con selectGroupBy
					Date fecAlta = (Date) registro.getValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_3_FECHA).getName());
					Calendar dateFec = Calendar.getInstance();
					dateFec.setTime(fecAlta);
					String year = String.valueOf(dateFec.get(Calendar.YEAR));
					String month = String.valueOf(dateFec.get(Calendar.MONTH) + 1);
					if (month.length() == 1) {
						month = "0".concat(month);
					}
					registro.setValue(cotizacionesEntidad.searchField(ConstantesModelo.INVERTIA_DATA_11_ANYO_MES).getName(), year + "-" + month);

					int ok = this.dataAccess.insertEntity(registro);
					if (ok != 1) {
						throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
					}
					numImportadas++;
					if (numImportadas%50 == 0){
						this.dataAccess.commit();
						this.dataAccess.setAutocommit(false);
					}
					
				}else{
					yaImportadas++;
				}
			}
			catch (Throwable exc31) {
				throw new Throwable(ERR_IMPORTANDO_FICHERO_EXCEL);
			}			
		}// while
		if (numImportadas%50 != 0){
			this.dataAccess.commit();
		}

		return numImportadas+yaImportadas;
	}

}
