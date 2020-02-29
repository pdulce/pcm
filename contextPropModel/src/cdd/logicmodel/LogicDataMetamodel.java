package cdd.logicmodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cdd.common.PCMConstants;
import cdd.comunication.dispatcher.CDDWebController;


/**
 * <h1>LogicDataMetamodel</h1> The LogicDataMetamodel class is responsible of manage the application
 * metamodel, accesing its own structure through a DOM implementation.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class LogicDataMetamodel implements ILogicDataMetamodel {

	private Document root;

	private String name;

	@Override
	public String getName() {
		return this.name;
	}

	public LogicDataMetamodel(final String name_, final InputStream uriXML) {
		try {
			this.root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(uriXML);
			this.name = name_;
		}
		catch (final Throwable exc) {
			CDDWebController.log.log(Level.SEVERE, "Error", exc);
		} finally {
			try {
				if (uriXML != null) {
					uriXML.close();
				}
			}
			catch (final IOException ioo) {
				// ignored
				CDDWebController.log.log(Level.SEVERE, "Error", ioo);
			}
		}
	}

	@Override
	public Collection<Element> getAllEntities() {
		final Collection<Element> entitiesArr = new ArrayList<Element>();
		final NodeList entidadesNodes = this.root.getElementsByTagName(PCMConstants.ENTITY_NODENAME);
		int entitiesNodesCount= entidadesNodes.getLength();
		for (int i = 0; i < entitiesNodesCount; i++) {
			entitiesArr.add((Element) entidadesNodes.item(i));
		}
		return entitiesArr;
	}

}
