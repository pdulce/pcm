package cdd.domain.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.comunication.dispatcher.CDDWebController;

public class DomainUseCaseService {
	
	private String uuid, useCaseName;
	private Document docOfServiceFileDescr;
	private List<Element> subCases;
	
	private final Collection<Element> discoverAllActions(final String subcaseName) {
		final Collection<Element> events = new ArrayList<Element>();
		try {
			final NodeList actionNodeSet = getSubCaseOfServiceName(subcaseName).getElementsByTagName(DomainApplicationContext.ACTION_ELEMENT);
			for (int i = 0; i < actionNodeSet.getLength(); i++) {
				events.add((Element) actionNodeSet.item(i));
			}
		}
		catch (final Throwable exc) {
			CDDWebController.log.log(Level.SEVERE, "Error", exc);
		}
		return events;
	}
	
	private String searchNameOfUseCase(){
		int indexOf = this.uuid.lastIndexOf(".xml");
		String firstPart = this.uuid.substring(0, indexOf);
		StringBuilder name = new StringBuilder();
		int firstPartLength = firstPart.length();
		for (int i=1;i<=firstPartLength;i++){
			String partOfName = firstPart.substring(firstPartLength - i, (firstPartLength - i)+1);
			if (partOfName.equals(File.separator) || partOfName.equals("/")){
				break;
			}else {
				name = new StringBuilder(partOfName+name.toString());
			}
		}
		return name.toString();
	}
	
	public DomainUseCaseService(String path_){		
		this.uuid = path_;		
		this.useCaseName = searchNameOfUseCase();
		this.subCases = new ArrayList<Element>();
		try {
			readDomainService();
		} catch (SAXException e1) {
			throw new RuntimeException("SAXException: " + e1);
		} catch (IOException e2) {
			throw new RuntimeException("IOException: " + e2);
		} catch (ParserConfigurationException e3) {
			throw new RuntimeException("ParserConfigurationException: " + e3);
		}
	}
	
	public final void readDomainService() throws SAXException, IOException,
	ParserConfigurationException {		
		File fileOfService = new File(this.uuid);
		this.docOfServiceFileDescr = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(fileOfService));
		final NodeList listaNodes = this.docOfServiceFileDescr.getElementsByTagName(DomainApplicationContext.SERVICE_ELEMENT);
		if (listaNodes.getLength() == 0) {// miramos si este fichero tiene servicios
			throw new RuntimeException(new StringBuilder(InternalErrorsConstants.SERVICE_LITERAL).append(" ")
					.append(" need some service nodes").toString());
		}
		for (int i = 0; i < listaNodes.getLength(); i++) {
			final Element serviceNode = (Element) listaNodes.item(i);
			if (!serviceNode.hasAttribute(DomainApplicationContext.NAME_ATTR)) {
				throw new RuntimeException(new StringBuilder(InternalErrorsConstants.SERVICE_LITERAL).append(" ")
						.append(" need NAME ATTRIBUTE in metamodel file definition").toString());
			}
			this.subCases.add(serviceNode);
		}
	}
	
	public String getUseCaseName(){
		return this.useCaseName;
	}
	
	public String getUUID(){
		return this.uuid;
	}
	
	public Document getDocOfServiceFileDescr(){
		return this.docOfServiceFileDescr;
	}
	
	public List<Element> getSubCases(){				
		return this.subCases;
	}
	
	public List<String> getSubCasesNames(){	
		List<String> listaSCUs = new ArrayList<String>();
		for (int i=0; i< this.getSubCases().size(); i++){
			Element subcase = this.getSubCases().get(i);
			listaSCUs.add(subcase.getAttribute(DomainApplicationContext.NAME_ATTR));
		}
		return listaSCUs;
	}
	
	public Element getSubCaseOfServiceName (final String subcaseName){
		for (int i=0; i< this.getSubCases().size(); i++){
			Element subcase = this.getSubCases().get(i);
			if (subcase.getAttribute(DomainApplicationContext.NAME_ATTR).equals(subcaseName)) {
				return subcase;
			}
		}
		return null;
	}
	
	public final Collection<String> discoverAllEvents(final String subcaseName) {
		Collection<String> events = new ArrayList<String>();
		Collection<Element> actionSet =  discoverAllActions(subcaseName);
		Iterator<Element> iteActionSet = actionSet.iterator();
		while (iteActionSet.hasNext()){
			Element actionElement = iteActionSet.next();
			events.add(actionElement.getAttribute(DomainApplicationContext.EVENT_ATTR));
		}
		return events;
	}
	
	public final Element extractActionElementByService(final String subcaseName, final String actionName)
			throws PCMConfigurationException {
		final NodeList listaNodes = getSubCaseOfServiceName(subcaseName).getElementsByTagName(DomainApplicationContext.ACTION_ELEMENT);
		for (int i = 0; i < listaNodes.getLength(); i++) {
			final Element node = (Element) listaNodes.item(i);
			if (node.getAttributes() != null && node.hasAttribute(DomainApplicationContext.EVENT_ATTR)
					&& actionName.toLowerCase().endsWith(node.getAttribute(DomainApplicationContext.EVENT_ATTR).toLowerCase()) ) {
				return node;
			}
		}
		final StringBuilder excep = new StringBuilder(InternalErrorsConstants.ACTION_LITERAL).append(actionName).append(
				PCMConstants.STRING_SPACE);
		excep.append(InternalErrorsConstants.SERVICE_LITERAL).append(this.docOfServiceFileDescr).append(InternalErrorsConstants.NOT_FOUND_LITERAL);
		throw new PCMConfigurationException(excep.toString());
	}

	
	
	public final Collection<Element> extractViewComponentElementsByEvent(final String subcaseName, final String event)
			throws PCMConfigurationException {
		Collection<Element> arrViewComponents = new ArrayList<Element>();
		final NodeList _listaNodes = getSubCaseOfServiceName(subcaseName).getElementsByTagName(DomainApplicationContext.ACTION_ELEMENT);
		for (int i = 0; i < _listaNodes.getLength(); i++) {
			final Element actionParentNode = (Element) _listaNodes.item(i);
			if (actionParentNode.hasAttribute(DomainApplicationContext.EVENT_ATTR)) {
				if (event.equals(actionParentNode.getAttribute(DomainApplicationContext.EVENT_ATTR))) {
					final NodeList listaNodes_ = actionParentNode.getElementsByTagName(DomainApplicationContext.VIEWCOMPONENT_ELEMENT);
					for (int j = 0; j < listaNodes_.getLength(); j++) {
						arrViewComponents.add((Element) listaNodes_.item(j));
					}
				}
			}
		}
		if (arrViewComponents.isEmpty()) {
			throw new PCMConfigurationException(new StringBuilder(InternalErrorsConstants.ACTION_LITERAL).append(event)
					.append(InternalErrorsConstants.NOT_FOUND_LITERAL).toString());
		}
		return arrViewComponents;
	}	
	
	
}
