package cdd.domain.component.definitions;

import java.io.Serializable;
import java.util.StringTokenizer;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.domain.logicmodel.definitions.EntityLogic;
import cdd.domain.logicmodel.definitions.IFieldLogic;
import cdd.domain.logicmodel.factory.EntityLogicFactory;
import cdd.domain.logicmodel.persistence.IDAOImpl;


public class VirtualField implements IVirtualField, Serializable {

	private static final long serialVersionUID = 9393000033333L;

	private static final String[] operators = new String[] { IDAOImpl.SQL_EQUALS_OPERATOR, IDAOImpl.SQL_MAYOR_EQUALS_OPERATOR,
			IDAOImpl.SQL_MINOR_EQUALS_OPERATOR };

	private boolean dummy;

	private StringBuilder whereExpression = new StringBuilder(), fromExpression = new StringBuilder(), primitiveSQL = new StringBuilder();

	public VirtualField(final String whereExpression_, final String from_, final boolean dummy_) {
		this.dummy = dummy_;
		this.whereExpression.append(whereExpression_);
		this.fromExpression.append(from_);
	}

	public VirtualField(final String whereExpression_, final String from_) {
		this.whereExpression.append(whereExpression_);
		this.fromExpression.append(from_);
	}

	public String getFromExpression() {
		return this.fromExpression.toString();
	}

	public String getWhereExpression() {
		return this.whereExpression.toString();
	}

	/**
	 * desglosa expresiones con este formato [FUNCTION-SQL] [(] entidad:mappingTo[,] [)]
	 * [OPERADOR-SQL [FUNCTION-SQL (] entidad:mappingTo [,] [)]]
	 */
	@Override
	public String getWherePrimitiveSQL(final String dictionaryName_) {

		if (this.primitiveSQL == null) {
			this.primitiveSQL = new StringBuilder();
			try {
				for (final String operador : VirtualField.operators) {
					final String[] sqlFormula = this.whereExpression.toString().split(operador);
					if (sqlFormula.length >= 2) {
						this.primitiveSQL.append(this.traducirExpr(dictionaryName_, /* leftExpression */sqlFormula[0]));
						this.primitiveSQL.append(PCMConstants.STRING_SPACE).append(operador).append(PCMConstants.STRING_SPACE);
						this.primitiveSQL.append(this.traducirExpr(dictionaryName_, /* rightExpression */sqlFormula[1]));
						break;
					}
				}
			}
			catch (final PCMConfigurationException exc) {
				return null;
			}
		}
		return this.primitiveSQL.toString();
	}

	/**
	 * Traduce expresiones de este tipo: [FUNCTION-SQL] [(] entidad:mappingTo[,] [)]
	 * 
	 * @param express
	 * @return
	 */
	private String traducirExpr(final String dictionaryName_, final String express) throws PCMConfigurationException {
		final String expr_ = express;
		if (express != null && expr_.indexOf(PCMConstants.CHAR_BEGIN_PARENT) != -1) {
			final String[] functionSQL = expr_.split(PCMConstants.REGEXP_PARENT);
			final String argumentos = functionSQL[1].replace(PCMConstants.CHAR_END_PARENT, PCMConstants.CHAR_SPACE).trim();
			final StringBuilder newExpression = new StringBuilder(functionSQL[0]).append(PCMConstants.STRING_SPACE).append(
					PCMConstants.BEGIN_PARENTHESIS);
			final StringTokenizer strTokenizer = new StringTokenizer(argumentos, PCMConstants.COMMA);
			while (strTokenizer.hasMoreElements()) {
				newExpression.append(this.getTableAndField(dictionaryName_, (String) strTokenizer.nextElement(), this.dummy));
				newExpression.append(PCMConstants.COMMA).append(PCMConstants.STRING_SPACE);
			}
			newExpression.append(PCMConstants.STRING_SPACE).append(PCMConstants.END_PARENTHESIS);
			return newExpression.toString();
		} else if (express != null && expr_.indexOf(PCMConstants.CHAR_DOBLE_POINT) != -1) {
			return this.getTableAndField(dictionaryName_, expr_, this.dummy);
		}
		return expr_;
	}

	/**
	 * Devuelve <nombre Tabla>.<nombre campo> para una entidad:mappingTo recibidos como argumento
	 * 
	 * @return
	 */
	private String getTableAndField(final String dictionaryName_, final String entityAndField, final boolean dummy)
			throws PCMConfigurationException {
		final String[] splitter2 = entityAndField.split(PCMConstants.POINT_COMMA);
		final String entidad = splitter2[0].trim(), mappingTo_ = splitter2[1].trim();
		if (!dummy) {
			EntityLogic associatedEntity_ = EntityLogicFactory.getFactoryInstance().getEntityDef(dictionaryName_, entidad);
			if (associatedEntity_ == null) {
				throw new PCMConfigurationException(InternalErrorsConstants.MUST_DEFINE_AGGREGATE_FIELD.replaceFirst(
						InternalErrorsConstants.ARG_1, entidad));
			}
			int mappingTo = (mappingTo_.indexOf(PCMConstants.CHAR_BEGIN_CORCH) != -1) ? Integer.parseInt(mappingTo_
					.split(PCMConstants.REGEXP_BEGIN_CORCH)[0]) : Integer.parseInt(mappingTo_);
			IFieldLogic fieldDef = associatedEntity_.searchField(mappingTo);
			if (fieldDef == null) {
				throw new PCMConfigurationException(InternalErrorsConstants.MUST_DEFINE_SECUENCE_FIELD.replaceFirst(
						InternalErrorsConstants.ARG_1, String.valueOf(mappingTo)));
			}
			return new StringBuilder(associatedEntity_.getName()).append(PCMConstants.POINT).append(fieldDef.getName()).toString();
		}
		return new StringBuilder(entidad).append(PCMConstants.POINT).append(mappingTo_).toString();
	}

}
