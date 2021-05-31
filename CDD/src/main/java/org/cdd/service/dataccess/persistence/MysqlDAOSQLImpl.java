package org.cdd.service.dataccess.persistence;

import org.cdd.common.PCMConstants;
import org.cdd.service.dataccess.definitions.IFieldLogic;

public class MysqlDAOSQLImpl extends AnsiSQLAbstractDAOImpl {

	private static final String QUERY_WITH_PAGINATION = "SELECT #FIELDGOT# #TABLES# #CLAUSULES_WHERE# ORDER BY #ORDER_FIELD# #ORDER_DIRECTION# LIMIT ?,? ",
			QUERY_WITHOUT_PAGINATION = "SELECT #FIELDGOT# #TABLES# #CLAUSULES_WHERE#";

	@Override
	protected String getQueryPagination(final int offset_) {
		return offset_ != -1 ? MysqlDAOSQLImpl.QUERY_WITH_PAGINATION : MysqlDAOSQLImpl.QUERY_WITHOUT_PAGINATION;
	}

	@Override
	public boolean hasDuplicatedCriteriaInEmbbededCounterSQL() {
		return true;
	}

	@Override
	public boolean hasCounterSQLEmbbeded() {
		return true;
	}

	@Override
	public boolean isUpperLimitBefore() {
		return false;
	}

	@Override
	public String getSequenceExpr(final IFieldLogic field) {
		return PCMConstants.EMPTY_;
	}

	@Override
	public String getSequenceValueExpr(final IFieldLogic field) {
		return PCMConstants.EMPTY_;
	}

	@Override
	protected String getLeftPartPreffixForString(String entityName, String fieldName) {
		StringBuilder leftPartOfSqlExpression = new StringBuilder(SQLUtils.LCASE_FUNCT);
		leftPartOfSqlExpression.append(PCMConstants.BEGIN_PARENTHESIS).append("TRIM(");
		leftPartOfSqlExpression.append(SQLUtils.getAlias(entityName));
		leftPartOfSqlExpression.append(PCMConstants.POINT).append(fieldName).append(PCMConstants.END_PARENTHESIS)
				.append(PCMConstants.END_PARENTHESIS);
		return leftPartOfSqlExpression.toString();
	}

	@Override
	protected String getRightPartPreffixForString() {		
		return SQLUtils.COLLATE;
	}

	@Override
	protected String getAsUtf8CompareForString() {
		return " _utf8 ";
	}

}
