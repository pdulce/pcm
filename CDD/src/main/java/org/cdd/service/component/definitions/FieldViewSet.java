package org.cdd.service.component.definitions;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cdd.common.PCMConstants;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.XmlUtils;
import org.cdd.service.component.element.ICtrl;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.comparator.ComparatorFieldLogic;
import org.cdd.service.dataccess.definitions.EntityLogic;
import org.cdd.service.dataccess.definitions.FieldAbstract;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.definitions.IFieldAbstract;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.FieldValue;
import org.cdd.service.dataccess.dto.IFieldValue;


public class FieldViewSet implements Serializable {

	public static final char FIELD_SEPARATOR = PCMConstants.CHAR_POINT;
	public static final String ENTITYMODEL_ATTR = "entitymodel";

	private static final long serialVersionUID = 699999999999999L;

	private IEntityLogic entityLogic;

	private Collection<IFieldView> fieldViews;

	private Map<String, IFieldValue> fieldViewsValues;

	private int order = 0;

	private boolean persist = true;

	private String contextName, dictionaryName, nameSpace;

	private FieldViewSet(final String dict, final String contexto) {
		this.dictionaryName = dict;
		this.contextName = contexto;
		this.fieldViewsValues = new HashMap<String, IFieldValue>();
	}
	
	public FieldViewSet(final IEntityLogic entidad_) {
		this.fieldViews = new ArrayList<IFieldView>();
		final Iterator<IFieldLogic> iteFieldEntities = entidad_.getFieldSet().values().iterator();
		while (iteFieldEntities.hasNext()) {
			this.fieldViews.add(new FieldView(iteFieldEntities.next()));
		}
		this.dictionaryName = entidad_.getDictionaryName();
		this.contextName = entidad_.getName();
		this.entityLogic = entidad_;
		this.initValuesMap();
	}

	public FieldViewSet(final String dict, final String nameContext_, final Collection<IFieldView> mappings) {
		this.setNameSpace(nameContext_);		
		this.dictionaryName = dict;
		if (mappings.isEmpty()) {
			return;
		}
		this.contextName = mappings.iterator().next().getContextName();
		Iterator<IFieldView> iteFieldViews = mappings.iterator();
		this.fieldViews = new ArrayList<IFieldView>();
		while (iteFieldViews.hasNext()) {
			this.fieldViews.add(iteFieldViews.next().copyOf());
		}
		final Iterator<IFieldView> iteF = mappings.iterator();
		while (iteF.hasNext()) {
			final IFieldView f = iteF.next();
			if (f.isSeparator()) {
				continue;
			}
			if (f.isUserDefined()) {
				final FieldValue val = new FieldValue();
				val.setValue(f.getDefaultValueExpr());
				if (this.fieldViewsValues != null) {
					this.fieldViewsValues.clear();
				} else {
					this.fieldViewsValues = new HashMap<String, IFieldValue>();
				}
				this.fieldViewsValues.put(f.getQualifiedContextName(), val);
				return;
			}
			this.entityLogic = f.getEntityField().getEntityDef();
		}
		this.initValuesMap();
	}

	public FieldViewSet(final String dict, final String nameContext_, final Collection<IFieldView> mappings,
			final FieldViewSet prototipoBase) {
		this.contextName = mappings.iterator().next().getContextName();
		this.nameSpace = prototipoBase.getNameSpace();
		this.entityLogic = prototipoBase.getEntityDef();
		this.dictionaryName = this.entityLogic != null ? this.entityLogic.getDictionaryName() : dict;
		Iterator<IFieldView> iteFieldViews = mappings.iterator();
		this.fieldViews = new ArrayList<IFieldView>();
		while (iteFieldViews.hasNext()) {
			this.fieldViews.add(iteFieldViews.next().copyOf());
		}
		this.initValuesMap(prototipoBase);
	}

	/********** END OF CONSTRUCTORS ************** */
	
	public String getNameSpace() {
		return this.nameSpace;
	}

	public void setNameSpace(String nameSpace_) {
		this.nameSpace = nameSpace_;
	}
	
	public FieldViewSet searchInFieldViewSetCollection(final Collection<FieldViewSet> fieldViewSets) {
		final Iterator<FieldViewSet> fieldSetIte = fieldViewSets.iterator();
		while (fieldSetIte.hasNext()) {
			final FieldViewSet fieldset = fieldSetIte.next();
			if (fieldset.getContextName().toUpperCase().equals(this.getContextName().toUpperCase())) {
				return fieldset;
			}
		}
		return null;
	}
	
	
	public IFieldLogic getDescriptionField(){
		
		List<IFieldLogic> newCollection = new ArrayList<IFieldLogic>();
		newCollection.addAll(this.getEntityDef().getFieldSet().values());
		Collections.sort(newCollection, new ComparatorFieldLogic());
		for (int i=0;i<newCollection.size();i++){
			IFieldLogic descField = newCollection.get(i);
			if (!descField.belongsPK() && !descField.isSequence()){
				if (descField.getParentFieldEntities()==null){
					return descField;
				}
			}
		}
		return null;
	}
	
	public List<IFieldLogic> getDescriptionFieldList(){
		
		List<IFieldLogic> resultDescFields = new ArrayList<IFieldLogic>();
		int max = 1;
		List<IFieldLogic> newCollection = new ArrayList<IFieldLogic>();
		newCollection.addAll(this.getEntityDef().getFieldSet().values());
		Collections.sort(newCollection, new ComparatorFieldLogic());
		for (int i=0;i<newCollection.size();i++){
			IFieldLogic descField = newCollection.get(i);
			if (!descField.belongsPK() && !descField.isSequence() && !descField.getAbstractField().isBoolean() && descField.getParentFieldEntities()==null){
				resultDescFields.add(descField);
				if (resultDescFields.size() == max){
					break;
				}
			}
		}
		return resultDescFields;
	}
	
	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order_) {
		this.order = order_;
	}

	public void setPersist(boolean b) {
		this.persist = b;
	}

	public boolean needPersist() {
		return this.persist;
	}

	public void setDictionaryName(String dict) {
		this.dictionaryName = dict;
	}

	public void setContextName(String contextName_) {
		this.contextName = contextName_;
	}

	public void setEntityDef(final EntityLogic entityLogic) {
		this.entityLogic = entityLogic;
	}

	public boolean isUserDefined() {
		return this.entityLogic == null;
	}

	public String getContextName() {
		return this.contextName;
	}

	protected String getNodeName() {
		return this.entityLogic.getName();
	}

	public IEntityLogic getEntityDef() {
		return this.entityLogic;
	}

	public String getDictionaryName() {
		return this.dictionaryName;
	}
	
	public void resetFieldValuesMap() {
		Iterator<String> keysOfValues = this.fieldViewsValues.keySet().iterator();
		while (keysOfValues.hasNext()) {
			this.fieldViewsValues.put(keysOfValues.next(), new FieldValue());
		}
	}
	
	public final void refreshUserFilter(final Collection<FieldViewSet> fieldViewSetsForm,  final IDataAccess dataAccess, 
			final Map<String, List<Object>> valuesMemo_) {
		
		try {
			//obtenemos padres de esta entidad por si en el formulario vienen filtros de esa otra entidad padre:			
			IEntityLogic entidadARefrescar = this.getEntityDef();
			 
			Map<String, FieldViewSet> recordParentMap = new HashMap<String, FieldViewSet>();			
			Map<String, IEntityLogic> parentEntitiesMap = new HashMap<String, IEntityLogic>();			
			Collection<IEntityLogic> padres = entidadARefrescar.getParentEntities();
			// de momento, solo me quedo con un padre para probar el algoritmo, y luego extenderemos
			if (padres != null && !padres.isEmpty()) {
				 Iterator<IEntityLogic> padresIterator = padres.iterator();
				 while (padresIterator.hasNext()) {					 
					 IEntityLogic padreDeEntidadARefrescar = padresIterator.next();
					 parentEntitiesMap.put(padreDeEntidadARefrescar.getName(), padreDeEntidadARefrescar);
				 }
			}
			final List<Map.Entry<String, List<Object>>> listOfRequestParams = new ArrayList<Map.Entry<String, List<Object>>>(valuesMemo_.entrySet());
			Iterator<FieldViewSet> iteFieldViewSetsOfForm = fieldViewSetsForm.iterator();
			while (iteFieldViewSetsOfForm.hasNext()) {
				FieldViewSet fSetOfForm = iteFieldViewSetsOfForm.next();
				
				for (int i=0;i<listOfRequestParams.size();i++) {
					final Map.Entry<String, List<Object>> entry = listOfRequestParams.get(i);
					String keyMemorized = entry.getKey();
					String[] fieldInRequest = keyMemorized.split(PCMConstants.REGEXP_POINT);
					String entityInRequest = fieldInRequest[0];
					String field_ =  keyMemorized.contains(".")? fieldInRequest[1]: keyMemorized;
					final List<Object> values = entry.getValue();
					if ((values == null || values.isEmpty() || values.get(0) == null || 
							"".contentEquals(values.get(0).toString())) || fieldInRequest.length == 1) {
						continue;
					}
					String fieldName_rank = keyMemorized;
					fieldName_rank = fieldName_rank.replace(IRank.DESDE_SUFFIX, "");
					fieldName_rank = fieldName_rank.replace(IRank.HASTA_SUFFIX, "");
					IFieldView fView = this.getFieldView(fieldName_rank);
					if (fView != null && keyMemorized.contains(IRank.HASTA_SUFFIX)) {
						final Rank rankHasta = new Rank(fView.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
						if (fView.isRankField()) {
							IFieldView fHasta = fView.copyOf();
							fHasta.setQualifiedContextName(keyMemorized);						
							fHasta.setRankField(rankHasta);						
							this.fieldViews.add(fHasta);
						}else {
							fView.setQualifiedContextName(keyMemorized);
							fView.setRankField(rankHasta);
						}
					}else if (fView != null && keyMemorized.contains(IRank.DESDE_SUFFIX)) {
						final Rank rankDesde = new Rank(fView.getEntityField().getName(), IRank.MINOR_EQUALS_OPE);
						if (fView.isRankField()) {
							IFieldView fDesde = fView.copyOf();
							fDesde.setQualifiedContextName(keyMemorized);
							fDesde.setRankField(rankDesde);
							this.fieldViews.add(fDesde);
						}else {
							fView.setQualifiedContextName(keyMemorized);
							fView.setRankField(rankDesde);
						}
					}
					
					List<String> stringVals = new ArrayList<String>();
					for (Object val: values) {
						if (!"".equals(val.toString())){
							stringVals.add(val.toString());
						}
					}
					
					if (entityInRequest.contentEquals(entidadARefrescar.getName()) && 
							entidadARefrescar.getName().contentEquals(fSetOfForm.getEntityDef().getName())) {
												
						if (!stringVals.isEmpty()) {
							this.setValues(keyMemorized, stringVals);
						}
						
					}else if (!parentEntitiesMap.isEmpty() && parentEntitiesMap.keySet().contains(entityInRequest)
							&& entityInRequest.contentEquals(fSetOfForm.getEntityDef().getName())) {
						
						if (!stringVals.isEmpty()) {
							//imagina que llega la fecha con un valor			
							keyMemorized = keyMemorized.replace(IRank.DESDE_SUFFIX, "");
							keyMemorized = keyMemorized.replace(IRank.HASTA_SUFFIX, "");
							FieldViewSet recordparent = recordParentMap.get(entityInRequest);
							if (recordparent == null) {
								recordparent = new FieldViewSet(parentEntitiesMap.get(entityInRequest));
								recordParentMap.put(entityInRequest, recordparent);
							}														
							recordparent.setValues(field_, stringVals);							
						}
						
					}
				}// fin del recorrrido de un fieldviewset
				
				//hacemos esto por cada fset localizado
				if (recordParentMap.get(fSetOfForm.getEntityDef().getName()) != null) {
					
					IFieldLogic keyOfParent = fSetOfForm.getEntityDef().getFieldKey().getPkFieldSet().iterator().next();
					Collection<FieldViewSet> parents = dataAccess.searchByCriteria(recordParentMap.get(fSetOfForm.getEntityDef().getName()));
					Iterator<FieldViewSet> iteParents = parents.iterator();
					Collection<String> fkValues = new ArrayList<String>();
					while (iteParents.hasNext()) {
						FieldViewSet parentSearched = iteParents.next();
						//obtenemos el valor PK de ese padre que engancha con la FK de nuestra entidad								
						String keyOfParentValue = parentSearched.getValue(keyOfParent.getName()).toString();
						fkValues.add(keyOfParentValue);
					}
					//buscamos nuestro valor FK de esta entidad
					IFieldLogic fkOfThisParent = entidadARefrescar.getFkFields(keyOfParent).iterator().next();
					//damos valor acumulativo en nuestra entidad
					this.setValues(fkOfThisParent.getName(), fkValues);
				}
			}
				
			
		} catch (final Throwable exc) {
			exc.printStackTrace();
		}
	}

	
	public FieldViewSet copyOf() {
		try {
			final FieldViewSet newFieldSet = new FieldViewSet(this.dictionaryName, this.contextName);
			newFieldSet.nameSpace = this.getNameSpace();
			newFieldSet.persist = this.persist;
			newFieldSet.order = this.order;
			newFieldSet.entityLogic = this.entityLogic;
			newFieldSet.dictionaryName = this.dictionaryName;
			newFieldSet.fieldViews = new ArrayList<IFieldView>();
			newFieldSet.fieldViewsValues = new HashMap<String, IFieldValue>();
			final List<IFieldView> _listOfFields = new ArrayList<IFieldView>(this.fieldViews);
			for (int i=0;i<_listOfFields.size();i++) {
				final IFieldView fView = _listOfFields.get(i);				
				final IFieldValue fValue = this.fieldViewsValues.get(fView.getQualifiedContextName());
				final IFieldValue newFValue = new FieldValue();
				newFValue.setValues(fValue == null ? new ArrayList<String>() : fValue.getValues());
				newFieldSet.fieldViewsValues.put(fView.getQualifiedContextName(), newFValue);				
				newFieldSet.fieldViews.add(fView.copyOf());
			}
			if (newFieldSet.fieldViewsValues.isEmpty()) {
				newFieldSet.initValuesMap();
			}
			return newFieldSet;
		}
		catch (final Throwable exc) {
			return null;
		}
	}

	public Map<String, Serializable> getPkOfEntity() {
		final Map<String, Serializable> pk = new HashMap<String, Serializable>();
		final Iterator<IFieldView> ite = this.getFieldViews().iterator();
		while (ite.hasNext()) {
			final IFieldView fieldView = ite.next();
			if (fieldView.getEntityField().belongsPK()) {
				pk.put(fieldView.getQualifiedContextName(), this.getFieldvalue(fieldView.getQualifiedContextName()).getValue());
			}
		}
		return pk;
	}

	public boolean isEqualsPk(final FieldViewSet f) {
		if (f == null) {
			return false;
		}
		final Iterator<IFieldLogic> iteFields = this.getEntityDef().getFieldKey().getPkFieldSet().iterator();
		while (iteFields.hasNext()) {
			final IFieldLogic fieldPk = iteFields.next();
			final StringBuilder nameQ = new StringBuilder(this.getContextName());
			nameQ.append(FieldViewSet.FIELD_SEPARATOR).append(fieldPk.getName());

			final StringBuilder nameQ2 = new StringBuilder(f.getContextName());
			nameQ2.append(FieldViewSet.FIELD_SEPARATOR).append(fieldPk.getName());

			if (!this.getFieldvalue(nameQ.toString()).isEquals(f.getFieldvalue(nameQ2.toString()))) {
				return false;
			}
		}
		return true;
	}

	public boolean isEmpty() {
		final Iterator<IFieldValue> values_ = this.fieldViewsValues.values().iterator();
		while (values_.hasNext()) {
			if (!values_.next().isNull()) {
				return false;
			}
		}
		return true;
	}

	public void updateValuesOfData(final FieldViewSet received) {
		this.fieldViewsValues = new HashMap<String, IFieldValue>();
		final Iterator<IFieldView> iteValues = received.getFieldViews().iterator();
		while (iteValues.hasNext()) {
			final IFieldView field = iteValues.next();
			this.fieldViewsValues.put(field.getQualifiedContextName(), received.getFieldvalue(field.getEntityField()));
		}
	}

	public void updateValuesOfPkData(final FieldViewSet received) {
		this.fieldViewsValues = new HashMap<String, IFieldValue>();
		final Iterator<IFieldView> iteValues = received.getFieldViews().iterator();
		while (iteValues.hasNext()) {
			final IFieldView field = iteValues.next();
			if (field.getEntityField().belongsPK()) {
				this.fieldViewsValues.put(field.getQualifiedContextName(), received.getFieldvalue(field.getEntityField()));
			}
		}
	}

	public Map<String, IFieldValue> getNamedValues() {
		if (this.fieldViewsValues == null) {
			this.fieldViewsValues = new HashMap<String, IFieldValue>();
		}
		return this.fieldViewsValues;
	}

	public void setNamedValues(final Map<String, IFieldValue> newValues_) {
		if (newValues_ == null) {
			this.fieldViewsValues = new HashMap<String, IFieldValue>();
		} else {
			if (this.fieldViewsValues == null) {
				this.fieldViewsValues = new HashMap<String, IFieldValue>();
			}
			final Iterator<Map.Entry<String, IFieldValue>> iteEntries = newValues_.entrySet().iterator();
			while (iteEntries.hasNext()) {
				final Map.Entry<String, IFieldValue> entry = iteEntries.next();
				Collection<Map<String, Boolean>> values = this.fieldViewsValues.get(entry.getKey()).getAllValues();
				String valorASetear = entry.getValue().getValue();
				if (valorASetear == null) {
					continue;
				}
				if (values.isEmpty()) {
					Map<String, Boolean> valor = new HashMap<String, Boolean>();
					valor.put(valorASetear, Boolean.TRUE);
					values.add(valor);
				} else {
					Iterator<Map<String, Boolean>> iteEntris = values.iterator();
					while (iteEntris.hasNext()) {
						Map<String, Boolean> entryPrevious = iteEntris.next();
						Iterator<Map.Entry<String, Boolean>> entriIte2 = entryPrevious.entrySet().iterator();
						while (entriIte2.hasNext()) {
							Map.Entry<String, Boolean> entry1 = entriIte2.next();
							String[] split = entry1.getKey().toString().toUpperCase().split(PCMConstants.EQUALS);
							String valueOption = "";
							if (split.length == 1) {
								valueOption = split[0];
							} else if (split.length == 2) {
								valueOption = split[1];
							}
							if (valueOption.equals(valorASetear.toString().toUpperCase())) {
								entry1.setValue(Boolean.TRUE);
							}
						}
					}
				}
			}
		}
	}

	public void addFieldViews(final Collection<IFieldView> fieldViewDefCol) {
		if (this.fieldViews == null) {
			this.fieldViews = new ArrayList<IFieldView>();
		}
		final Iterator<IFieldView> fieldViewsIte = fieldViewDefCol.iterator();
		while (fieldViewsIte.hasNext()) {
			final IFieldView fieldView = fieldViewsIte.next();
			if (!fieldView.isUserDefined() && fieldView.getQualifiedContextName() == null) {
				final StringBuilder strB = new StringBuilder(this.getContextName());
				strB.append(FieldViewSet.FIELD_SEPARATOR).append(fieldView.getEntityField().getName());
				fieldView.setQualifiedContextName(strB.toString());
				this.fieldViews.add(fieldView);
			} else if (this.getFieldView(fieldView.getQualifiedContextName()) == null) {
				this.fieldViews.add(fieldView);
			}
		}
	}
	
	public void addFieldView(final IFieldView fieldView_) {
		if (this.fieldViews == null) {
			this.fieldViews = new ArrayList<IFieldView>();
		}
		this.fieldViews.add(fieldView_);
	}

	private void initValuesMap() {
		this.initValuesMap(null);
	}

	public boolean isNullPrimaryKey() {
		if (this.getEntityDef() == null) {
			return true;
		}
		if (this.fieldViewsValues == null || this.fieldViewsValues.isEmpty()) {
			return true;
		}
		final Iterator<IFieldLogic> iteFields = this.getEntityDef().getFieldKey().getPkFieldSet().iterator();
		while (iteFields.hasNext()) {
			if (this.getFieldvalue(iteFields.next()).isNull()) {
				return true;
			}
		}
		return false;
	}

	private void initValuesMap(final FieldViewSet prototipoBase) {

		this.fieldViewsValues = new HashMap<String, IFieldValue>();
		if (prototipoBase != null) {
			final Iterator<IFieldView> fieldViewsIte = prototipoBase.getFieldViews().iterator();
			while (fieldViewsIte.hasNext()) {
				final IFieldView fieldView = fieldViewsIte.next();
				if (!fieldView.isUserDefined()) {
					final StringBuilder strB = new StringBuilder(this.getContextName());
					strB.append(FieldViewSet.FIELD_SEPARATOR).append(fieldView.getEntityField().getName());
					fieldView.setQualifiedContextName(strB.toString());
					final IFieldValue fvalue = new FieldValue();
					fvalue.setValues(prototipoBase.getFieldvalue(fieldView.getEntityField().getName()).getValues());
					this.fieldViewsValues.put(fieldView.getQualifiedContextName(), fvalue);
				}
			}
			return;
		}
		final Iterator<IFieldView> mappinsToIterator = this.fieldViews.iterator();
		while (mappinsToIterator.hasNext()) {
			final IFieldView fieldView = mappinsToIterator.next();
			if (fieldView.getQualifiedContextName() == null) {
				final StringBuilder strB = new StringBuilder();
				if (!fieldView.isUserDefined() && fieldView.isAggregate()) {
					strB.append(this.getContextName()).append(FieldViewSet.FIELD_SEPARATOR)
							.append(fieldView.getAggregateField().getFormulaSQL());
					strB.append(PCMConstants.UNDERSCORE).append(fieldView.getEntityField().getName());
					fieldView.setQualifiedContextName(strB.toString());
				} else if (!fieldView.isUserDefined() && fieldView.isRankField()) {
					strB.append(this.getContextName()).append(FieldViewSet.FIELD_SEPARATOR).append(fieldView.getRankField().getName());
					fieldView.setQualifiedContextName(strB.toString());
				} else if (!fieldView.isUserDefined()) {
					strB.append(this.getContextName()).append(FieldViewSet.FIELD_SEPARATOR).append(fieldView.getEntityField().getName());
					fieldView.setQualifiedContextName(strB.toString());
				}
			}
			if (!fieldView.isUserDefined()) {
				if (fieldView.hasNOptionsToChoose() && fieldView.getFieldAndEntityForThisOption() != null
						&& fieldView.getFieldAndEntityForThisOption().getOptions() != null
						&& !fieldView.getFieldAndEntityForThisOption().getOptions().isEmpty()) {
					final Collection<String> values_ = new ArrayList<String>();
					final Iterator<Option> iteOptions = fieldView.getFieldAndEntityForThisOption().getOptions().iterator();
					while (iteOptions.hasNext()) {
						values_.add(iteOptions.next().getCode());
					}
					this.fieldViewsValues.put(fieldView.getQualifiedContextName(), new FieldValue());
					this.fieldViewsValues.get(fieldView.getQualifiedContextName()).chargeValues(values_);
				} else {
					this.fieldViewsValues.put(fieldView.getQualifiedContextName(), new FieldValue());
				}
			} else {
				if (fieldView.isUserDefined() && this.fieldViewsValues.get(fieldView.getQualifiedContextName()) == null) {
					final FieldValue fieldValue = new FieldValue();
					fieldValue.setValue(fieldView.getDefaultValueExpr());
					this.fieldViewsValues.put(fieldView.getQualifiedContextName(), fieldValue);
				}
			}
		}
	}

	public IFieldView getFieldView(final String fieldName) {
		final StringBuilder qualifiedName = new StringBuilder();
		if (!this.isUserDefined() && fieldName.indexOf(FieldViewSet.FIELD_SEPARATOR) == -1) {
			qualifiedName.append(this.getContextName()).append(FieldViewSet.FIELD_SEPARATOR).append(fieldName);
		} else{
			qualifiedName.append(fieldName);
		}
		final Iterator<IFieldView> iteratorOfFieldViews = this.getFieldViews().iterator();
		while (iteratorOfFieldViews.hasNext()) {
			final IFieldView fieldView = iteratorOfFieldViews.next();
			if (fieldView.isSeparator()) {
				continue;
			}
			if (!fieldView.isUserDefined() && fieldView.getQualifiedContextName() == null) {
				final StringBuilder strB = new StringBuilder(this.getContextName());
				strB.append(FieldViewSet.FIELD_SEPARATOR).append(fieldView.getEntityField().getName());
				fieldView.setQualifiedContextName(strB.toString());
			}
			if (fieldView.isUserDefined() && fieldView.getQualifiedContextName().toUpperCase().equals(fieldName.toString().toUpperCase())) {
				return fieldView;
			}else if (fieldView.isUserDefined() && fieldView.getUserNamed().toUpperCase().equals(fieldName.toString().toUpperCase())) {
				return fieldView;
			} else if (!fieldView.isUserDefined()
					&& fieldView.getQualifiedContextName().toUpperCase().equals(qualifiedName.toString().toUpperCase())) {
				return fieldView;
			} else if (!fieldView.isUserDefined() && fieldView.getEntityField() != null
					&& this.getContextName().concat(PCMConstants.POINT).concat(fieldView.getEntityField().getName()).toUpperCase()
							.equals(qualifiedName.toString().toUpperCase())) {
				return fieldView;
			}
		}
		return null;
	}

	public void removeFieldView(IFieldView fView) {
		String fieldName = fView.getQualifiedContextName();
		final Iterator<IFieldView> iteratorOfFieldViews = this.getFieldViews().iterator();
		while (iteratorOfFieldViews.hasNext()) {
			final IFieldView fieldView = iteratorOfFieldViews.next();
			if (fieldView.getQualifiedContextName().equals(fieldName)
					|| (fieldView.getEntityField() != null
							&& fieldView.getEntityField().getEntityDef().getName().equals(fView.getEntityField().getEntityDef().getName()) && fieldView
							.getEntityField().getName().equals(fView.getEntityField().getName()))) {
				this.fieldViews.remove(fieldView);
				if (!fieldView.isRankField()) {
					break;
				}
			}
		}
	}

	public IFieldView getFieldView(final IFieldLogic fieldLogic) {
		final StringBuilder strB = new StringBuilder(this.getContextName()).append(FieldViewSet.FIELD_SEPARATOR);
		strB.append(fieldLogic.getName());
		return this.getFieldView(strB.toString());
	}

	public List<IFieldView> getDatePairFieldViews(final IFieldLogic fieldLogic) {
		List<IFieldView> pair = new ArrayList<IFieldView>();
		final Iterator<IFieldView> iteratorOfFieldViews = this.getFieldViews().iterator();
		while (iteratorOfFieldViews.hasNext()) {
			final IFieldView fieldView = iteratorOfFieldViews.next();
			if (fieldView.getEntityField() != null
					&& fieldLogic.getEntityDef().getName().equals(fieldView.getEntityField().getEntityDef().getName())
					&& fieldLogic.getName().equals(fieldView.getEntityField().getName())) {
				pair.add(fieldView);
				if (pair.size() == 2) {
					break;
				}
			}
		}
		return pair;
	}

	public List<IFieldView> getFieldViews() {
		List<IFieldView> retorno = new ArrayList<IFieldView>();
		if (this.fieldViews != null) {
			retorno.addAll(this.fieldViews);
		}
		return retorno;
	}
	
	public Collection<String> getValues(final int fieldPosition) {
		return getFieldvalue(this.entityLogic.searchField(fieldPosition).getName()).getValues();		
	}
	
	public Serializable getValue(final int fieldPosition) {
		IFieldLogic ifLogic = this.entityLogic.searchField(fieldPosition);
		if (ifLogic == null) {
			throw new RuntimeException("error en búsqueda de fieldPosition: " + fieldPosition);
		}
		return this.getValue(ifLogic.getName());		
	}
	
	public Serializable getValue(final String fieldName) {

		IFieldValue fieldValue_ = this.getFieldvalue(fieldName);
		StringBuilder qualifiedName_ = new StringBuilder(fieldName);
		if (qualifiedName_.toString().equals(fieldName) && fieldName.indexOf(".")==-1) {
			qualifiedName_ = new StringBuilder(this.contextName);
			qualifiedName_.append(FieldViewSet.FIELD_SEPARATOR).append(fieldName);
			final IFieldView fView = this.getFieldView(qualifiedName_.toString());
			Serializable value = null; 
			if (fieldValue_ == null || fieldValue_.isNull()) {
				value = this.getFieldvalue(qualifiedName_.toString()).getValue();
			}else{
				value = fieldValue_.getValue();
			}
			if (value == null){
				return null;
			}
			IFieldAbstract fieldAbstract = null;
			if (!isUserDefined()){
				fieldAbstract = fView.getEntityField().getAbstractField();
			}else{
				fieldAbstract = new FieldAbstract(fView.getType());
			}
			
			if (fieldAbstract.isDate()) {
				try {
					if ("".contentEquals(value.toString()) ){
						 return null;
					}
					if (fieldAbstract.isTimestamp()) {						 
						return new Timestamp(CommonUtils.myDateFormatter.parse(value.toString()).getTime());
					}
					return CommonUtils.myDateFormatter.parse(value.toString());	
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (fieldAbstract.isLong() && value instanceof String) {

				return Long.valueOf(value.toString().replaceAll(PCMConstants.REGEXP_POINT, ""));

			} else if (fieldAbstract.isInteger() && value instanceof String) {

				return Integer.valueOf(value.toString().replaceAll(PCMConstants.REGEXP_POINT, ""));

			} else if (fieldAbstract.isDecimal() && value instanceof String) {
				try {
					return CommonUtils.numberFormatter.parse(value.toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (fieldAbstract.isBoolean() && value instanceof String) {
				return ("1".contentEquals(value.toString()) || "true".contentEquals((value.toString()).toLowerCase()));
			} else {
				return value;
			}
		}
		
		if (fieldValue_ != null) {
			return fieldValue_.getValue();
		}
		return null;
	}

	public IFieldValue getFieldvalue(final IFieldLogic fieldLogic) {
		final StringBuilder nameQ = new StringBuilder(this.getContextName()).append(FieldViewSet.FIELD_SEPARATOR);
		nameQ.append(fieldLogic.getName());
		return this.getFieldvalue(nameQ.toString());
	}

	
	public IFieldValue getFieldvalue(final String qualifiedN) {
		if (qualifiedN == null) {
			return null;
		}

		IFieldValue valueF = null;
		StringBuilder qualifiedName_ = new StringBuilder(qualifiedN);
		if (!this.isUserDefined() && qualifiedName_.toString().indexOf(FieldViewSet.FIELD_SEPARATOR) == -1) {
			qualifiedName_ = new StringBuilder(this.contextName);
			qualifiedName_.append(FieldViewSet.FIELD_SEPARATOR).append(qualifiedN);
			final IFieldView fView = this.getFieldView(qualifiedName_.toString());
			if (fView == null) {
				valueF = new FieldValue();
				this.fieldViewsValues.put(qualifiedName_.toString(), valueF);
			} else {
				valueF = this.fieldViewsValues.get(qualifiedName_.toString());
				if (ICtrl.RADIO_TYPE.equals(fView.getType())) {
					final String value = fView.getValueOfOption(valueF.getValue());
					valueF.setValue(value);
					if (!valueF.isNull()) {
						this.fieldViewsValues.put(qualifiedName_.toString(), valueF);
					}
				}
			}
		} else {
			if (this.getFieldView(qualifiedName_.toString()) == null || this.fieldViewsValues.get(qualifiedName_.toString()) == null) {
				valueF = new FieldValue();
				this.fieldViewsValues.put(qualifiedName_.toString(), valueF);
			} else {
				valueF = this.fieldViewsValues.get(qualifiedName_.toString());
				IFieldView fView = getFieldView(qualifiedName_.toString());
				if (ICtrl.RADIO_TYPE.equals(fView.getType())) {
					final String value = fView.getValueOfOption(valueF.getValue());
					if (!valueF.isNull()) {
						valueF.setValue(value);
						this.fieldViewsValues.put(qualifiedName_.toString(), valueF);
					}
				}
			}
		}
		return valueF;
	}
	
	public void resetValues(final int fieldPosition_) {
		resetValues(this.entityLogic.searchField(fieldPosition_).getName());
	}	
	public void resetValues(final String qualifiedName_) {
		IFieldValue fieldValue = this.getFieldvalue(qualifiedName_);
		fieldValue.reset();
	}
	
	public void setValues(final int fieldPosition_, final Collection<String> values) {
		setValues(this.entityLogic.searchField(fieldPosition_).getName(), values);
	}	
	public void setValues(final String qualifiedName_, final Collection<String> values) {
		if (values == null) {
			return;
		}
				
		String qualifiedName = qualifiedName_;
		if (qualifiedName != null && qualifiedName.indexOf(FieldViewSet.FIELD_SEPARATOR) != -1) {
			if (!qualifiedName.substring(0, qualifiedName.indexOf(FieldViewSet.FIELD_SEPARATOR)).toUpperCase()
					.equals(this.contextName.toUpperCase())) {
				return;
			}
		}
		if (!this.isUserDefined() && qualifiedName_.indexOf(FieldViewSet.FIELD_SEPARATOR) == -1) {
			final StringBuilder nameQ = new StringBuilder(this.getContextName()).append(FieldViewSet.FIELD_SEPARATOR);
			nameQ.append(qualifiedName_);
			qualifiedName = nameQ.toString();
		}
		IFieldView fieldView = this.getFieldView(qualifiedName);
		if (fieldView == null) {
			fieldView = this.getEntityDef() != null ? new FieldView(this.getEntityDef().searchByName(qualifiedName_)) : new FieldView("");
			fieldView.setQualifiedContextName(qualifiedName);
			this.fieldViews.add(fieldView);
		}
		
		Collection<String> _values = new ArrayList<String>();
		Iterator<String> iteValues = values.iterator();
		while (iteValues.hasNext()){
			String val = iteValues.next();
			_values.add(val.replaceFirst(qualifiedName.concat("="), ""));
		}
		
		IFieldValue fieldValue = this.getFieldvalue(qualifiedName);		
		fieldValue.setValues(_values);
		this.fieldViewsValues.put(qualifiedName, fieldValue);
	}
	
	public void setValue(final int fieldPosition, final Serializable value_) {		
		setValue(this.entityLogic.searchField(fieldPosition).getName(), value_);
	}	
	public void setValue(final String qualifiedName_, final Serializable value_) {
		if (value_ != null) {
			final Collection<String> values = new ArrayList<String>();
			values.add(value_.toString());
			String qName_ = getContextName() !=null && !qualifiedName_.contains(getContextName())?getContextName().concat(PCMConstants.POINT).concat(qualifiedName_):qualifiedName_;
			this.setValues(qName_, values);
		} else {
			setNull(qualifiedName_);
		}
	}
	
	public void setNull(final int fieldPosition) {
		setNull(this.entityLogic.searchField(fieldPosition).getName());
	}	
	private void setNull(final String qualifiedName_) {
		
		FieldValue nullValue = new FieldValue();
		nullValue.setValue(null);
		String qName_ = getContextName() !=null && !qualifiedName_.contains(getContextName())?getContextName().concat(PCMConstants.POINT).concat(qualifiedName_):qualifiedName_;
		this.fieldViewsValues.put(qName_, nullValue);
	}

	protected void toXML(final StringBuilder sbXML) {
		if (sbXML == null) {
			return;
		}
	}

	public String toXML() {
		return this.toXML(false);
	}

	public String toXML(final boolean esLista) {
		final StringBuilder sbXML = new StringBuilder();
		if (esLista) {
			XmlUtils.openXmlNode(sbXML, this.getNodeName());
		}
		this.toXML(sbXML);
		if (esLista) {
			XmlUtils.closeXmlNode(sbXML, this.getNodeName());
		}
		return sbXML.toString();
	}

}
