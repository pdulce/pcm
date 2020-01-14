/**
 * 
 */
package facturacionUte.servlets;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;

import pcm.common.PCMConstants;
import pcm.common.exceptions.MessageException;
import pcm.comunication.actions.IAction;
import pcm.comunication.actions.SceneResult;
import pcm.comunication.dispatcher.BasePCMServlet;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.logicmodel.persistence.DAOConnection;
import pcm.context.viewmodel.factory.IBodyContainer;
import facturacionUte.utils.bolsa.ValoresActuales;

/**
 * @author 99GU3997
 */
public class LauncherServlet extends BasePCMServlet {
	
	public static final String EVENTO_ALL_INFO_BOLSA_VALORES = "allInfoBolsa";
	
	public static final String EVENTO_MY_INFO_BOLSA_VALORES = "myInfoBolsa";
	
	public static final String EVENTO_GEN_PPT = "generarPPT_SGASM";

	public static final String EVENTO_CERRAR_MES = "cerrarMes_";

	public static final String EVENTO_ABRIR_MES = "abrirMes_";

	public static final String EVENTO_BORRAR_PREFACTURA = "BorrarPrefactura_";

	public static final String EVENTO_ACTUALIZAR_EXCEL = "GenPreFactura_";

	public static final String EVENTO_INTRODUCIR_HORAS = "IntroducirHoras_";

	public static final String EVENTO_CARGAR_HORAS_FICHAJES = "CargarHorasFichajes_";

	public static final String EVENTO_GRABAR_HORAS = "GrabarHoras_";

	public static final String EVENTO_AUTOAJUSTE = "AjustarHoras_";

	public static final String EVENTO_SINCRONIZAR_INCIDENCIAS = "FileSincronize";

	public static final String EVENTO_RASTREO_INCIDENCIAS = "FileRastreator";

	public static final String EVENTO_REGISTRO_INCID = "ConsultaRegistroIncidencias.query";

	public static final String UTIL_URI_SERVLET = "LauncherUtility";

	public static final String[] ficheros_Excel_Seg_SSGG = new String[]{"SG ASM_SANI-FAMA-FAM2.xlsx", "SG ASM_FORMAR.xlsx"};
	
	public static final String BASE_PATH_PPT = "O:\\externos\\__Hojas SegDireccion";
	
	public static final String PLANTILLAS_PPT_PATH = "O:\\externos\\jdk1.7.0\\external\\plantillas";
	
	public static final String[] LOCAL_SERVER_DIARIO_PATHS = { "O:\\UTE SAG-INDRA\\Incidencias\\", "O:\\SAG-ATRIUM\\Incidencias\\"
			/*"G:\\UTE-SAG-BABEL-ALFA-IRISS\\Incidencias\\"*/ };

	public static final String[] REMOTE_SERVER_DIARIO_PATHS = { "W:\\Incidencias\\"/*las de SAG*/, "Y:\\Incidencias\\"/*las de Atrium*/ 
			/*"G:\\UTE-SAG-BABEL-ALFA-IRISS\\Incidencias\\"*/ };

	public static final String[] LOCAL_ROOT_SERVER_FIRMADAS_PATH = { "O:\\SOFTWARE_AG\\GESTION\\Incidencias-firmadas\\",
			"O:\\SOFTWARE_AG\\GESTION\\Incidencias-firmadas\\Obsoletos-2015",
			"O:\\SOFTWARE_AG\\GESTION\\Incidencias-firmadas\\Obsoletos-2014" };

	public static final String[] REMOTE_ROOT_SERVER_FIRMADAS_PATH = { "Z:\\", "Z:\\Obsoletos-2015", "Z:\\Obsoletos-2014" };

	private static final long serialVersionUID = 777777781L;
	
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		
		super.doPost(request, response);
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);
	}

	@Override
	protected SceneResult renderRequestFromNode(final DAOConnection conn, final IDataAccess dataAccess, final String serviceName,
			final Element actionNode, final String event, final RequestWrapper request_, final boolean eventSubmitted, IAction action,
			IBodyContainer containerView_, Collection<MessageException> messageExceptions, final String lang) {
		
		StringBuilder htmlOutput = new StringBuilder();
		htmlOutput.append("<form class=\"pcmForm\" enctype=\"multipart/form-data\" method=\"POST\" name=\"enviarDatos\" action=\""
				+ UTIL_URI_SERVLET + "\">");
		htmlOutput.append("<input type=\"hidden\" id=\"exec\" name=\"exec\" value=\"" + request_.getParameter(EXEC_PARAM) + "\" />");
		htmlOutput.append("<input type=\"hidden\" id=\"event\" name=\""+PCMConstants.EVENT+"\" value=\"" + event + "\" />");

		if (request_.getParameter(EXEC_PARAM) == null){
			return new SceneResult();
		}else if (request_.getParameter(EXEC_PARAM).startsWith(EVENTO_ALL_INFO_BOLSA_VALORES)) {
			//actualizar todos los valores desde invertia.com/históricos
			htmlOutput.append(new ValoresActuales().refrescarIndicesBursatiles(request_, dataAccess));						
		}else if (request_.getParameter(EXEC_PARAM).startsWith(EVENTO_MY_INFO_BOLSA_VALORES)) {
			//pintar el valor último de las empresas/sectores/índices bursátiles de la lista
			htmlOutput.append(new ValoresActuales().refreshMiCartera(request_, dataAccess));
		}
		
		htmlOutput.append("</form>");
		SceneResult scene = new SceneResult();
		scene.appendXhtml(htmlOutput.toString());
		return scene;
	}

	

}
