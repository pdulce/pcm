package pcm.context.logicmodel.persistence;

import pcm.common.PCMConstants;
import pcm.context.logicmodel.definitions.IFieldLogic;

/***
 * Métodos que dependen del gestor de BBDD: - getEntityFilter - getDateOfRightSqlExpression
 * 
 * @author 99GU3997
 */
public class OracleDAOSQLImpl extends AnsiSQLAbstractDAOImpl {

	private static final String QUERY_WITH_PAGINATION = "SELECT v2.* FROM ( SELECT v1.*, ROWNUM fila FROM (SELECT COUNT(*) OVER () TOTALREG, #FIELDGOT# #TABLES# #CLAUSULES_WHERE# ORDER BY #ORDER_FIELD# #ORDER_DIRECTION#) v1 WHERE ROWNUM <= ?) v2 WHERE fila > ? ",
			QUERY_WITHOUT_PAGINATION = "SELECT #FIELDGOT# #TABLES# #CLAUSULES_WHERE#";

	private static final String TRUNC_ = "TRUNC(", TO_DATE_ = " TO_DATE ('", TO_DATE_FORMAT_ARG = "', 'yyyy-mm-dd') ";

	public static final String NEXTVAL_ = ".NEXTVAL";

	@Override
	protected String getQueryPagination(final int offset_) {
		return offset_ != -1 ? OracleDAOSQLImpl.QUERY_WITH_PAGINATION : OracleDAOSQLImpl.QUERY_WITHOUT_PAGINATION;
	}

	@Override
	public boolean hasDuplicatedCriteriaInEmbbededCounterSQL() {
		return false;
	}

	@Override
	public boolean hasCounterSQLEmbbeded() {
		return true;
	}

	@Override
	public boolean isUpperLimitBefore() {
		return true;
	}

	/**
	 * returns: "SEQ_NAME.NEXTVAL"
	 */
	@Override
	public String getSequenceExpr(final IFieldLogic field) {
		return field.getName().toUpperCase();
	}

	@Override
	public String getSequenceValueExpr(final IFieldLogic field) {
		return new StringBuilder(field.getSequence()).append(OracleDAOSQLImpl.NEXTVAL_).toString();
	}

	@Override
	protected String getInitOfLeftExpr() {
		return OracleDAOSQLImpl.TRUNC_;
	}

	@Override
	protected String getEndOfLeftExpr() {
		return PCMConstants.END_PARENTHESIS;
	}

	@Override
	protected String getRightDateValue(final String newDateUnformatted) {
		final StringBuilder valuesWithOr = new StringBuilder(OracleDAOSQLImpl.TO_DATE_);
		valuesWithOr.append(newDateUnformatted).append(OracleDAOSQLImpl.TO_DATE_FORMAT_ARG);
		return valuesWithOr.toString();
	}

	@Override
	protected String getRightPartPreffixForString() {
		return "";
	}

	@Override
	protected String getAsUtf8CompareForString() {
		return "";
	}

}
