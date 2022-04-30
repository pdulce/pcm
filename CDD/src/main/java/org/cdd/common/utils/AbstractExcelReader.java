package org.cdd.common.utils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.definitions.IFieldLogic;

public abstract class AbstractExcelReader {
	
	protected static Map<String, Integer> COLUMNSET2ENTITYFIELDSET_MAP = new HashMap<String, Integer>();
	protected static int UNIQUE_COLUMN = -1;
	
	protected static Logger log = Logger.getLogger(AbstractExcelReader.class.getName());
	
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
	/**
	 * @param sheetNewVersion
	 * @param sheetOldVersion
	 * @param entidad
	 * @return
	 * @throws Throwable
	 * This method read an Excel and maps each column in Excel with the equivalent field in the entityLogic instance (fieldviewset)
	 */
	public final List<FieldViewSet> leerFilas(final Sheet sheet, final IEntityLogic entidad) throws Throwable{
		
		List<FieldViewSet> filas = new ArrayList<FieldViewSet>();
		int nrow = 0;
		Cell columnTitle = null;

		Row rowTitle = sheet.getRow(nrow++);
		while (rowTitle != null) {// while
			final Row rowIEsima = sheet.getRow(nrow++);
			if (rowIEsima == null) {
				break;
			}
			FieldViewSet fila = new FieldViewSet(entidad);
			for (int nColum = 0; nColum < 60; nColum++) {
				String columnName = null;
				try {
					columnTitle = rowTitle.getCell(nColum);
					if (columnTitle == null) {
						continue;
					}
					columnName = columnTitle.getStringCellValue();
					Integer positionOfEntityField = null;
					Iterator<String> iteMapeos = COLUMNSET2ENTITYFIELDSET_MAP.keySet().iterator();
					while (iteMapeos.hasNext()) {
						String clave = iteMapeos.next();
						if (clave.contains(columnName)) {
							positionOfEntityField = COLUMNSET2ENTITYFIELDSET_MAP.get(clave);
							break;
						}
					}
					if (positionOfEntityField == null) {
						continue;
					}

					final Cell cell = rowIEsima.getCell(nColum);
					Serializable valueCell = null;
					if (cell == null) {
						continue;
					}

					if (cell.getCellType() == CellType.NUMERIC) {
						valueCell = cell.getNumericCellValue();
					} else {
						valueCell = cell.getStringCellValue();
					}

					valueCell = getFieldOfColumnValue(entidad, positionOfEntityField, cell, valueCell);
					Collection<String> valuesPrevios = fila.getValues(positionOfEntityField.intValue());
					if (valuesPrevios == null || valuesPrevios.isEmpty() || valuesPrevios.iterator().next().toString().contentEquals("")){					
						fila.setValue(positionOfEntityField.intValue(), valueCell);
					}else {
						//acumulo: ver si solo para algunos campos...
						if (!valueCell.toString().contentEquals(valuesPrevios.iterator().next().toString())) {
							valuesPrevios.add(valueCell.toString());
							fila.setValues(positionOfEntityField.intValue(), valuesPrevios);
						}
					}
					
				}
				catch (Throwable excc) {
					excc.printStackTrace();
					AbstractExcelReader.log.log(Level.SEVERE, "Exception thrown in processing column " + columnName + " while importing numbered row " + nrow);
				}
			}// for
			if (fila.getValue(UNIQUE_COLUMN) == null ) {
				break;
			}
			filas.add(fila);
		}

		return filas;
	}
	
	protected Serializable getFieldOfColumnValue(final IEntityLogic entidad, 
			final Integer positionOfEntityField, 			
			final Cell cell, Serializable valueCell) throws DatabaseException, ParseException{
		
		IFieldLogic fLogic = entidad.searchField(positionOfEntityField.intValue());
		if (fLogic.getAbstractField().isDate()) {
			if (valueCell.equals("")){
				valueCell = null;
			}else{
				try {
					Date valueCel_Date =  CommonUtils.myDateFormatter.parse(valueCell);							
					if (fLogic.getAbstractField().isTimestamp()) {
						valueCell = new Timestamp(valueCel_Date.getTime());
					}else{
						valueCell = valueCel_Date;
					}
				}catch (ParseException parseExc) {
					valueCell = cell.getDateCellValue();													
				}catch (ClassCastException castExc) {
					castExc.printStackTrace();
					valueCell = cell.getDateCellValue();												
				}
			}
		} else if (fLogic.getAbstractField().isLong() || fLogic.getAbstractField().isInteger()) {						
			valueCell = valueCell.equals("") ? null : valueCell.toString();
		} else if (fLogic.getAbstractField().isDecimal()) {
			valueCell = valueCell.equals("") ? null : CommonUtils.numberFormatter.parse(valueCell);
		}
		return valueCell;
	}

	
}
