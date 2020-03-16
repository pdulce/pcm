package domain.service.dataccess;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import domain.common.PCMConstants;


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

	protected static Logger log = Logger.getLogger(LogicDataMetamodel.class.getName());
	
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
			LogicDataMetamodel.log.log(Level.SEVERE, "Error", exc);
		} finally {
			try {
				if (uriXML != null) {
					uriXML.close();
				}
			}
			catch (final IOException ioo) {
				// ignored
				LogicDataMetamodel.log.log(Level.SEVERE, "Error", ioo);
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
