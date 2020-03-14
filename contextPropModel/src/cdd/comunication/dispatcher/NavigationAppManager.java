package cdd.comunication.dispatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cdd.common.InternalErrorsConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.services.ApplicationDomain;


public class NavigationAppManager {
	
public static Logger log = Logger.getLogger("cdd.comunication.dispatcher.NavigationAppManager");
	
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
	
	private final List<Document> appRoots = new ArrayList<Document>();
	private Document appNavigation;
	
	public NavigationAppManager(final String appNavigationXML, final ServletContext servletContext){		
		try {
			int indexOf = appNavigationXML.indexOf("WEB-INF");
			String uriWebAppFile = appNavigationXML.substring(indexOf);
			
			File fUriWebAppFile = new File(servletContext.getRealPath(uriWebAppFile));
			this.appNavigation = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fUriWebAppFile.getAbsolutePath());			
			if (this.appNavigation == null){
				throw new RuntimeException("navigationWebModel file not found, relative path: " + appNavigationXML);
			}			
			this.appRoots.addAll(readFiles(new File(new File(servletContext.getRealPath("WEB-INF/web.xml"))
			.getParentFile().getAbsolutePath().concat("/services")).listFiles(), ".xml"));							
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new RuntimeException("Error instantiating NavigationAppManager ", e);
		}
	}
	
	private List<Document> readFiles(final File[] pFiles, String suffix_) throws FileNotFoundException, SAXException, IOException,
		ParserConfigurationException {
		
		List<Document> filesFound = new ArrayList<Document>();
		if (pFiles == null) {
			return filesFound;
		}
		for (File pFile : pFiles) {
			if (pFile.isFile() && pFile.getName().endsWith(suffix_)) {
				filesFound.add(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(pFile)));
			}
		}
		return filesFound;
	}
	
	public Document getAppNavigation(){
		return this.appNavigation;
	}
	
	public final Map<String, String> extractAuditFieldSet() throws PCMConfigurationException {
		Map<String, String> auditFieldSet = null;		
		final NodeList listaNodes = this.getAppNavigation().getDocumentElement()
				.getElementsByTagName(ApplicationDomain.AUDITFIELDSET_ELEMENT);
		if (listaNodes.getLength() > 0) {
			auditFieldSet = new HashMap<String, String>();
			final Element auditFieldSetElem = (Element) listaNodes.item(0);
			final NodeList listaChildren = auditFieldSetElem.getElementsByTagName(ApplicationDomain.AUDITFIELD_ELEMENT);
			if (listaChildren.getLength() < 6) {
				final StringBuilder excep = new StringBuilder(InternalErrorsConstants.AUDIT_FIELDS_NOT_FOUND);
				throw new PCMConfigurationException(excep.toString());
			}
			auditFieldSet.put(ApplicationDomain.USU_ALTA, listaChildren.item(0).getFirstChild().getNodeValue());
			auditFieldSet.put(ApplicationDomain.USU_MOD, listaChildren.item(1).getFirstChild().getNodeValue());
			auditFieldSet.put(ApplicationDomain.USU_BAJA, listaChildren.item(2).getFirstChild().getNodeValue());
			auditFieldSet.put(ApplicationDomain.FEC_ALTA, listaChildren.item(3).getFirstChild().getNodeValue());
			auditFieldSet.put(ApplicationDomain.FEC_MOD, listaChildren.item(4).getFirstChild().getNodeValue());
			auditFieldSet.put(ApplicationDomain.FEC_BAJA, listaChildren.item(5).getFirstChild().getNodeValue());
			return auditFieldSet;
		}
		final StringBuilder excep = new StringBuilder(InternalErrorsConstants.APP_NOT_FOUND.replaceFirst(InternalErrorsConstants.ARG_1, "application"));
		throw new PCMConfigurationException(excep.toString());
	}
	
	
	public String getTitleOfAction(final ApplicationDomain applicationDomain, final String service, final String event){		
		String serviceSceneTitle = "";
		try {
			
			Element actionElementNode = applicationDomain.getDomainService(service).extractActionElementByService(event);
			NodeList nodes = actionElementNode.getElementsByTagName("form");
			int n = nodes.getLength();
			for (int nn=0;nn<n;nn++){
				Node elem = nodes.item(nn);
				if (elem.getNodeName().equals("form")){
					serviceSceneTitle = ((Element)elem).getAttribute("title");
				}
			}
		} catch (PCMConfigurationException e) {
			NavigationAppManager.log.log(Level.INFO, "Error getting title of " + service + " event: " + event, e);
		}
		return serviceSceneTitle;
	}
	
	
}
