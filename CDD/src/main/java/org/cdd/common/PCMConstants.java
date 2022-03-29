package org.cdd.common;

/**
 * <h1>PCMConstants</h1> The InternalErrorsConstants interface
 * maintains the constants of all configuration terms widely used in this
 * framework.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public interface PCMConstants {

	public static final int MAX_SIZE_BLOBS = 5 * 1024 * 1024;// 5 megas
	
	public static final char CHAR_SLASH = '\\', CHAR_POINT = '.', CHAR_DOBLE_POINT = ':', CHAR_POINT_COMMA = ';', CHAR_BEGIN_PARENT = '(',
			CHAR_END_PARENT = ')', CHAR_SPACE = ' ', CHAR_COMMA = ',', CHAR_VERTICAL_SEP = '|', CHAR_BEGIN_CORCH = '[',
			CHAR_END_CORCH = ']', CHAR_SIMPLE_SEPARATOR = '-', CHAR_APOSTROF = '\'', CHAR_ANTISLASH = '/', CHAR_BACK_SLASH = '\\',
			CHAR_9 = '9', CHAR_ASTERISC = '*', CHAR_PERCENT = '%';
	
	public static final String ID_PAGINACION = "paginacion", LANGUAGE = "language", DEFAULT_LANG = "es_",
			SERVICE_DIRECTORY = "serviceDirectory", ENTITIES_MODEL = "entitiesDictionary",
			DOWNLOAD_DIR = "downloadDir", LOG_APPFILE = "logAppFile", UPLOAD_DIR = "uploadDir", APPURI_ = "uri", PAGESIZE = "pageSize",
			APP_PROFILE = "profile", STYLE_MODE_SITE = "style", PALETA_COLORES = "paletaColores", 
					PALETA_ID = "paleta_id", 
			SERVLET_MAPPING = "servletMapping", AUDITON_APP = "auditActivated",	DATA_SOURCE_TYPE = "datasourceAccess", 
			DATASOURCE_FACTORY_IMPL = "datasourceFactoryImpl",	DSOURCE_IMPLEMENTATION = "datasourceImpl", SOURCE_DRIVER = "driver",
			DB_PREFFIX = "dbPreffix", DB_RESOURCE_NAME = "dbResourceName", JNDI_PREFFIX = "jndiPreffix", JNDI_RESOURCE_NAME = "jndiName",
			DATASOURCE_JNDI_TYPE = "JNDI", DATASOURCE_JDBC_TYPE = "JDBC", DB_USER = "dbUserName", DB_PASSWD = "dbPassword",
			URL_DDBB_CONNECTION = "url-connection", URL_DDBB_DRIVERCLASS = "driver-class", SCHEMADDBB = "esquemaDDBB",
			PASSWORD_MARK = "********", TEMPLATE_PATH = "template", STYLESHEET_RESOURCE_PATH = "cssStyleSheet",
			APPLICATION_TITLE = "appTitle", APP_DICTIONARY = "appDictionary", ENTITY_NODENAME = "entity",
			ROW_LABEL = "row", JPG_FORMAT = "jpg",
			XML_EXTENSION = ".xml", CSS = "#CSS#", LOGO = "#LOGO#", FOOT = "#FOOT#", MENU_ITEMS = "#MENU_ITEMS#",
			TREE = "#TREE#", FILE_INTERNAL_URI_PARAM = "filePath", FILE_UPLOADED_PARAM = "fileUploaded",
			END_COMILLAS = "\"", EMPTY_ = "", ASTERISC_SCAPED = "\\*", NULL_LITERAL = "null",
			LABEL_PREFFIX = "'label_'", ALIGN_PREFFIX = "'align'", EVENT_PREFFIX = "'event_'", RESET_EVENT = "reset",
			CLEAN_EVENT = "clean", SEL_PREFFIX_WITHOUT_POINT = "Sel", SEL_PREFFIX = "Sel.", URI_SUBMIT_PREFFIX = "'uriSubmit'",
			TITLE_PREFFIX = "'title'", HTTP_PREFIX = "http", HTTP_FIRST_URI_SEPARATOR = "?", FOLDER_LINKED_PARAM = "folderLinked",
			REGEXP_BEGIN_CORCH = "\\[", REGEXP_POINT = "\\.", REGEXP_PARENT = "\\(", CIF_REGEXP = "CIF", CLASSIC_SEPARATOR = "|",
			COMMA = ",", POINT = ".", AMPERSAND = "#", SEPARATOR_SPECIAL = "o", POINT_COMMA = ";", EQUALS = "=", END_FUNC = "')",
			BEGIN_PARENTHESIS = "(", END_PARENTHESIS = ")", NEXT_STRING_ARG = "','", NEXT_NO_STRING_ARG = "', ", SIMPLE_COMILLA = "'",
			UNDERSCORE = "_", COMMA_ARGS = "\",\"", SECOND_COMMA_ARGS = ",\"", STRING_SPACE = " ", END_BLOCK = "}", EVENT = "event", 								
			BEGIN_BLOCK = "{", SIMPLE_SEPARATOR = "-",
			HTTP_PARAM_SEPARATOR = "&", ENTIYY_PARAM = "entity", HTTP_FIRST_PARAM_SEPARATOR = "?", PERCENTAGE_SCAPED = "\\%",
			PERCENTAGE = "%", INVERSE_SLASH = "/", CHAR_DOBLE_POINT_S = ":", ESTILO_PAR = "trPaired", ESTILO_IMPAR = "trImpaired",
			NO_DATA = "No hay datos", ES_CHARCODE = "ES", BLOB_EXTENSION = "Ext", AMP_ = "&", 
			AMP_SCAPE = "&amp;", MAYOR_ = ">", MAYOR_SCAPE = "&gt;", MINOR_ = "<",
			MINOR_SCAPE = "&lt;", APOSTROFE_ = "'", APOSTROFE_SCAPE = "&apos;", COMILLA_SCAPE = "&quot;";

	public static final String[] MESES = { "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre",
		"octubre", "noviembre", "diciembre" };//

	public static final String[] MESES_ABBREVIATED = { "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep",
			"oct", "nov", "dic" };//

}
