package org.cdd.service.dataccess.persistence;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.cdd.common.PCMConstants;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.IViewComponent;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.IFieldValue;


public class SqliteDAOSQLImpl extends AnsiSQLAbstractDAOImpl {

	private static final String QUERY_WITH_PAGINATION = "SELECT (SELECT COUNT(*) #TABLES# #CLAUSULES_WHERE#) AS TOTALREG, #FIELDGOT# #TABLES# #CLAUSULES_WHERE# ORDER BY #ORDER_FIELD# #ORDER_DIRECTION# LIMIT ?,? ",
			QUERY_WITHOUT_PAGINATION = "SELECT #FIELDGOT# #TABLES# #CLAUSULES_WHERE#";

	@Override
	protected String getQueryPagination(final int offset_) {
		return offset_ != -1 ? SqliteDAOSQLImpl.QUERY_WITH_PAGINATION : SqliteDAOSQLImpl.QUERY_WITHOUT_PAGINATION;
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
	protected String getSpecialCharsConversion(String cadena_) {
		String cadena = cadena_.replaceAll("á", PCMConstants.PERCENTAGE_SCAPED);
		cadena = cadena.replaceAll("é", PCMConstants.PERCENTAGE_SCAPED);
		cadena = cadena.replaceAll("í", PCMConstants.PERCENTAGE_SCAPED);
		cadena = cadena.replaceAll("ó", PCMConstants.PERCENTAGE_SCAPED);
		cadena = cadena.replaceAll("ú", PCMConstants.PERCENTAGE_SCAPED);
		cadena = cadena.replaceAll("ñ", PCMConstants.PERCENTAGE_SCAPED);
		cadena = cadena.toUpperCase();
		/** escapado rústico ***/
		
		return cadena;
	}

	@Override
	protected String getRightPartPreffixForString() {
		return "";// COLLATE BINARY";
	}

	@Override
	protected String getAsUtf8CompareForString() {
		return "";
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * pcm.context.logicmodel.persistence.IDAOImpl#getSequenceExpr(pcm.context.logicmodel.definitions
	 * .IFieldLogic) */
	@Override
	public String getSequenceExpr(final IFieldLogic field) {
		return PCMConstants.EMPTY_;
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * pcm.context.logicmodel.persistence.IDAOImpl#getSequenceValueExpr(pcm.context.logicmodel.
	 * definitions.IFieldLogic) */
	@Override
	public String getSequenceValueExpr(final IFieldLogic field) {
		return PCMConstants.EMPTY_;
	}

	@Override
	protected String getDateOfRightSqlExpression(final String dateFormatted, final String operator) throws ParseException{
		try {
			final StringBuilder valuesWithOr = new StringBuilder(), newDateUnformatted = new StringBuilder();
			final Calendar cal = Calendar.getInstance();
			Date fecha = CommonUtils.myDateFormatter.parse(dateFormatted);			
			cal.setTime(fecha);

			final int date = cal.get(Calendar.DAY_OF_MONTH);
			final StringBuilder day = new StringBuilder(date < 10 ? IViewComponent.ZERO.concat(String.valueOf(date)) : String.valueOf(date));
			final int month_ = cal.get(Calendar.MONTH) + 1;
			final StringBuilder month = new StringBuilder(month_ < 10 ? IViewComponent.ZERO.concat(String.valueOf(month_))
					: String.valueOf(month_));
			newDateUnformatted.append(String.valueOf(cal.get(Calendar.YEAR))).append(PCMConstants.SIMPLE_SEPARATOR);
			newDateUnformatted.append(month).append(PCMConstants.SIMPLE_SEPARATOR).append(day);

			if (dateFormatted.indexOf("/") != -1 && dateFormatted.indexOf(":") != -1 && dateFormatted.length() > 10){
				// con hora, la anyadimos
				String[] dateFormattedAndTime = dateFormatted.split(" ");
				newDateUnformatted.append(" ");
				newDateUnformatted.append(dateFormattedAndTime[1]);				
			}else{
				newDateUnformatted.append(" 00:00:00");
			}
			
			valuesWithOr.append(PCMConstants.STRING_SPACE).append(SQLUtils.translateReversedOperator(operator));
			
			valuesWithOr.append(PCMConstants.STRING_SPACE).append(this.getRightDateValue(newDateUnformatted.toString()));
			
			return valuesWithOr.toString();
		}
		catch (final ParseException parseExc) {
			throw parseExc;
		}
	}

	@Override
	protected Timestamp getTimestamp(final ResultSet resultSet, final String alias) throws SQLException {
		try {
			
			final String dateFormatted = resultSet.getString(alias);
			if (dateFormatted == null || "".equals(dateFormatted.trim())) {
				return null;
			}
			final Calendar cal = Calendar.getInstance();
			if (dateFormatted.length() > 12) {
				// formato largo; puede venir en formato extendido '1417474800000' (timestamp)
				if (dateFormatted.indexOf(PCMConstants.SIMPLE_SEPARATOR) > 0) {// formato aaaa-mm-dd
																				// HH:MM:ss
					final String[] timeS = dateFormatted.split(PCMConstants.STRING_SPACE);
					final String[] timeS1 = timeS[0].split(PCMConstants.SIMPLE_SEPARATOR);
					cal.set(Calendar.YEAR, Integer.parseInt(timeS1[0]));
					cal.set(Calendar.MONTH, Integer.parseInt(timeS1[1]) - 1);
					cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeS1[2]));
					final String[] timeS2 = timeS[1].substring(0, 8).split(PCMConstants.CHAR_DOBLE_POINT_S);
					cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeS2[0]));
					cal.set(Calendar.MINUTE, Integer.parseInt(timeS2[1]));
					cal.set(Calendar.SECOND, Integer.parseInt(timeS2[2]));
				} else {// nomero, mills

					cal.setTime(new Date(Long.valueOf(dateFormatted).longValue()));
				}
			} else {
				// formato corto
				if (dateFormatted.indexOf(PCMConstants.SIMPLE_SEPARATOR) > 0) {// formato aaaa-mm-dd
					final String[] timeS1 = dateFormatted.split(PCMConstants.SIMPLE_SEPARATOR);
					cal.set(Calendar.YEAR, Integer.parseInt(timeS1[0]));
					cal.set(Calendar.MONTH, Integer.parseInt(timeS1[1]) - 1);
					cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeS1[2]));
				} else if (dateFormatted.indexOf(PCMConstants.INVERSE_SLASH) != -1) {// formato
																						// aaaa/mm/dd
					final String[] timeS1 = dateFormatted.split(PCMConstants.INVERSE_SLASH);
					cal.set(Calendar.YEAR, Integer.parseInt(timeS1[0]));
					cal.set(Calendar.MONTH, Integer.parseInt(timeS1[1]) - 1);
					cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeS1[2]));
				}
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
			}
			return new Timestamp(cal.getTimeInMillis());
		}
		catch (final NumberFormatException numberFormatExc) {
			throw new SQLException(numberFormatExc.getMessage());
		}
		catch (final IndexOutOfBoundsException indexExc) {
			throw new SQLException(indexExc.getMessage());
		}
	}

	@Override
	protected String getParameterArgument(IFieldLogic field, IFieldValue fieldV) {
		String argument_ = null;
		try {
			if (field.getAbstractField().isDate() && !fieldV.isNull()) {
				Serializable value = fieldV.getValue();
				value = value.toString().split(PCMConstants.REGEXP_POINT)[0];//quito decimales
				Calendar cal = Calendar.getInstance();
				cal.setTime(CommonUtils.myDateFormatter.parse(value));
				
				String time_ =  "00:00:00";
				if (!PCMConstants.EMPTY_.equals(value) && field.getAbstractField().isTimestamp()) {
					int hours_ = cal.get(Calendar.HOUR_OF_DAY);
					int minutes_ =  cal.get(Calendar.MINUTE);
					int seconds_ =  cal.get(Calendar.SECOND);
					time_ = CommonUtils.addLeftZeros(String.valueOf(hours_), 2) + ":" +
							CommonUtils.addLeftZeros(String.valueOf(minutes_), 2) + ":" + 
									CommonUtils.addLeftZeros(String.valueOf(seconds_), 2);
				}
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH) + 1;
				String month_ = month < 10 ? "0" + month : String.valueOf(month);
				int date = cal.get(Calendar.DAY_OF_MONTH);
				String date_ = CommonUtils.addLeftZeros(String.valueOf(date), 2);
				argument_ = "datetime('" + year + "-" + month_ + "-" + date_ + " " + time_ + "')";
			} else {
				argument_ = IDAOImpl.ARG;
			}
		}
		catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		return argument_;
	}

}
