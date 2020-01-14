/**
 * 
 */
package pcm.context.logicmodel.persistence;

import java.io.File;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import pcm.common.InternalErrorsConstants;
import pcm.common.PCMConstants;
import pcm.common.comparator.ComparatorFieldViews;
import pcm.common.exceptions.DatabaseException;
import pcm.common.utils.CommonUtils;
import pcm.comunication.dispatcher.ContextApp;
import pcm.context.data.IFieldValue;
import pcm.context.logicmodel.definitions.IFieldLogic;
import pcm.context.logicmodel.definitions.ILogicTypes;
import pcm.context.viewmodel.IViewMetamodel;
import pcm.context.viewmodel.components.IViewComponent;
import pcm.context.viewmodel.definitions.FieldViewSet;
import pcm.context.viewmodel.definitions.IFieldView;
import pcm.context.viewmodel.definitions.IRank;

/**
 * @author 99GU3997
 */
public abstract class AbstractDAOImpl implements IDAOImpl {

	protected boolean auditActivated = false;

	protected ContextApp ctx;

	protected Properties auditFieldSet;

	@Override
	public void setContext(final ContextApp ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean isAuditActivated() {
		return this.auditActivated;
	}

	@Override
	public void setAuditFieldset(final Map<String, String> auditFieldSet_) {
		this.auditFieldSet = new Properties();
		if (auditFieldSet_ != null && !auditFieldSet_.isEmpty() && !auditFieldSet_.values().contains(null)) {
			this.auditFieldSet.putAll(auditFieldSet_);
			this.auditActivated = true;
		}
	}

	@Override
	public Properties getAuditFieldset() {
		return this.auditFieldSet;
	}

	public ContextApp getContextApp() {
		return this.ctx;
	}

	protected Map<String, Integer> getEntityFilter(final FieldViewSet fieldViewSet,
			final Collection<Map<String, Serializable>> sqlParameters_out, final boolean fecBajaActivated, final Properties auditSet) {

		final String alias = SQLUtils.getAlias(fieldViewSet.getEntityDef().getName());
		final StringBuilder sqlWhereOfEntity = new StringBuilder();

		List<IFieldView> fieldCollection = new ArrayList<IFieldView>();
		fieldCollection.addAll(fieldViewSet.getFieldViews());
		Collections.sort(fieldCollection, new ComparatorFieldViews());

		int nArgs = 0;
		for (final IFieldView fieldView : fieldCollection) {
			if (fieldViewSet.getFieldvalue(fieldView.getQualifiedContextName()).isNull()) {
				continue;
			}
			StringBuilder leftPartOfSqlExpression = new StringBuilder(), rightPartOfSqlExpression = new StringBuilder(), sqlExpression = new StringBuilder();			
			if (fieldView.getEntityField().getAbstractField().getType().equals(ILogicTypes.STRING)) {
				leftPartOfSqlExpression.append(getLeftPartPreffixForString(fieldViewSet.getEntityDef().getName(), fieldView
						.getEntityField().getName()));
			} else {
				leftPartOfSqlExpression.append(SQLUtils.getAlias(fieldViewSet.getEntityDef().getName()));
				leftPartOfSqlExpression.append(PCMConstants.POINT).append(fieldView.getEntityField().getName());
			}
			final Collection<String> values = fieldViewSet.getFieldvalue(fieldView.getQualifiedContextName()).getValues();
			if (fieldView.isRankField() || fieldView.getEntityField().getAbstractField().isDate()) {
				if (fieldView.getEntityField().getAbstractField().isDate()) {
					String operator = "";
					if (fieldView.isRankField()) {
						operator = fieldView.getRankField().getRelationalOpe();
						if (fieldView.getEntityField().getAbstractField().isTimestamp() && operator.equals(IRank.MAYOR_EQUALS_OPE)) {
							operator = IRank.MAYOR_OPE;
						}
					} else {
						operator = IRank.EQUALS_OPE;
					}
					Serializable fecha = values.iterator().next();
					String dateFormatted = "";
					if (fecha instanceof Date){
						dateFormatted = CommonUtils.myDateFormatter.format((Date) fecha);
					}else if (fecha instanceof Timestamp){
						dateFormatted = CommonUtils.myDateFormatter.format((Timestamp) fecha);
					}else if (fecha instanceof String){
						dateFormatted = (String) fecha;
					}
					rightPartOfSqlExpression.append(this.getDateOfRightSqlExpression(dateFormatted, operator));
				} else if (fieldView.getEntityField().getAbstractField().isNumeric()) {
					final String valueFormattted = values.iterator().next().toString();
					rightPartOfSqlExpression.append(PCMConstants.STRING_SPACE).append(
							SQLUtils.translateReversedOperator(fieldView.getRankField().getRelationalOpe()));
					rightPartOfSqlExpression.append(valueFormattted.replace(PCMConstants.COMMA, PCMConstants.POINT));
				}
				sqlWhereOfEntity.append(sqlWhereOfEntity.toString().equals(PCMConstants.EMPTY_) ? PCMConstants.EMPTY_ : IDAOImpl.AND_);
				sqlExpression.append(this.getInitOfLeftExpr()).append(leftPartOfSqlExpression).append(this.getEndOfLeftExpr())
						.append(rightPartOfSqlExpression);
				sqlWhereOfEntity.append(sqlExpression);
				continue;
			}
			
			sqlWhereOfEntity.append(sqlWhereOfEntity.toString().equals(PCMConstants.EMPTY_) ? PCMConstants.EMPTY_ : IDAOImpl.AND_);
			sqlWhereOfEntity.append(PCMConstants.STRING_SPACE).append(PCMConstants.BEGIN_PARENTHESIS);
			int counterOfValues = 0;
			final Iterator<String> ite = values.iterator();
			while (ite.hasNext()) {
				counterOfValues++;
				final String value_ = ite.next();
				if (counterOfValues > 1) {
					sqlWhereOfEntity.append(IDAOImpl.OR_);
					leftPartOfSqlExpression = new StringBuilder();
					if (fieldView.getEntityField().getAbstractField().getType().equals(ILogicTypes.STRING)) {
						leftPartOfSqlExpression.append(getLeftPartPreffixForString(fieldViewSet.getEntityDef().getName(), fieldView
								.getEntityField().getName()));
					} else {
						leftPartOfSqlExpression.append(SQLUtils.getAlias(fieldViewSet.getEntityDef().getName()));
						leftPartOfSqlExpression.append(PCMConstants.POINT).append(fieldView.getEntityField().getName());
					}
					rightPartOfSqlExpression = new StringBuilder();
					sqlExpression = new StringBuilder();
				}
				if (fieldView.getEntityField().getAbstractField().getType().equals(ILogicTypes.STRING)) {
					rightPartOfSqlExpression.append(fieldView.getEntityField().getAbstractField().getMaxLength() < MAX_FOR_CODES ? "="
							: IDAOImpl.LIKE_);
					/** si se trata de un campo de tipo código no usamos 'LIKE', sino '=' ***/
					rightPartOfSqlExpression.append(getAsUtf8CompareForString());
				} else if (fieldView.getEntityField().getAbstractField().isDecimal()) {
					rightPartOfSqlExpression.append(IDAOImpl.SQL_MINOR_EQUALS_OPERATOR/* " <= " */);
				} else {
					rightPartOfSqlExpression.append(PCMConstants.EQUALS);
				}
				rightPartOfSqlExpression.append(IDAOImpl.ARG);
				if (fieldView.getEntityField().getAbstractField().getType().equals(ILogicTypes.STRING)) {
					rightPartOfSqlExpression.append(getRightPartPreffixForString());
				}
				final Map<String, Serializable> valueOfWhere = new HashMap<String, Serializable>();
				valueOfWhere.put(
						fieldView.getEntityField().getAbstractField().getType().concat(",")
								.concat(String.valueOf(fieldView.getEntityField().getAbstractField().getMaxLength())), value_);
				sqlParameters_out.add(valueOfWhere);
				sqlExpression.append(leftPartOfSqlExpression).append(rightPartOfSqlExpression);
				sqlWhereOfEntity.append(sqlExpression);
				nArgs++;
			}
			if (!values.isEmpty()) {
				sqlWhereOfEntity.append(PCMConstants.END_PARENTHESIS);
			}
		}// for de campos
		if (this.needFecBajaFilter(fieldViewSet, fecBajaActivated, auditSet)) {
			sqlWhereOfEntity.append(!sqlWhereOfEntity.toString().equals(PCMConstants.EMPTY_) ? IDAOImpl.AND_ : PCMConstants.EMPTY_);
			sqlWhereOfEntity.append(alias).append(PCMConstants.POINT).append(auditSet.getProperty(IViewMetamodel.FEC_BAJA))
					.append(IDAOImpl.IS_NULL_);
		}
		final Map<String, Integer> filterWithNArgs = new HashMap<String, Integer>();
		filterWithNArgs.put(sqlWhereOfEntity.toString(), Integer.valueOf(nArgs));
		return filterWithNArgs;
	}

	protected String getDateOfRightSqlExpression(final String dateFormatted, final String operator) {
		try {
			final StringBuilder valuesWithOr = new StringBuilder(), newDateUnformatted = new StringBuilder();
			final Calendar cal = Calendar.getInstance();
			Date fecha = CommonUtils.myDateFormatter.parse(dateFormatted);
			cal.setTime(fecha);

			final StringBuilder day = new StringBuilder(cal.get(Calendar.DAY_OF_MONTH) < 10 ? IViewComponent.ZERO : PCMConstants.EMPTY_);
			day.append(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
			final StringBuilder month = new StringBuilder((cal.get(Calendar.MONTH) + 1) < 10 ? IViewComponent.ZERO : PCMConstants.EMPTY_);
			month.append(String.valueOf((cal.get(Calendar.MONTH) + 1)));
			newDateUnformatted.append(String.valueOf(cal.get(Calendar.YEAR))).append(PCMConstants.SIMPLE_SEPARATOR);
			newDateUnformatted.append(month).append(PCMConstants.SIMPLE_SEPARATOR).append(day);
			return valuesWithOr.append(PCMConstants.STRING_SPACE).append(SQLUtils.translateReversedOperator(operator))
					.append(this.getRightDateValue(newDateUnformatted.toString())).toString();
		}
		catch (final ParseException parseExc) {
			return null;
		}
	}

	protected final int deleteByPk(final FieldViewSet fieldViewSet, final DAOConnection conn) throws DatabaseException {
		String whereClausule = SQLUtils.getWhereClausuleFKorPK(fieldViewSet);
		int numberOfWhereParams = SQLUtils.countParams(whereClausule, "=");
		PreparedStatement pstmt = null;
		int res = 0;
		try {
			pstmt = conn.prepareStatement(SQLUtils.replaceDeleteSql(ELIMINAR_ENTIDAD, fieldViewSet.getEntityDef().getName().toUpperCase(),
					whereClausule));
			int contador = 1;
			Iterator<IFieldLogic> iteratorCamposClave = fieldViewSet.getEntityDef().getFieldKey().getPkFieldSet().iterator();
			while (iteratorCamposClave.hasNext() && contador <= numberOfWhereParams) {
				final IFieldLogic fieldEntityDefPK = iteratorCamposClave.next();
				if (fieldViewSet.getFieldvalue(fieldEntityDefPK).isNull()
						|| fieldEntityDefPK.getAbstractField().getDefaultValueObject() != null) {
					continue;
				}
				final Serializable value = !fieldViewSet.getFieldvalue(fieldEntityDefPK).isNull() ? fieldViewSet.getFieldvalue(fieldEntityDefPK)
						.getValue() : fieldEntityDefPK.getAbstractField().getDefaultValueObject();
				pstmt.setObject(contador++, value);
			}
			final Iterator<IFieldLogic> iteratorCampos = fieldViewSet.getEntityDef().getFieldSet().values().iterator();
			while (iteratorCampos.hasNext() && contador <= numberOfWhereParams) {
				final IFieldLogic fieldEntityDef = iteratorCampos.next();
				if (fieldEntityDef.getParentFieldEntities() == null || fieldEntityDef.getParentFieldEntities().isEmpty()) {
					continue;
				}
				if (fieldViewSet.getFieldvalue(fieldEntityDef).isNull()) {
					continue;
				}
				final Serializable value = fieldViewSet.getFieldvalue(fieldEntityDef).getValue();
				if (!fieldEntityDef.getAbstractField().isBlob()) {
					pstmt.setObject(contador++, value);
				} else if (fieldEntityDef.getAbstractField().isBlob()) {
					if (PCMConstants.EMPTY_.equals(value)) {
						// nothing
					} else {
						final byte[] binary_ = CommonUtils.getPrimitiveByteArrayFromUploadedFile(new File(value.toString()));
						if (binary_ != null) {
							pstmt.setBytes(contador++, binary_);
						}
					}
				}
			}
			res = pstmt.executeUpdate();
		}
		catch (final Throwable exc) {
			res = -1;
			throw new DatabaseException(InternalErrorsConstants.BBDD_DELETE_EXCEPTION, exc);
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			}
			catch (final Throwable ezc) {
				// AnsiSQLAbstractDAOImpl.log.error(InternalErrorsConstants.BBDD_FREE_EXCEPTION,
				// ezc);
				throw new DatabaseException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, ezc);
			}
		}
		return res;
	}

	protected String getRightDateValue(final String newDateUnformatted) {
		return new StringBuilder(PCMConstants.SIMPLE_COMILLA).append(newDateUnformatted).append(PCMConstants.SIMPLE_COMILLA).toString();
	}

	protected boolean needFecBajaFilter(final FieldViewSet fieldViewSet, final boolean fecBajaActivated, final Properties auditSet) {
		return fecBajaActivated && fieldViewSet.getEntityDef().getFieldSet().get(auditSet.getProperty(IViewMetamodel.FEC_BAJA)) != null;
	}

	protected Timestamp getTimestamp(final ResultSet resultSet, final String alias) throws SQLException {
		return resultSet.getTimestamp(alias);
	}

	protected String getParameterArgument(final IFieldLogic field, final IFieldValue fieldV) {
		return ARG;
	}

	protected String getInitOfLeftExpr() {
		return PCMConstants.EMPTY_;
	}

	protected String getEndOfLeftExpr() {
		return PCMConstants.EMPTY_;
	}

	protected String getSpecialCharsConversion(final String cadena) {
		return cadena;
	}

	@Override
	public abstract boolean hasCounterSQLEmbbeded();

	@Override
	public abstract boolean isUpperLimitBefore();

	public abstract boolean hasDuplicatedCriteriaInEmbbededCounterSQL();

	protected abstract String getQueryPagination(int offset_);

	protected abstract String getLeftPartPreffixForString(final String entityName, final String fieldName);

	protected abstract String getRightPartPreffixForString();

	protected abstract String getAsUtf8CompareForString();
}
