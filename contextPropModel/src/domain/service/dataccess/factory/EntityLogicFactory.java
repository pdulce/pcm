package domain.service.dataccess.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import domain.common.PCMConstants;
import domain.common.exceptions.PCMConfigurationException;
import domain.service.dataccess.definitions.EntityLogic;
import domain.service.dataccess.definitions.IFieldLogic;


public class EntityLogicFactory implements IEntityLogicFactory {

	/** * instancia onica de la factoroa * */
	private static EntityLogicFactory entityFactory_;
	
	protected static Logger log = Logger.getLogger(EntityLogicFactory.class.getName());
	
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


	/** * miembros de Objeto * */
	private final Map<String, Map<String, EntityLogic>> dataDictionaries;

	private final Map<String, Collection<Element>> xmlDictionaries;

	public static EntityLogicFactory getFactoryInstance() {
		if (EntityLogicFactory.entityFactory_ == null) {
			EntityLogicFactory.entityFactory_ = new EntityLogicFactory();
		}
		return EntityLogicFactory.entityFactory_;
	}

	private EntityLogicFactory() {
		this.dataDictionaries = new HashMap<String, Map<String, EntityLogic>>();
		this.xmlDictionaries = new HashMap<String, Collection<Element>>();
	}

	@Override
	public boolean isInitiated(final String dictionary) {
		try {
			return this.getEntityMap(dictionary) != null;
		}
		catch (final Throwable confExc) {
			EntityLogicFactory.log.log(Level.SEVERE, "Error", confExc);
			return false;
		}
	}
	
	@Override
	public boolean existsInDictionaryMap(final String dictionary, String entidadName) {
		Map<String, EntityLogic> mapaEntidades = getEntityMap(dictionary);
		Iterator<EntityLogic> iteradorEntidades = mapaEntidades.values().iterator();
		while (iteradorEntidades.hasNext()){
			EntityLogic entidadIesima = iteradorEntidades.next();
			if (entidadIesima.getName().equals(entidadName)){
				return true;
			}
		}
		return false;
	}

	@Override
	public Map<String, EntityLogic> getEntityMap(final String dictionary) {
		return this.dataDictionaries.get(dictionary);
	}

	public EntityLogic getEntityDef(final String dictionary, final String entidad) throws PCMConfigurationException {
		String entidad_ = entidad;
		if (entidad_ != null && entidad_.endsWith(PCMConstants.XML_EXTENSION)) {
			entidad_ = entidad_.substring(0, entidad_.length() - 4);
		}
		if (this.dataDictionaries.get(dictionary) == null) {
			throw new PCMConfigurationException("You must initialize the Entities Logic Model");
		}
		if (this.dataDictionaries.get(dictionary).get(entidad_) != null) {
			return this.dataDictionaries.get(dictionary).get(entidad_);
		}		
		if (this.xmlDictionaries.get(dictionary) == null) {
			this.xmlDictionaries.put(dictionary, LogicDataMetamodelFactory.getFactoryInstance().getLogicDataMetamodel(dictionary)
					.getAllEntities());
		}
		try {
			final Iterator<Element> ite = this.xmlDictionaries.get(dictionary).iterator();
			while (ite.hasNext()) {
				final Element entityNode = ite.next();
				final NodeList tableNameNode = entityNode.getElementsByTagName(EntityLogic.ENTITY_NODENAME);
				if (tableNameNode == null || tableNameNode.getLength() == 0) {
					throw new PCMConfigurationException(new StringBuilder("Error when searching tablename node in entity named as ")
							.append(entidad_).toString());
				}
				if (entidad_ != null && entidad_.equals(tableNameNode.item(0).getFirstChild().getNodeValue())) {
					final EntityLogic entityLogic = new EntityLogic(dictionary, entityNode);
					if (this.dataDictionaries.get(dictionary).get(entityLogic.getName()) == null) {
						this.dataDictionaries.get(dictionary).put(entityLogic.getName(), entityLogic);
					}
					return this.dataDictionaries.get(dictionary).get(entityLogic.getName());
				}
			}// while
		}
		catch (final Throwable exc) {
			EntityLogicFactory.log.log(Level.SEVERE, "Error", exc);
			throw new PCMConfigurationException("Error:".concat(exc.getMessage()), exc);
		}
		throw new PCMConfigurationException(new StringBuilder("Error when searching tablename node for this entity: ").append(entidad_)
				.toString());
	}

	@Override
	public void initEntityFactory(final String dictionary, final InputStream dictionaryStream) throws PCMConfigurationException {
		
		if (LogicDataMetamodelFactory.getFactoryInstance() != null) {
			if (!LogicDataMetamodelFactory.getFactoryInstance().isInitiated(dictionary)) {
				LogicDataMetamodelFactory.getFactoryInstance().initLogicDataMetamodel(
						dictionary, dictionaryStream);
			}
		}
		
		if (this.xmlDictionaries.get(dictionary) == null) {
			this.xmlDictionaries.put(dictionary, LogicDataMetamodelFactory.getFactoryInstance().getLogicDataMetamodel(dictionary)
					.getAllEntities());
		}
		StringBuilder constantes = new StringBuilder(), diccionario = new StringBuilder();
		final Iterator<Element> ite = this.xmlDictionaries.get(dictionary).iterator();
		while (ite.hasNext()) {
			try {
				if (this.dataDictionaries.get(dictionary) == null) {
					this.dataDictionaries.put(dictionary, new HashMap<String, EntityLogic>());
				}
				final Element element = ite.next();
				final EntityLogic entityLogic = new EntityLogic(dictionary, element);
				Iterator<Map.Entry<String, IFieldLogic>> iteFieldSet = entityLogic.getFieldSet().entrySet().iterator();
				while (iteFieldSet.hasNext()) {
					Map.Entry<String, IFieldLogic> entry = iteFieldSet.next();
					IFieldLogic field = entry.getValue();
					String nameOfConstant = entityLogic.getName().concat("_").concat(String.valueOf(field.getMappingTo()))
							.concat("_" + field.getName()).toUpperCase();
					String nameOfProperty = entityLogic.getName().concat(".").concat(field.getName());
					diccionario.append(nameOfProperty + " = " + field.getName() + "\n");
					constantes.append("public static final int " + nameOfConstant + " = " + field.getMappingTo() + ";\n");
				}
				diccionario.append(entityLogic.getName() + "." + entityLogic.getName() + " = " + entityLogic.getName() + "\n");
				constantes.append("public static final String " + entityLogic.getName().toUpperCase() + "_ENTIDAD = \""
						+ entityLogic.getName() + "\";\n");
				if (this.dataDictionaries.get(dictionary).get(entityLogic.getName()) == null) {
					this.dataDictionaries.get(dictionary).put(entityLogic.getName(), entityLogic);
				}
			}
			catch (final Throwable exc) {
				EntityLogicFactory.log.log(Level.SEVERE, "Error", exc);
				throw new PCMConfigurationException(
						"Error when reading file .xml for entity : check if defined the name (case-sensitive mode on) of the table for this entity: ",
						exc);
			}
		}// while
		
		FileOutputStream fout = null;
		try{
			fout = new FileOutputStream(new File("constantes.java"));
			fout.write(constantes.toString().getBytes());
		}catch (Throwable excdd){
			excdd.printStackTrace();
		}finally {
			try {
				fout.flush();
				fout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		FileOutputStream fout2 = null;
		try{
			fout2 = new FileOutputStream(new File("dicc.txt"));
			fout2.write(diccionario.toString().getBytes());
		}catch (Throwable excdd){
			excdd.printStackTrace();
		}finally {
			try {
				fout2.flush();
				fout2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
