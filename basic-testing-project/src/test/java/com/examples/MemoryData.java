package test.java.com.examples;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class MemoryData {
	
	private static final String ERR_FICHERO_EXCEL_FORMATO_XLS = "ERR_FICHERO_EXCEL_FORMATO_XLS",
			ERR_FICHERO_EXCEL_NO_LOCALIZADO = "ERR_FICHERO_EXCEL_NO_LOCALIZADO";
	
	/**
	 * Secci�n de cabecera: to object called datosConexion
	 * 
	 * Servidor HOSTNAME PORT CONTEXT
	 * 
	 * /*** Secci�n de detail con n� din�mico de columnas: to object called
	 * escenariosTest
	 * 
	 * Login1-Error input1 valor1 input2 valor2 submitName element2Check
	 */

	private static final String HOSTNAME = "HOSTNAME";
	private static final String CONTEXT = "CONTEXT";	
	public static final String SUBMIT_ELEMENT = "submitName";
	public static final String ELEMENT_2_EVALUATE = "element2Check";
	public static final String VALUE_2_EVALUATE = "value2Check";
	
	private Map<String, Map<String, String>> dataTests;
	private String URL = "";
	
	public MemoryData(final String file_) {		
		try {
			chargeInMemory(file_);
		} catch (Exception notFound) {
			notFound.printStackTrace();
			throw new RuntimeException(ERR_FICHERO_EXCEL_NO_LOCALIZADO);
		}
	}
	
	public String getURL(){
		return this.URL;
	}
	
	public Map<String,Map<String, String>> getDatosEscenariosTest(){
		return this.dataTests;
	}
	
	public Map<String, String> getDatosEscenarioTest(String methodTestName){
		return this.dataTests.get(methodTestName);
	}
	
	private void chargeInMemory(String file_) throws Exception {
		
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(file_);
		try {
			XSSFWorkbook wb = new XSSFWorkbook(in);
			final XSSFSheet sheet = wb.getSheetAt(0);
			if (sheet == null) {
				throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
			}
			leerDataTestFile(sheet, null);
		} catch (Throwable exc) {
			try {				
				HSSFWorkbook wb2 = new HSSFWorkbook(in);
				final HSSFSheet sheet = wb2.getSheetAt(0);
				if (sheet == null) {
					throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
				}
				leerDataTestFile(null, sheet);

			} catch (Throwable exc2) {
				throw new Exception(ERR_FICHERO_EXCEL_FORMATO_XLS);
			}
		}finally{
			try {
				in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * @param sheetNewVersion
	 * @param sheetOldVersion
	 * @param entidad
	 * @return
	 * @throws Throwable
	 *             This method read an Excel and maps each column in Excel with
	 *             the equivalent field in the entityLogic instance
	 *             (fieldviewset)
	 */
	private final void leerDataTestFile(final XSSFSheet sheetNewVersion, final HSSFSheet sheetOldVersion) throws Throwable {

		int nrow = 0;		
		Row rowEtiquetas = sheetNewVersion != null ? sheetNewVersion.getRow(nrow++) : sheetOldVersion.getRow(nrow++);
		if (rowEtiquetas == null) {
			throw new Exception("error leyendo fichero Excel: primera row de etiquetas de conexion");
		}
		Row rowDatos = sheetNewVersion != null ? sheetNewVersion.getRow(nrow++) : sheetOldVersion.getRow(nrow++);
		if (rowDatos == null) {
			throw new Exception("error leyendo fichero Excel: segunda row de valores de conexion");
		}
		
		Map<String, String> datosConexion = new HashMap<String, String>();
		// cargamos los datos de la conexion para obtener la url
		for (int nColum = 1; nColum < 3; nColum++) {			
			try {
				Cell columnCell = rowEtiquetas.getCell(nColum);
				if (columnCell == null) {
					throw new Exception("Error en etiqueta conexion");
				}
				datosConexion.put(columnCell.getStringCellValue(), rowDatos.getCell(nColum).getStringCellValue());
			} catch (Throwable excc) {
				excc.printStackTrace();
				throw new Exception("1.Error leyendo fichero Excel, fila: " + nrow + " columna " + nColum + ". Inner msg: " + excc.getMessage());
			}
		}// end of while: datos cabecera
		this.URL = datosConexion.get(HOSTNAME).concat(datosConexion.get(CONTEXT));

		/*** En este punto, tenemos la nRow apuntando a la fila tercera, de etiquetas del primer escenario de pruebas***/
		
		this.dataTests = new HashMap<String, Map<String, String>>();		
		
		while (rowEtiquetas != null) {// while
			rowEtiquetas = sheetNewVersion != null ? sheetNewVersion.getRow(nrow++) : sheetOldVersion.getRow(nrow++);
			if (rowEtiquetas == null) {//si hemos llegado al final del libro de datos excel
				break;
			}
			rowDatos = sheetNewVersion != null ? sheetNewVersion.getRow(nrow++) : sheetOldVersion.getRow(nrow++);
			if (rowDatos == null) {
				throw new Exception("error leyendo fichero Excel: segunda row de valores de escenario no consignada " + (nrow - 2));
			}
			/** inicializamos el mapa de datos; ojo, no todos son par�metros, est�n los tres 'submitName' 'element2Check'	'value2Check' ***/
			Map<String, String> datosTest = new HashMap<String, String>();
			String methodName = rowDatos.getCell(0).getStringCellValue();
			if (methodName==null || methodName.equals("")){
				break;//ya no hay nuevos methods definidos en este tesdatamap
			}
			// cargamos los datos de cada escenario de test
			int nColum = 1;
			while (nColum < 100) {
				try {
					Cell columnCellEtiqueta = rowEtiquetas.getCell(nColum);
					Cell columnCellValor = rowDatos.getCell(nColum);
					if (columnCellEtiqueta == null) {
						throw new Exception("Error en etiqueta escenario " + (nrow - 2));
					}
					String columnNameEtiqueta = columnCellEtiqueta.getStringCellValue(); 
					if (Character.isDigit(columnNameEtiqueta.charAt(columnNameEtiqueta.length()-1))){
						datosTest.put(columnCellValor.getStringCellValue(), rowDatos.getCell(nColum+1).getStringCellValue());//el adyacente es su valor
						nColum += 2; 
					}else{
						//hemos llegado al final de los params de entrada, estos tres son fijos: 'submitName' 'element2Check'	'value2Check'
						datosTest.put(columnCellEtiqueta.getStringCellValue(), columnCellValor.getStringCellValue());//submitName
						Cell columnCellEtiquetaEle2Check = rowEtiquetas.getCell(nColum+1);
						Cell columnCellValorEle2Check = rowDatos.getCell(nColum+1);
						datosTest.put(columnCellEtiquetaEle2Check.getStringCellValue(), columnCellValorEle2Check.getStringCellValue());//element2Check
						Cell columnCellEtiquetaVal2Check = rowEtiquetas.getCell(nColum+2);
						Cell columnCellValorVal2Check = rowDatos.getCell(nColum+2);
						datosTest.put(columnCellEtiquetaVal2Check.getStringCellValue(), columnCellValorVal2Check.getStringCellValue());//value2Check
						break;
					}
				} catch (Throwable excc) {
					excc.printStackTrace();
					throw new Exception("2.Error leyendo fichero Excel, fila: " + nrow + " columna " + nColum + ". Inner msg: " + excc.getMessage());
				}
			}//fin de la iteracion de columnas que representan cada par�metro de input, excepto los tres finales, 'submitName' 'element2Check'	'value2Check'
			this.dataTests.put(methodName, datosTest);
		}
	}
	
	public static void main(String[] args) {
		String excelFile = "resources/Data.xlsx";
		MemoryData chargerDataSet = new MemoryData(excelFile);
		
		Map<String,Map<String,String>> mapaAll = chargerDataSet.getDatosEscenariosTest();
		System.out.println("Datos de todo el mapa para pruebas: " + mapaAll);
		
		System.out.println("URL para pruebas: " + chargerDataSet.getURL());
	}

}





	
	
	

	