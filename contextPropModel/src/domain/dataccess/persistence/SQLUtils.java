package domain.dataccess.persistence;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
import domain.common.utils.CommonUtils;
import domain.component.IViewComponent;
import domain.component.definitions.FieldViewSet;
import domain.component.definitions.FieldViewSetCollection;
import domain.component.definitions.IFieldView;
import domain.component.definitions.IRank;
import domain.dataccess.definitions.IEntityLogic;
import domain.dataccess.definitions.IFieldLogic;


public class SQLUtils {

	public static final String LCASE_FUNCT = "LCASE", COLLATE = " COLLATE utf8_general_ci";

	protected static String getSQLAggregated(final IFieldView fieldView) {
		final String parent_ = fieldView.getEntityField().getEntityDef().getName();
		final FieldViewSet child = new FieldViewSet(fieldView.getEntityField().getEntityDef());
		final Collection<Map<String, Object>> paramsOfQuery = new ArrayList<Map<String, Object>>();
		final Iterator<IFieldLogic> fieldsOfChildIterator = child.getEntityDef().getFieldSet().values().iterator();
		while (fieldsOfChildIterator.hasNext()) {
			final IFieldLogic fieldOfChild = fieldsOfChildIterator.next();
			if (fieldOfChild.getParentFieldEntity(parent_) != null) {
				final IFieldLogic pkParent = fieldOfChild.getParentFieldEntity(parent_);
				boolean dimensionAdded = false;
				final Iterator<Map<String, Object>> iteParams = paramsOfQuery.iterator();
				while (iteParams.hasNext()) {
					final Map<String, Object> mapaPK = iteParams.next();
					int totalDimensions = fieldView.getAggregateField().getDimension();
					for (int i = 1; i < totalDimensions; i++) {
						final int nextDimension = SQLUtils.nextDimensionInParams(pkParent.getName(), mapaPK);
						if (nextDimension > 1) {
							dimensionAdded = true;
							final StringBuilder pkP_ = new StringBuilder(pkParent.getName());
							pkP_.append(String.valueOf(nextDimension));
							mapaPK.put(pkP_.toString(), fieldOfChild.getName());
						}
					}
				}
				if (!dimensionAdded) {
					final Map<String, Object> mapaPK = new HashMap<String, Object>();
					final StringBuilder pkP_ = new StringBuilder(pkParent.getName());
					pkP_.append(IViewComponent.ONE);
					mapaPK.put(pkP_.toString(), fieldOfChild.getName());
					paramsOfQuery.add(mapaPK);
				}
			}
		}
		final StringBuilder aggregateSQL = new StringBuilder(IDAOImpl.SELECT_);
		aggregateSQL.append(fieldView.getAggregateField().getFormulaSQL()).append(PCMConstants.END_PARENTHESIS)
				.append(fieldView.getEntityField().getName());
		aggregateSQL.append(PCMConstants.BEGIN_PARENTHESIS).append(IDAOImpl.FROM_).append(child.getEntityDef().getName())
				.append(IDAOImpl.ENTITY_ALIAS);
		aggregateSQL.append(IDAOImpl.WHERE_);
		final Iterator<Map<String, Object>> paramsIte = paramsOfQuery.iterator();
		while (paramsIte.hasNext()) {
			final Map<String, Object> param = paramsIte.next();
			final String claveFK = SQLUtils.getFieldFKOfDimension(param, fieldView.getAggregateField().getDimension());
			if (aggregateSQL.toString().indexOf(IDAOImpl.C_PREFFIX) != -1) {
				aggregateSQL.append(IDAOImpl.AND_);
			}
			aggregateSQL.append(IDAOImpl.C_PREFFIX).append(param.get(claveFK)).append(PCMConstants.EQUALS)
					.append(SQLUtils.getAlias(parent_));
			aggregateSQL.append(PCMConstants.POINT).append(claveFK.substring(0, claveFK.length() - 1));
		}
		return aggregateSQL.toString();
	}

	protected static final Date getDateObjectForPreparedStatement(Serializable value) {
		try {
			return CommonUtils.myDateFormatter.parse(value.toString());
		}catch (ParseException e) {
			return null;
		}
	}

	protected static String getJoin(final Collection<FieldViewSet> entidades, final String parentAlias, final String childAlias, final IFieldLogic masterEntityField_) {
		int contador = 0;
		final Iterator<FieldViewSet> iteEntidadesC = entidades.iterator();
		while (iteEntidadesC.hasNext()) {
			iteEntidadesC.next();
			contador++;
		}
		if (contador < 2) {
			return PCMConstants.EMPTY_;
		}

		/** * CADENA DE SQL JOIN ** */
		final StringBuilder sqlWhereOfEntity = new StringBuilder();
		final Iterator<FieldViewSet> iteEntidades = entidades.iterator();
		while (iteEntidades.hasNext()) {
			final FieldViewSet entidad = iteEntidades.next();
			if (entidad.getEntityDef().getParentEntities() != null && !entidad.getEntityDef().getParentEntities().isEmpty()) {
				/**
				 * ** MIRAMOS SI ES UNA ENTIDAD HIJA, PARA METER EL JOIN CONTRA SU ENTIDAD PADRE **
				 */
				final Iterator<Map.Entry<String, IFieldLogic>> iteratorDetailIFields = entidad.getEntityDef().getFieldSet().entrySet()
						.iterator();
				while (iteratorDetailIFields.hasNext()) {
					final Map.Entry<String, IFieldLogic> entryOfEntity = iteratorDetailIFields.next();
					final IFieldLogic detailField = entryOfEntity.getValue();
					
					if (detailField.getParentFieldEntities() != null) {
						final Iterator<IFieldLogic> iteFieldParents = detailField.getParentFieldEntities().iterator();
						while (iteFieldParents.hasNext()) {
							final IFieldLogic parentFieldPK = iteFieldParents.next();
							
							if (SQLUtils.isContainedInSet(parentFieldPK.getEntityDef(), entidades)) {
								
								if (masterEntityField_ != null && 
										!(masterEntityField_.getEntityDef().getName().equals(detailField.getEntityDef().getName()) &&
												masterEntityField_.getName().equals(detailField.getName()))){
									continue;
								}
								final String masterAlias = parentAlias != null ? parentAlias : SQLUtils.getAlias(parentFieldPK
										.getEntityDef().getName());
								final String detailAlias = childAlias != null ? childAlias : SQLUtils.getAlias(entidad.getEntityDef()
										.getName());
								final String pkFieldName = new StringBuilder(masterAlias).append(PCMConstants.POINT)
										.append(parentFieldPK.getName()).toString();
								final String cadenaRepetida = new StringBuilder(pkFieldName).append(PCMConstants.EQUALS)
										.append(detailAlias).append(PCMConstants.POINT).toString();
								if (sqlWhereOfEntity.toString().indexOf(cadenaRepetida) == -1) {
									if (!PCMConstants.EMPTY_.equals(sqlWhereOfEntity.toString())) {
										sqlWhereOfEntity.append(IDAOImpl.AND_);
									}
									sqlWhereOfEntity.append(pkFieldName).append(PCMConstants.EQUALS).append(detailAlias)
											.append(PCMConstants.POINT).append(detailField.getName());
								}
							}
						}// for
					}
				}// for de campos
			}// en-if de una entidad hija (o detalle)
		}
		return sqlWhereOfEntity.toString();
	}

	protected static String getAlias(final String entityName) {
		int numero = 0;
		try {
			final byte[] byteArr = entityName.getBytes(IViewComponent.UTF_8);
			for (final byte element : byteArr) {
				numero += (char) element;
			}
			return new StringBuilder(entityName.substring(0, 1)).append(String.valueOf(numero % 100)).toString();
		}
		catch (final UnsupportedEncodingException exc) {
			return IDAOImpl.ALIAS_;
		}
	}

	protected static boolean isChildEntity(final IEntityLogic entity, final Collection<FieldViewSet> fieldViewSets) {
		if (entity.getParentEntities() == null || entity.getParentEntities().isEmpty()) {
			return false;
		}
		final Iterator<IEntityLogic> entidadesPadre = entity.getParentEntities().iterator();
		while (entidadesPadre.hasNext()) {
			if (SQLUtils.isContainedInSet(entidadesPadre.next(), fieldViewSets)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isContainedInSet(final IEntityLogic entity, final Collection<FieldViewSet> entidades) {
		final Iterator<FieldViewSet> iteEntidades = entidades.iterator();
		while (iteEntidades.hasNext()) {
			if (iteEntidades.next().getEntityDef().getName().equals(entity.getName())) {
				return true;
			}
		}
		return false;
	}

	protected static String translateReversedOperator(final String ope) {
		if (IRank.MAYOR_EQUALS_OPE.equals(ope)) {
			return IDAOImpl.SQL_MINOR_EQUALS_OPERATOR;
		} else if (IRank.MAYOR_OPE.equals(ope)) {
			return IDAOImpl.SQL_MINOR_OPERATOR;
		} else if (IRank.MINOR_EQUALS_OPE.equals(ope)) {
			return IDAOImpl.SQL_MAYOR_EQUALS_OPERATOR;
		} else if (IRank.MINOR_OPE.equals(ope)) {
			return IDAOImpl.SQL_MAYOR_OPERATOR;
		} else {
			return IDAOImpl.SQL_EQUALS_OPERATOR;
		}
	}

	/**
	 * Obtiene la entidad (fieldviewset) que difiere entre las entidades 'padre' que actoan como
	 * criterio en el 'Formulario', y la entidad 'hija' que se emplea en el 'PaginationGrid'.
	 * 
	 * @param entidades_Criteria
	 * @param prototypeResult
	 * @return
	 */
	protected static FieldViewSet getDetailEntity(final FieldViewSetCollection prototypeResult) {
		if (!prototypeResult.getFieldViewSets().isEmpty()) {
			return prototypeResult.getFieldViewSets().get(prototypeResult.getFieldViewSets().size() - 1);
		}
		return null;
	}

	protected static int nextDimensionInParams(final String pkName, final Map<String, Object> mapaPK) {
		int max = 0;
		final Iterator<String> iteClaves = mapaPK.keySet().iterator();
		while (iteClaves.hasNext()) {
			final String clave = iteClaves.next();
			if (clave.substring(0, clave.length() - 1).equals(pkName)) {
				final int relative = Integer.parseInt(clave.substring(clave.length() - 1, clave.length()));
				if (relative > max) {
					max = relative;
				}
			}
		}
		return max + 1;
	}

	/** ** CONSULTAS SQL CON JOIN SI HAY MAS DE UNA ENTIDAD EN JUEGO ** */

	protected static String getWhereClausuleFKorPK(final FieldViewSet fieldViewSet) {

		int numberOfPKFields = fieldViewSet.getEntityDef().getFieldKey().getPkFieldSet().size();
		Iterator<IFieldLogic> iteratorCampos = fieldViewSet.getEntityDef().getFieldKey().getPkFieldSet().iterator();
		while (iteratorCampos.hasNext()) {
			IFieldLogic campo = iteratorCampos.next();
			if (campo.belongsPK() && numberOfPKFields == 1) {
				Serializable value = fieldViewSet.getValue(fieldViewSet.getEntityDef().searchField(campo.getMappingTo()).getName());
				if (value != null) {
					return campo.getName().toUpperCase().concat(PCMConstants.EQUALS).concat(IDAOImpl.ARG);
				}
			}
		}

		List<String> whereArr = new ArrayList<String>();
		iteratorCampos = fieldViewSet.getEntityDef().getFieldSet().values().iterator();
		while (iteratorCampos.hasNext()) {
			IFieldLogic campo = iteratorCampos.next();
			if ((campo.getParentFieldEntities() != null && !campo.getParentFieldEntities().isEmpty())) {
				Serializable value = fieldViewSet.getValue(fieldViewSet.getEntityDef().searchField(campo.getMappingTo()).getName());
				if (value != null) {
					whereArr.add(campo.getName().toUpperCase().concat(PCMConstants.EQUALS).concat(IDAOImpl.ARG));
				}
			}
		}

		int consignados = whereArr.size();
		final StringBuilder where = new StringBuilder();
		for (int i = 0; i < consignados; i++) {
			where.append(whereArr.get(i));
			if ((i + 1) < consignados) {
				where.append(IDAOImpl.AND_);
			}
		}

		return where.toString();
	}

	protected static String replaceDeleteSql(final String sqlPattern_, final String table, final String fieldKeys) {
		final String sqlPattern = sqlPattern_.replaceAll(IDAOImpl.TABLE_, table);
		return sqlPattern.replaceAll(IDAOImpl.FIELDKEYS_,
				fieldKeys != null && !PCMConstants.EMPTY_.equals(fieldKeys.trim()) ? new StringBuilder(IDAOImpl.WHERE_).append(fieldKeys)
						.toString() : PCMConstants.EMPTY_);
	}

	protected static String replaceUpdateSql(final String sqlPattern_, final String table, final String fieldSetValues,
			final String fieldKeys) {
		final String sqlPattern = sqlPattern_.replaceAll(IDAOImpl.TABLE_, table).replaceAll(IDAOImpl.FIELDSETVALUES_, fieldSetValues);
		return sqlPattern.replaceAll(IDAOImpl.FIELDKEYS_,
				fieldKeys != null && !PCMConstants.EMPTY_.equals(fieldKeys.trim()) ? new StringBuilder(IDAOImpl.WHERE_).append(fieldKeys)
						.toString() : PCMConstants.EMPTY_);
	}

	protected static String replaceInsertSql(final String sqlPattern_, final String table, final String fieldSet, final String values) {
		final String sqlPattern = sqlPattern_.replaceAll(IDAOImpl.TABLE_, table).replaceAll(IDAOImpl.FIELDSET_, fieldSet);
		return sqlPattern.replaceAll(IDAOImpl.VALUES_, values);
	}

	protected static String replaceSelectByPkSql(final String sqlPattern_, final String table, final String fieldKeys) {
		final String sqlPattern = sqlPattern_.replaceAll(IDAOImpl.TABLE_, table);
		return sqlPattern.replaceAll(IDAOImpl.FIELDKEYS_,
				fieldKeys != null && !PCMConstants.EMPTY_.equals(fieldKeys.trim()) ? new StringBuilder(IDAOImpl.WHERE_).append(fieldKeys)
						.toString() : PCMConstants.EMPTY_);
	}

	protected static String replaceSelectByEntitySql(final String sqlPattern_, final String table, final String fieldSet) {
		final String sqlPattern = sqlPattern_.replaceAll(IDAOImpl.TABLE_, new StringBuilder(table).append(PCMConstants.STRING_SPACE)
				.append(SQLUtils.getAlias(table)).toString());
		return sqlPattern.replaceAll(IDAOImpl.FIELDSET_,
				fieldSet != null && !PCMConstants.EMPTY_.equals(fieldSet.trim()) ? new StringBuilder(IDAOImpl.WHERE_).append(fieldSet)
						.toString() : PCMConstants.EMPTY_);
	}

	protected static String replaceSQLWithPagination(final String sql, final String fieldGot, final String tablesFrom,
			final String clausulesWhereSql, final String[] orderFields_, final String orderDirec_) throws DatabaseException {
		return SQLUtils.replaceSQLMultiEntities(sql, fieldGot, tablesFrom, clausulesWhereSql, orderFields_, orderDirec_);
	}

	private static String filterDuplicated(final String tablesFrom_) {
		if (tablesFrom_ == null || tablesFrom_.equals(PCMConstants.EMPTY_)) {
			return PCMConstants.EMPTY_;
		}
		String tablesFrom = tablesFrom_;
		if (tablesFrom_.indexOf(IDAOImpl.FROM_) != -1) {
			tablesFrom = tablesFrom_.replaceAll(IDAOImpl.FROM_, PCMConstants.EMPTY_);
		}
		final StringBuilder strBuilder = new StringBuilder(IDAOImpl.FROM_);
		final Collection<String> tableCollection = new ArrayList<String>();
		final String[] splitter = tablesFrom.split(PCMConstants.COMMA);
		for (final String element : splitter) {
			final String table_ = element.trim();
			if (!tableCollection.contains(table_)) {
				tableCollection.add(element);
				if (strBuilder.toString().length() > IDAOImpl.FROM_.length()) {
					strBuilder.append(PCMConstants.COMMA).append(PCMConstants.STRING_SPACE).append(table_);
				} else {
					strBuilder.append(table_);
				}
			}
		}
		return strBuilder.toString();
	}

	/**
	 * Reemplaza esta query: SELECT #FIELDGOT# #TABLES# #CLAUSULES_WHERE#
	 */
	protected static String replaceSQLWithoutPagination(final String sql, final String fieldGot, final String tablesFrom,
			final String clausulesWhereSql) {
		String sqlPattern = sql.replaceAll(IDAOImpl.FIELDGOT_, fieldGot)
				.replaceAll(IDAOImpl.TABLES_, SQLUtils.filterDuplicated(tablesFrom));
		sqlPattern = sqlPattern.replaceAll(
				IDAOImpl.CLAUSULES_WHERE_,
				clausulesWhereSql != null && !PCMConstants.EMPTY_.equals(clausulesWhereSql) ? new StringBuilder(IDAOImpl.WHERE_).append(
						clausulesWhereSql).toString() : PCMConstants.EMPTY_);

		return sqlPattern;
	}

	protected static String replaceSQLMultiEntities(final String sqlPattern_, final String fieldGot, final String tablesFrom,
			final String clausulesWhereSql, final String[] orderFields_, final String orderDirec_) throws DatabaseException {
		String sqlPattern = sqlPattern_.replaceAll(IDAOImpl.FIELDGOT_, fieldGot).replaceAll(IDAOImpl.TABLES_,
				SQLUtils.filterDuplicated(tablesFrom));
		sqlPattern = sqlPattern.replaceAll(
				IDAOImpl.CLAUSULES_WHERE_,
				clausulesWhereSql != null && !PCMConstants.EMPTY_.equals(clausulesWhereSql) ? new StringBuilder(IDAOImpl.WHERE_).append(
						clausulesWhereSql).toString() : PCMConstants.EMPTY_);
		sqlPattern = sqlPattern.replaceAll(IDAOImpl.ORDER_DIRECTION_, orderDirec_);
		if (orderFields_.length != 0 && orderDirec_ != null) {
			final StringBuilder orderFieldapp_ = new StringBuilder();
			int orderFieldsCount = orderFields_.length;
			for (int o_ = 0; o_ < orderFieldsCount; o_++) {
				final String orderField_ = orderFields_[o_];
				if (orderField_ == null || "null".equals(orderField_)) {
					throw new DatabaseException("Error in the query string: orderField is null");
				}
				orderFieldapp_.append(orderField_);
				if (o_ + 1 < orderFields_.length) {
					orderFieldapp_.append(PCMConstants.COMMA);
				}
			}
			sqlPattern = sqlPattern.replaceAll(IDAOImpl.ORDER_FIELD_, orderFieldapp_.toString());
		}
		return sqlPattern;
	}

	protected static double getDouble(final String double_) {
		try {
			String double_p = double_.replaceAll(PCMConstants.REGEXP_POINT, PCMConstants.EMPTY_);
			if (double_p.indexOf(PCMConstants.CHAR_VERTICAL_SEP) != -1) {
				double_p = double_p.substring(0, double_p.indexOf(PCMConstants.CHAR_VERTICAL_SEP));
			}
			if (double_p.indexOf(PCMConstants.CHAR_COMMA) != -1) {
				return CommonUtils.numberFormatter.parse(double_p).doubleValue();
			}
			return Double.parseDouble(double_p);
		}
		catch (final ParseException parseExc) {
			return Double.NEGATIVE_INFINITY;
		}
	}

	protected static final String translateFiltered(final String pCadena) {
		String cadena = pCadena;
		if (cadena != null) {
			if (cadena.indexOf(PCMConstants.CHAR_VERTICAL_SEP) != -1) {
				cadena = cadena.substring(0, cadena.indexOf(PCMConstants.CHAR_VERTICAL_SEP));
			}
			if (PCMConstants.EMPTY_.equals(cadena)) {
				return null;
			}
			return cadena.replace(PCMConstants.CHAR_ASTERISC, PCMConstants.CHAR_PERCENT);
		}
		return null;
	}

	protected static Collection<FieldViewSet> getEntitiesNotLeftJoin(final Collection<FieldViewSet> ents_prototype,
			final IEntityLogic parent, final IEntityLogic detail) {
		final Collection<FieldViewSet> entsNoLeftJoin = new ArrayList<FieldViewSet>();
		final Iterator<FieldViewSet> iteProto = ents_prototype.iterator();
		while (iteProto.hasNext()) {
			final FieldViewSet entidadCand = iteProto.next();
			if (!entidadCand.getEntityDef().getName().equals(parent.getName())
					&& !entidadCand.getEntityDef().getName().equals(detail.getName())) {
				entsNoLeftJoin.add(entidadCand);
			}
		}
		return entsNoLeftJoin;
	}

	protected static String getFieldFKOfDimension(final Map<String, Object> fieldFKMap, final int dimension) {
		if (fieldFKMap.keySet().isEmpty()) {
			return null;
		}
		if (fieldFKMap.keySet().size() == 1 && !fieldFKMap.keySet().isEmpty()) {
			return fieldFKMap.keySet().iterator().next();
		}
		final String clave = PCMConstants.EMPTY_;
		final Iterator<String> clavesIte = fieldFKMap.keySet().iterator();
		while (clavesIte.hasNext()) {
			final String clave_ = clavesIte.next();
			if (clave_.endsWith(String.valueOf(dimension))) {
				return clave_;
			}
		}
		if (PCMConstants.EMPTY_.equals(clave)) {
			return fieldFKMap.keySet().iterator().next();
		}
		return clave;
	}

	public static Timestamp getTimestamp(final Serializable date__) {
		if (date__ == null) {
			return null;
		}
		try {
			return (Timestamp) date__;
		}
		catch (final ClassCastException castExc) {
			try {
				return new Timestamp(((java.sql.Date) date__).getTime());
			}
			catch (final ClassCastException castExc2) {
				try {
					String date_ = date__.toString();
					if (date_.indexOf(PCMConstants.CHAR_VERTICAL_SEP) != -1) {
						date_ = date_.substring(0, date_.indexOf(PCMConstants.CHAR_VERTICAL_SEP));
					}
					return new Timestamp(CommonUtils.myDateFormatter.parse(date_).getTime());
				}
				catch (final ParseException parseExc) {
					return null;
				}
			}
		}
	}

	protected static Map<String, Map<String, String>> searchAliasFields(Map<String, Map<String, String>> fieldAliases, String entityName, Collection<String> descartesDeAlias) {
		if (fieldAliases== null || fieldAliases.isEmpty()){
			return null;
		}
		Iterator<String> iteOfKeysInAliasMap = fieldAliases.keySet().iterator();
		while (iteOfKeysInAliasMap.hasNext()){
			String keyInAliasMap = iteOfKeysInAliasMap.next();
			String[] spliterAliasEntity = keyInAliasMap.split("\\|");
			if (entityName.equals(spliterAliasEntity[1]) && !descartesDeAlias.contains(spliterAliasEntity[0])){
				Map<String, Map<String, String>> mapEntry = new HashMap<String, Map<String, String>>();
				mapEntry.put(keyInAliasMap, fieldAliases.get(keyInAliasMap));
				return mapEntry;
			}
		}
		
		return null;
	}

	protected static int countParams(String clausule, String paramSimbol) {
		int apariciones = 0;
		String cadena = clausule;
		int index = -1;
		while ((index = cadena.indexOf(paramSimbol)) != -1) {
			apariciones++;
			cadena = cadena.substring(index + paramSimbol.length(), cadena.length());
		}
		return apariciones;
	}

	protected static boolean fieldForeignKey(final IFieldView fieldView) {
		return fieldView.getFieldAndEntityForThisOption() != null
				&& fieldView.getFieldAndEntityForThisOption().getEntityFromCharge() != null
				&& !fieldView.getFieldAndEntityForThisOption().getEntityFromCharge()
						.equals(fieldView.getEntityField().getEntityDef().getName());
	}

	protected static Collection<FieldViewSet> devolutionOfRestOfFViewSets(final FieldViewSet fieldViewSet,
			final Collection<FieldViewSet> colSets) {
		Collection<FieldViewSet> resultado = new ArrayList<FieldViewSet>();
		Iterator<FieldViewSet> iteCol = colSets.iterator();
		while (iteCol.hasNext()) {
			FieldViewSet candidato = iteCol.next();
			if (candidato.getEntityDef().getName().equals(fieldViewSet.getEntityDef().getName())) {
				continue;
			}
			resultado.add(candidato);
		}
		return resultado;
	}

}
