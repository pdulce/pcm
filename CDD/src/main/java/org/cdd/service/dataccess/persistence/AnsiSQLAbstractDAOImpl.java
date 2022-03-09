package org.cdd.service.dataccess.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.cdd.common.InternalErrorsConstants;
import org.cdd.common.PCMConstants;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.definitions.FieldView;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.FieldViewSetCollection;
import org.cdd.service.component.definitions.IFieldView;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.FieldLogic;
import org.cdd.service.dataccess.definitions.FieldLogicComparator;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.definitions.IFieldAbstract;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.dataccess.dto.IFieldValue;
import org.cdd.service.dataccess.factory.EntityLogicFactory;


public abstract class AnsiSQLAbstractDAOImpl extends AbstractDAOImpl implements IDAOImpl {

	protected static Logger log = Logger.getLogger(AnsiSQLAbstractDAOImpl.class.getName());
	
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

	@Override
	public final FieldViewSet insert(final FieldViewSet fieldViewSet, final DAOConnection conn) throws DatabaseException, ParseException {
		final StringBuilder fieldSet = new StringBuilder();
		final StringBuilder fieldSetValues = new StringBuilder();
		Collection<IFieldLogic> camposConArgumentos = new ArrayList<IFieldLogic>();
		Iterator<IFieldLogic> iteratorCampos_ = fieldViewSet.getEntityDef().getFieldSet().values().iterator();
		while (iteratorCampos_.hasNext()) {
			final IFieldLogic field = iteratorCampos_.next();
			if (field.isVolatile()) {
				continue;
			}
			IFieldValue fieldV = fieldViewSet.getFieldvalue(field);
			if (field.isAutoIncremental()) {
				continue;
			} else if (field.belongsPK() && field.isSequence()) {
				final String sequenceExpr = this.getSequenceExpr(field), sequenceValueExpr = this.getSequenceValueExpr(field);
				if (sequenceExpr != null && sequenceValueExpr != null) {
					if (!fieldSet.toString().equals(PCMConstants.EMPTY_)) {
						fieldSet.append(PCMConstants.COMMA);
						fieldSetValues.append(PCMConstants.COMMA);
					}
					fieldSet.append(sequenceExpr);
					fieldSetValues.append(sequenceValueExpr);
					camposConArgumentos.add(field);
				}
			} else if (!fieldViewSet.getFieldvalue(field).isNull() || field.getAbstractField().getDefaultValueObject() != null) {
				if (!fieldSet.toString().equals(PCMConstants.EMPTY_)) {
					fieldSet.append(PCMConstants.COMMA);
					fieldSetValues.append(PCMConstants.COMMA);
				}
				fieldSet.append(field.getName().toUpperCase());
				fieldSetValues.append(getParameterArgument(field, fieldV));
				if (fieldSetValues.toString().endsWith(ARG)) {
					camposConArgumentos.add(field);
				}
			}
		}
		PreparedStatement pstmt = null;
		int retorno = 0;
		try {
			pstmt = conn.prepareStatement(SQLUtils.replaceInsertSql(INSERTAR_ENTIDAD, fieldViewSet.getEntityDef().getName().toUpperCase(),
					fieldSet.toString(), fieldSetValues.toString()));
			Iterator<IFieldLogic> iteratorCampos = camposConArgumentos.iterator();
			int contador = 1;
			while (iteratorCampos.hasNext()) {
				final IFieldLogic field = iteratorCampos.next();
				if (field.isVolatile()) {
					continue;
				}
				if (fieldViewSet.getFieldvalue(field).isNull() && field.isAutoIncremental() || (field.belongsPK() && field.isSequence())) {
					continue;
				}
				if (field.belongsPK() && (fieldViewSet.getFieldvalue(field).isNull())) {
					throw new SQLException(new StringBuilder(PK_ERROR).append(field.getName()).append(IS_NULL_LITERAL).toString());
				}
				if (!fieldViewSet.getFieldvalue(field).isNull() || field.getAbstractField().getDefaultValueObject() != null) {
					Serializable value = !fieldViewSet.getFieldvalue(field).isNull() ? fieldViewSet.getFieldvalue(field).getValue() : field
							.getAbstractField().getDefaultValueObject();
					if (field.getAbstractField().isDecimal()) {
						pstmt.setDouble(contador++, CommonUtils.numberFormatter.parse(value).doubleValue());
					} else if (field.getAbstractField().isDate()) {
						if (value!=null && !value.equals("")) {
							pstmt.setDate(contador++, new java.sql.Date(SQLUtils.getDateObjectForPreparedStatement(value).getTime()));
						}else {
							pstmt.setDate(contador++, null);
						}
					} else if (field.getAbstractField().isBoolean()) {
						pstmt.setInt(contador++, "1".equals(value.toString()) || "true".equals(value.toString()) ? 1 : 0);
					} else if (!field.getAbstractField().isBlob()) {
						pstmt.setObject(contador++, value);
					} else if (field.getAbstractField().isBlob()) {
						if (value == null || PCMConstants.EMPTY_.equals(value)) {
							pstmt.setBinaryStream(contador++, null, 0);
						} else {
							File fileUploaded = new File(value.toString());
							final byte[] binary_ = CommonUtils.getPrimitiveByteArrayFromUploadedFile(fileUploaded);
							if (binary_ != null) {
								pstmt.setBytes(contador++, binary_);
							}
							fileUploaded.delete();
						}
					}
				}
			}// for
			retorno = pstmt.executeUpdate();
		}
		catch (final SQLException exc) {
			exc.printStackTrace();
			if (exc.getCause() != null && exc.getCause().getMessage().indexOf(DUPLICATE_ERR_LITERAL) != -1) {
				throw new DatabaseException(new StringBuilder(RECORD_LITERAL).append(fieldViewSet.getEntityDef().getName()).append(EXISTS)
						.toString(), exc);
			} else if (exc.getCause() != null && exc.getMessage().indexOf("locked") != -1) {
				throw new DatabaseException("Database is locked", exc);
			}
			throw new DatabaseException(InternalErrorsConstants.BBDD_INSERT_EXCEPTION, exc);
		}
		catch (final Throwable exc) {
			// //AnsiSQLAbstractDAOImpl.log.error("Unknown Error", exc);
			throw new DatabaseException(InternalErrorsConstants.BBDD_INSERT_EXCEPTION, exc);
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			}
			catch (final Throwable ezc) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, ezc);
			}
		}
		if (retorno == 1) {
			return fieldViewSet;
		}
		return null;
	}

	@Override
	public final int update(final String service, final FieldViewSet fieldViewSet, final DAOConnection conn) throws DatabaseException, ParseException {

		if (IDataAccess.ELIMINAR_ENTIDAD.equals(service)) {
			final IFieldLogic fieldFecBaja = fieldViewSet.getEntityDef().getFieldSet()
					.get(this.auditFieldSet.getProperty(Datamap.FEC_BAJA));
			if (fieldFecBaja != null) {
				final Timestamp fecBajaOfActualRecord = SQLUtils.getTimestamp(fieldViewSet.getFieldvalue(fieldFecBaja).getValue());
				if (fecBajaOfActualRecord == null) {
					throw new DatabaseException(new StringBuilder(RECORD_LITERAL).append(fieldViewSet.getEntityDef().getName())
							.append(NO_FECBAJA).toString());
				}
			}
		}
		List<Serializable> valueObjectsPk = new ArrayList<Serializable>();		
		List<Serializable> valueObjects = new ArrayList<Serializable>();
		List<IFieldLogic> fieldsWithVal = new ArrayList<IFieldLogic>();
		final StringBuilder fieldSetValues = new StringBuilder();
		boolean first = true;
		int nArgs = 0;
		Iterator<IFieldLogic> iteratorCampos = fieldViewSet.getEntityDef().getFieldSet().values().iterator();
		while (iteratorCampos.hasNext()) {
			final IFieldLogic field = iteratorCampos.next();
			if (field.isVolatile()) {
				continue;
			}
			IFieldValue fieldV = fieldViewSet.getFieldvalue(field);
			Serializable value = fieldV.getValue();
			if (field.belongsPK()){//si viene PK en el objeto, lo almaceno
				valueObjectsPk.add(value);
				continue;
			}			
			
			if ( (fieldV.isNull() || fieldV.isEmpty()) && field.isRequired()) {					
				continue;
			}
			if (!first) {
				fieldSetValues.append(PCMConstants.COMMA);
			} else {
				first = false;
			}
			String parameterArg = getParameterArgument(field, fieldV);
			fieldSetValues.append(field.getName().toUpperCase()).append(PCMConstants.EQUALS).append(parameterArg);
			if (parameterArg.equals(ARG)) {
				fieldsWithVal.add(field);
				valueObjects.add(value);
				nArgs++;//incremento los argumentos del SET
			}
		}// while

		PreparedStatement pstmt = null;
		int res = 0;
		try {
			String whereClausule = SQLUtils.getWhereClausuleFKorPK(fieldViewSet);
			pstmt = conn.prepareStatement(SQLUtils.replaceUpdateSql(UPDATE_SENTENCE, fieldViewSet.getEntityDef().getName().toUpperCase(),
					fieldSetValues.toString(), whereClausule));
			iteratorCampos = fieldViewSet.getEntityDef().getFieldSet().values().iterator();
			for (int contadorArgs = 1; contadorArgs <= nArgs; contadorArgs++) {
				final IFieldLogic field = fieldsWithVal.get(contadorArgs-1);
				if (field.isVolatile() || field.belongsPK()) {
					continue;
				}
				IFieldValue fieldV = fieldViewSet.getFieldvalue(field);
				Serializable value = valueObjects.get(contadorArgs-1);
				if ( (fieldV.isNull() || fieldV.isEmpty()) && field.isRequired()) {					
					continue;
				} 
				if (field.getAbstractField().isDecimal() && value != null) {
					pstmt.setDouble(contadorArgs, CommonUtils.numberFormatter.parse(value).doubleValue());
				} else if (field.getAbstractField().isDate()) {
					if (value != null && !value.equals("")) {
						pstmt.setDate(contadorArgs, new java.sql.Date(SQLUtils.getDateObjectForPreparedStatement(value).getTime()));
					}else {
						pstmt.setDate(contadorArgs, null);
					}
				} else if (field.getAbstractField().isBoolean()) {
					pstmt.setInt(contadorArgs, "1".equals(value.toString()) || "true".equals(value.toString()) ? 1 : 0);
				} else if (!field.getAbstractField().isBlob()) {
					pstmt.setObject(contadorArgs, value);
				} else if (field.getAbstractField().isString()) {
					final byte[] bytesOfvalue = ((String) value).getBytes("UTF-8");// Charset
					value = new String(bytesOfvalue);
					pstmt.setString(contadorArgs, (String) value);
				} else {
					if (value == null || PCMConstants.EMPTY_.equals(value)) {
						// nothing
					} else {
						File fileUploaded = new File(value.toString());
						final byte[] binary_ = CommonUtils.getPrimitiveByteArrayFromUploadedFile(fileUploaded);
						if (binary_ != null) {
							pstmt.setBytes(contadorArgs, binary_);
						}
						fileUploaded.delete();
					}
				}
			}// for

			// OK
			for (int j1=0;j1< valueObjectsPk.size();j1++) {
				
				Serializable value = valueObjectsPk.get(j1); 
				if (value == null || "".equals(value.toString())) {
					continue;
				}
				
				pstmt.setObject((nArgs+1), value);
				nArgs++;
			}
			res = pstmt.executeUpdate();
		}
		catch (final SQLException exc1) {
			exc1.printStackTrace();
			res = -1;
			AnsiSQLAbstractDAOImpl.log.info("exception1 = " + exc1);
			throw new DatabaseException(InternalErrorsConstants.BBDD_UPDATE_EXCEPTION, exc1);
		}
		catch (final Throwable exc2) {
			exc2.printStackTrace();
			throw new DatabaseException(InternalErrorsConstants.BBDD_UPDATE_EXCEPTION, exc2);
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			}
			catch (final Throwable ezc) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, ezc);
			}
		}
		return res;
	}

	@Override
	public final int delete(final FieldViewSet fieldViewSet, final DAOConnection conn) throws DatabaseException, ParseException {
		final StringBuilder fieldSetValues = new StringBuilder();
		boolean first = true;
		Iterator<IFieldLogic> iteratorCampos = fieldViewSet.getEntityDef().getFieldSet().values().iterator();
		while (iteratorCampos.hasNext()) {
			final IFieldLogic field = iteratorCampos.next();
			if (field.isVolatile()) {
				continue;
			}
			if (field.getAbstractField().isBlob() || fieldViewSet.getFieldvalue(field).isNull() || fieldViewSet.getFieldvalue(field).isEmpty()) {
				continue;
			}
			if (field.belongsPK()) {
				return deleteByPk(fieldViewSet, conn);
			}
			if (!first) {
				fieldSetValues.append(" AND ");
			} else {
				first = false;
			}
			fieldSetValues.append(field.getName().toUpperCase()).append(PCMConstants.EQUALS).append(ARG);
		}

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(SQLUtils.replaceDeleteSql(ELIMINAR_ENTIDAD, fieldViewSet.getEntityDef().getName().toUpperCase(),
					fieldSetValues.toString()));
			int contador = 1;
			iteratorCampos = fieldViewSet.getEntityDef().getFieldSet().values().iterator();
			while (iteratorCampos.hasNext()) {
				final IFieldLogic field = iteratorCampos.next();
				if (field.isVolatile()) {
					continue;
				}
				if (fieldViewSet.getFieldvalue(field).isNull() || fieldViewSet.getFieldvalue(field).isEmpty()) {
					continue;
				}
				final Serializable value = fieldViewSet.getFieldvalue(field).getValue();
				if (!field.getAbstractField().isBlob()) {
					pstmt.setObject(contador++, value);
				} else if (field.getAbstractField().isBlob()) {
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
			return pstmt.executeUpdate();
		}
		catch (final Throwable exc) {
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

	}

	@Override
	public final FieldViewSet getRecordByPrimaryKey(final FieldViewSet fieldViewSet, final DAOConnection conn) throws DatabaseException, ParseException {
		String whereClausule = SQLUtils.getWhereClausuleFKorPK(fieldViewSet);
		final String sql = SQLUtils
				.replaceSelectByPkSql(CONSULTA_BY_PK, fieldViewSet.getEntityDef().getName().toUpperCase(), whereClausule);
		int numberOfParams = SQLUtils.countParams(whereClausule, "=");

		FieldViewSet resultado = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			int contador = 1;
			Iterator<IFieldLogic> iteratorCamposClave = fieldViewSet.getEntityDef().getFieldKey().getPkFieldSet().iterator();
			while (iteratorCamposClave.hasNext() && contador <= numberOfParams) {
				final IFieldLogic fieldEntityDefPK = iteratorCamposClave.next();
				if (fieldEntityDefPK.isVolatile()) {
					continue;
				}
				if (fieldViewSet.getFieldvalue(fieldEntityDefPK).isNull()
						|| fieldEntityDefPK.getAbstractField().getDefaultValueObject() != null) {
					continue;
				}
				Serializable value = !fieldViewSet.getFieldvalue(fieldEntityDefPK).isNull() ? fieldViewSet.getFieldvalue(fieldEntityDefPK)
						.getValue() : fieldEntityDefPK.getAbstractField().getDefaultValueObject();
				if (fieldEntityDefPK.getAbstractField().isNumeric()){
					value = value.toString().replaceAll(PCMConstants.REGEXP_POINT, "");
					value = value.toString().replaceAll(",", ".");
				}
				pstmt.setObject(contador++, value);
			}

			final Iterator<IFieldLogic> iteratorCampos = fieldViewSet.getEntityDef().getFieldSet().values().iterator();
			while (iteratorCampos.hasNext() && contador <= numberOfParams) {
				final IFieldLogic fieldLogic = iteratorCampos.next();
				if (fieldLogic.isVolatile()) {
					continue;
				}
				if (!(fieldLogic.belongsPK())) {
					continue;
				}
				final Collection<String> values = fieldViewSet.getFieldvalue(fieldLogic).getValues();
				if (values == null || values.isEmpty() || values.iterator().next() == null) {
					continue;
				}
				final Serializable value = values.iterator().next();
				if (!fieldLogic.getAbstractField().isBlob()) {
					pstmt.setObject(contador++, value);
				} else {
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
			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				resultado = fieldViewSet.copyOf();
				final Iterator<IFieldView> iteratorFields = resultado.getFieldViews().iterator();
				while (iteratorFields.hasNext()) {
					final IFieldView fieldView = iteratorFields.next();
					final IFieldLogic fieldLogic = fieldView.getEntityField();
					if (fieldLogic.isVolatile()) {
						continue;
					}
					if (fieldLogic.getAbstractField().isBlob() && resultSet.getObject(fieldLogic.getName()) != null) {
						final byte[] bytesOfStream = resultSet.getBytes(fieldLogic.getName());
						if (bytesOfStream != null && bytesOfStream.length > 0) {
							String fileName = CommonUtils.getFileNameUploadedFromPrimitiveByteArray(bytesOfStream);
							File file = new File(new StringBuilder(this.appDomain.getResourcesConfiguration().getUploadDir()).append(fileName).toString());
							if (!file.exists()) {
								// crear fichero
								FileOutputStream fout = null;
								try {
									fout = new FileOutputStream(file);
									fout.write(CommonUtils.getBytesOfFileFromPrimitiveByteArray(bytesOfStream));
								}
								catch (FileNotFoundException e1) {
									throw new SQLException(e1.getMessage());
								}
								catch (IOException e2) {
									throw new SQLException(e2.getMessage());
								} finally {
									try {
										fout.flush();
										fout.close();
									}
									catch (IOException e3) {
										throw new SQLException(e3.getMessage());
									}
								}
							}
							resultado.setValue(fieldLogic.getName(), file.getAbsolutePath());
						}
					} else {
						if (fieldLogic.getAbstractField().isDate() || fieldLogic.getAbstractField().isTimestamp()) {
							try {
								resultado.setValue(fieldView.getQualifiedContextName(), getTimestamp(resultSet, fieldLogic.getName()));
							}
							catch (final SQLException sqlExcc) {
								if (sqlExcc.getMessage().indexOf(EMPTY_DATE) == -1) {
									throw sqlExcc;
								}
							}
						} else {
							if (fieldLogic.getAbstractField().isLong()) {
								resultado.setValue(fieldView.getQualifiedContextName(),
										Long.valueOf(resultSet.getLong(fieldLogic.getName())));
							} else if (fieldLogic.getAbstractField().isBoolean()) {
								resultado.setValue(fieldView.getQualifiedContextName(),
										Boolean.valueOf(resultSet.getBoolean(fieldLogic.getName())).booleanValue() ? Integer.valueOf(1)
												: Integer.valueOf(0));
							} else {
								resultado.setValue(fieldView.getQualifiedContextName(),
										(Serializable) resultSet.getObject(fieldLogic.getName()));
							}
						}
					}
				}
			}
		}
		catch (final SQLException exc) {
			// //AnsiSQLAbstractDAOImpl.log.error(exc);
			throw new DatabaseException(InternalErrorsConstants.BBDD_QUERYBYPK_EXCEPTION, exc);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			}
			catch (final Throwable ezc) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, ezc);
			}
		}
		return resultado;
	}

	@Override
	public final List<List<Serializable>> selectWithSpecificFields(final FieldViewSet fieldViewSet, final List<Integer> fieldMappings,
			final DAOConnection conn) throws DatabaseException, ParseException {
		// OJO: falla porque en lugar de ='' lo que hace es un like y al ser un campo de codigo, se
		// loa
		List<IFieldLogic> fieldCollection = new ArrayList<IFieldLogic>();
		fieldCollection.addAll(fieldViewSet.getEntityDef().getFieldSet().values());
		Collections.sort(fieldCollection, new FieldLogicComparator());
		StringBuilder groupBy = new StringBuilder("");

		final StringBuilder fieldSetValues = new StringBuilder();
		boolean first = true;
		int contabilizados = 0;
		for (final IFieldLogic field : fieldCollection) {
			if (field.isVolatile()) {
				continue;
			}
			if (fieldMappings.contains(Integer.valueOf(field.getMappingTo())) && contabilizados < fieldMappings.size()) {
				if (!first) {
					fieldSetValues.append(PCMConstants.COMMA);
					fieldSetValues.append(" ");
				} else {
					first = false;
				}
				fieldSetValues.append(field.getName());
				contabilizados++;
				if (!field.getAbstractField().isNumeric()){
					if (!groupBy.toString().equals("")){
						groupBy.append(PCMConstants.COMMA);
						groupBy.append(" ");
					}
					groupBy.append(field.getName());
				}
			}
		}

		final Collection<Map<String, Serializable>> sqlParameters = new ArrayList<Map<String, Serializable>>();
		final Map<String, Integer> whereForEntityAndArgs = this.getEntityFilter(fieldViewSet, sqlParameters, this.auditActivated,
				this.auditFieldSet);
		final Map.Entry<String, Integer> entryWhereForEntity = whereForEntityAndArgs.entrySet().iterator().next();
		final String whereForEntity = entryWhereForEntity.getKey();

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		List<List<Serializable>> resultados = new ArrayList<List<Serializable>>(100000);
		try {
			// SELECT #FIELDS# FROM #TABLE# #FIELDSET#
			String sql = SQLUtils.replaceSelectByEntitySql(CONSULTA_WITH_SPECIFIC_FIELDS, fieldViewSet.getEntityDef().getName(),
					whereForEntity);
			sql = sql.replaceFirst("#FIELDS#", fieldSetValues.toString());
			sql = (!groupBy.toString().equals(""))?sql.concat(" GROUP BY " + groupBy.toString()):sql;
			pstmt = conn.prepareStatement(sql);
			int contador = 1;
			for (final IFieldLogic field : fieldCollection) {	
				if (field.isVolatile()) {
					continue;
				}
				if (fieldViewSet.getFieldvalue(field).isNull() || fieldViewSet.getFieldvalue(field).isEmpty()) {
					continue;
				}
				final IFieldValue fieldValue = fieldViewSet.getFieldvalue(field);
				Iterator<String> iteValues = fieldValue.getValues().iterator();
				while (iteValues.hasNext()) {
					String value = iteValues.next();
					if (!field.getAbstractField().isBlob()) {
						if (field.getAbstractField().isNumeric() && value != null){
							value = value.toString().replaceAll(PCMConstants.REGEXP_POINT, "");
							value = value.toString().replaceAll(",", ".");
						}else if (field.getAbstractField().isString()) {							
							value = value.replaceAll(PCMConstants.ASTERISC_SCAPED, PCMConstants.PERCENTAGE_SCAPED);
							value = getSpecialCharsConversion(value);
						}
						pstmt.setObject(contador++, value);
					} else if (field.getAbstractField().isBlob()) {
						if (PCMConstants.EMPTY_.equals(value)) {
							// nothing
						} else {
							final byte[] binary_ = CommonUtils.getPrimitiveByteArrayFromUploadedFile(new File(value.toString()));
							if (binary_ != null) {
								pstmt.setBytes(contador++, binary_);
							}
						}
					}
				}// while
			}
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				final List<Serializable> tupla = new ArrayList<Serializable>();
				final Iterator<Integer> iteratorFields = fieldMappings.iterator();
				while (iteratorFields.hasNext()) {
					IFieldLogic fieldLogic = fieldViewSet.getEntityDef().searchField(iteratorFields.next().intValue());
					if (fieldLogic.isVolatile()) {
						continue;
					}
					try {
						Serializable valueSerialized = (Serializable) resultSet.getObject(fieldLogic.getName());
						IFieldAbstract fieldAbstract = fieldLogic.getAbstractField();
						if (fieldAbstract.isDate() || fieldAbstract.isTimestamp()) {
							valueSerialized = getTimestamp(resultSet, fieldLogic.getName());
						} else if (fieldAbstract.isLong()) {
							valueSerialized = Long.valueOf(resultSet.getLong(fieldLogic.getName()));
						} else if (fieldAbstract.isInteger()) {
							valueSerialized = Integer.valueOf(resultSet.getInt(fieldLogic.getName()));
						} else if (fieldAbstract.isDecimal()) {
							valueSerialized = Double.valueOf(resultSet.getDouble(fieldLogic.getName()));
						} else if (fieldAbstract.isBoolean()) {
							valueSerialized = Boolean.valueOf(resultSet.getInt(fieldLogic.getName()) == 1 ? true : false);
						} else {
							valueSerialized = (Serializable) resultSet.getObject(fieldLogic.getName());
						}
						tupla.add(valueSerialized);
					}catch (SQLException sqlExcc) {
						AnsiSQLAbstractDAOImpl.log.log(Level.SEVERE, "Error.. al obtener campo..." + fieldLogic.getName(), sqlExcc);
						throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, new SQLException(
								"Error.. al obtener campo..." + fieldLogic.getName()));
					}
				}// while of fieldset iterator
				resultados.add(tupla);
			}// while resultset iterado

		}
		catch (final SQLException exc11) {
			throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, exc11);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			}
			catch (final Throwable ezc) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, ezc);
			}
		}
		return resultados;

	}

	
	@Override
	public final List<Map<FieldViewSet, Map<String,Double>>> selectWithAggregateFuncAndGroupBy(final FieldViewSet fieldViewSet,
			final List<IEntityLogic> joinFViewSet, final List<IFieldLogic> joinFView, final String aggregateFunction_,
			final IFieldLogic[] fieldsToAggregate, final IFieldLogic[] fieldsForGroupBy, final IFieldLogic orderbyField, final String order, final DAOConnection conn)
			throws DatabaseException, ParseException {
//n
		List<IFieldLogic> fieldCollection = new ArrayList<IFieldLogic>();
		fieldCollection.addAll(fieldViewSet.getEntityDef().getFieldSet().values());
		Collections.sort(fieldCollection, new FieldLogicComparator());

		final Collection<Map<String, Serializable>> sqlParameters = new ArrayList<Map<String, Serializable>>();
		final Map<String, Integer> whereForEntityAndArgs = this.getEntityFilter(fieldViewSet, sqlParameters, this.auditActivated,
				this.auditFieldSet);
		final Map.Entry<String, Integer> entryWhereForEntity = whereForEntityAndArgs.entrySet().iterator().next();
		final String whereForEntity = entryWhereForEntity.getKey();
		
		List<Map<FieldViewSet, Map<String,Double>>> resultados = new ArrayList<Map<FieldViewSet, Map<String,Double>>>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			
			//"SELECT #AGGREGATE# #FIELD# #JOINFIELD# FROM #TABLE# #JOINTABLE# #FIELDSET# #JOIN# #GROUP_BY# ORDER BY #FIELDORDER# #ORDER#"
			String sql = SQLUtils.replaceSelectByEntitySql(CONSULTA_WITH_AGGREGATE_FUNC_AND_GROUP_BY, fieldViewSet.getEntityDef().getName(), 
					whereForEntity);
			String tableAlias = SQLUtils.getAlias(fieldViewSet.getEntityDef().getName());

			StringBuilder agregadosSQL = new StringBuilder();
			if (fieldsToAggregate != null){
				for (int i=0;i<fieldsToAggregate.length;i++){
					if (!agregadosSQL.toString().isEmpty()){
						agregadosSQL.append(", ");
					}
					agregadosSQL.append(aggregateFunction_.concat("(" + tableAlias.concat(".").concat(fieldsToAggregate[i].getName()) + ") as total" + i));
				}
			}else{
				agregadosSQL.append("COUNT(*) as total0");
			}
			sql = sql.replaceFirst("#AGGREGATE#", agregadosSQL.toString());				
			if (!joinFViewSet.isEmpty() && fieldsForGroupBy != null) {				
				String aliasEntidadJoin = SQLUtils.getAlias(joinFViewSet.get(0).getName());
				// hacemos el JOIN entre el fieldToAggrupation correspondiente con el joinFView
				sql = sql.replaceFirst("#JOINFIELD#", ", ".concat(aliasEntidadJoin).concat(".").concat(joinFView.get(0).getName()/* NOMBRE */));
				sql = sql.replaceFirst("#JOINTABLE#",
						", ".concat(joinFViewSet.get(0).getName()/* DELEGACION */).concat(" ").concat(aliasEntidadJoin));
				StringBuilder join = new StringBuilder();
				// buscamos el FK de la entidad filtro contra la joinEntity
				for (IFieldLogic fieldLogic : fieldsForGroupBy) {	
					if (fieldLogic.isVolatile()) {
						continue;
					}
					if (fieldLogic.getParentFieldEntity(joinFViewSet.get(0).getName()) != null) {
						join.append(aliasEntidadJoin);
						join.append(".");
						join.append(fieldLogic.getParentFieldEntity(joinFViewSet.get(0).getName()).getName());
						join.append("=");
						join.append(tableAlias);
						join.append(".");
						join.append(fieldLogic.getName());
						break;
					}
				}
				if (sql.indexOf("WHERE") == -1) {
					sql = sql.replaceFirst("#JOIN#", "WHERE ".concat(join.toString()));
				} else {
					sql = sql.replaceFirst("#JOIN#", " AND ".concat(join.toString()));
				}
			} else {
				sql = sql.replaceFirst("#JOINFIELD#", "");
				sql = sql.replaceFirst("#JOINTABLE#", "");
				sql = sql.replaceFirst("#JOIN#", "");
			}
			
			String groupByTogetInSelect = ", ", conAlias= "";
			int fieldsCount = fieldsForGroupBy.length;
			for (int i = 0; i < fieldsCount; i++) {
				if (fieldsForGroupBy[i] == null){
					throw new Throwable("Error: no hay filtro de agrupación definido");					
				}
				groupByTogetInSelect += tableAlias.concat(".").concat(fieldsForGroupBy[i].getName());
				conAlias += tableAlias.concat(".").concat(fieldsForGroupBy[i].getName());
				if (i < (fieldsCount - 1)) {
					groupByTogetInSelect += ", ";
					conAlias += ", ";
				}
			}
			if (conAlias.length() > 1){
				sql = sql.replaceFirst("#FIELD#", groupByTogetInSelect);
				sql = sql.replaceFirst("#GROUP_BY#", fieldsForGroupBy.length == 0 ? "" : " GROUP BY ".concat(conAlias));
				sql = sql.replaceFirst("#FIELDORDER#", tableAlias.concat(".").concat(orderbyField.getName()));
				sql = sql.replaceFirst("#ORDER#", order);
			}
			
			
			int contador = 1;
			pstmt = conn.prepareStatement(sql);
			for (final IFieldLogic field : fieldCollection) {
				if (field.isVolatile()) {
					continue;
				}
				if (fieldViewSet.getFieldvalue(field).isNull() || fieldViewSet.getFieldvalue(field).isEmpty()) {
					continue;
				}
				final IFieldValue fieldValue = fieldViewSet.getFieldvalue(field);
				Iterator<String> iteValues = fieldValue.getValues().iterator();
				while (iteValues.hasNext()) {
					String value = iteValues.next().toString();
					if (!field.getAbstractField().isBlob()) {
						if (field.getAbstractField().isNumeric() && value != null){
							value = value.toString().replaceAll(PCMConstants.REGEXP_POINT, "");
							value = value.toString().replaceAll(",", ".");
						} else if (field.getAbstractField().isString()) {							
							value = value.replaceAll(PCMConstants.ASTERISC_SCAPED, PCMConstants.PERCENTAGE_SCAPED);
							value = getSpecialCharsConversion(value);
						}
						pstmt.setObject(contador++, value);
					} else if (field.getAbstractField().isBlob()) {
						if (PCMConstants.EMPTY_.equals(value)) {
							// nothing
						} else {
							final byte[] binary_ = CommonUtils.getPrimitiveByteArrayFromUploadedFile(new File(value.toString()));
							if (binary_ != null) {
								pstmt.setBytes(contador++, binary_);
							}
						}
					}
				}// while
			}
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				Map<FieldViewSet, Map<String,Double>> entrada = new HashMap<FieldViewSet, Map<String,Double>>();
				FieldViewSet fSet = new FieldViewSet(fieldViewSet.getEntityDef());
				int fieldsGroupBylength = fieldsForGroupBy == null || fieldsForGroupBy[0] == null ? 0 : fieldsForGroupBy.length;
				for (int i = 0; i < fieldsGroupBylength; i++) {
					String groupByFView = fieldsForGroupBy[i].getName();
					String valueOfCategoryIesima = resultSet.getString(groupByFView);
					fSet.setValue(groupByFView, valueOfCategoryIesima);
				}
				Map<String,Double> retornoAgregados = new HashMap<String,Double>();
				if (fieldsToAggregate != null){
					for (int i=0;i<fieldsToAggregate.length;i++){
						retornoAgregados.put(fieldsToAggregate[i].getName(), Double.valueOf(resultSet.getDouble("total"+i)));
					}
				}else{
					retornoAgregados.put(fSet.getEntityDef().getName(), Double.valueOf(resultSet.getDouble("total0")));
				}
				entrada.put(fSet, retornoAgregados/*subTotales, puede haber mos de una dimension*/);
				// recogemos, si procede el valor de la columna de la tabla master
				if (!joinFViewSet.isEmpty()) {
					FieldViewSet fSetMaster = new FieldViewSet(joinFViewSet.get(0));
					String valueOfJoinFView = resultSet.getString(joinFView.get(0).getName()/* "NOMBRE" */);
					fSetMaster.setValue(joinFView.get(0).getName(), valueOfJoinFView);
				}
				resultados.add(entrada);
				//resultados.add(entrada);
				
			}
		}
		catch (final SQLException exc2) {
			throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, exc2);
		} catch (final Throwable exc21) {
			exc21.printStackTrace();
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			}
			catch (final Throwable ezc) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, ezc);
			}
		}
		return resultados;

	}
	
	
	@Override
	public final double selectWithAggregateFunction(final FieldViewSet fieldViewSet, final String aggregateFunction, final int fieldIndex_,
			final DAOConnection conn) throws DatabaseException, ParseException {

		List<IFieldLogic> fieldCollection = new ArrayList<IFieldLogic>();
		fieldCollection.addAll(fieldViewSet.getEntityDef().getFieldSet().values());
		Collections.sort(fieldCollection, new FieldLogicComparator());

		final Collection<Map<String, Serializable>> sqlParameters = new ArrayList<Map<String, Serializable>>();
		final Map<String, Integer> whereForEntityAndArgs = this.getEntityFilter(fieldViewSet, sqlParameters, this.auditActivated,
				this.auditFieldSet);
		final Map.Entry<String, Integer> entryWhereForEntity = whereForEntityAndArgs.entrySet().iterator().next();
		final String whereForEntity = entryWhereForEntity.getKey();

		double aggregateResult = 0.00;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			// "SELECT #FIELD# FROM #TABLE# #FIELDSET#"
			String sql = SQLUtils.replaceSelectByEntitySql(CONSULTA_WITH_AGGREGATE_FUNCTION, fieldViewSet.getEntityDef().getName(),
					whereForEntity);
			sql = sql.replaceFirst(
					"#FIELD#",
					aggregateFunction.concat("(")
							.concat(fieldIndex_ == -1 ? "*" : fieldViewSet.getEntityDef().searchField(fieldIndex_).getName()).concat(")"));
			int contador = 1;
			pstmt = conn.prepareStatement(sql);
			for (final IFieldLogic field : fieldCollection) {
				if (field.isVolatile()) {
					continue;
				}
				if (fieldViewSet.getFieldvalue(field).isNull() || fieldViewSet.getFieldvalue(field).isEmpty()) {
					continue;
				}
				final IFieldValue fieldValue = fieldViewSet.getFieldvalue(field);
				Iterator<String> iteValues = fieldValue.getValues().iterator();
				while (iteValues.hasNext()) {
					String value = iteValues.next();					
					if (!field.getAbstractField().isBlob()) {
						if (field.getAbstractField().isNumeric() && value != null){
							value = value.toString().replaceAll(PCMConstants.REGEXP_POINT, "");
							value = value.toString().replaceAll(",", ".");							
						}else if (field.getAbstractField().isString()) {							
							value = value.replaceAll(PCMConstants.ASTERISC_SCAPED, PCMConstants.PERCENTAGE_SCAPED);
							value = getSpecialCharsConversion(value);
						}
						pstmt.setObject(contador++, value);
					} else if (field.getAbstractField().isBlob()) {
						if (PCMConstants.EMPTY_.equals(value)) {
							// nothing
						} else {
							final byte[] binary_ = CommonUtils.getPrimitiveByteArrayFromUploadedFile(new File(value.toString()));
							if (binary_ != null) {
								pstmt.setBytes(contador++, binary_);
							}
						}
					}
				}// while
			}
			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				aggregateResult = resultSet.getDouble(1);
			}
		}
		catch (final SQLException exc11) {
			throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, exc11);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			}
			catch (final Throwable ezc) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, ezc);
			}
		}
		return aggregateResult;

	}

	@Override
	public final List<FieldViewSet> selectWithDistinct(final FieldViewSet fieldViewSet, final int fieldIndex, final String order_,
			final DAOConnection conn) throws DatabaseException, ParseException {

		final String fieldName = fieldViewSet.getEntityDef().searchField(fieldIndex).getName();
		List<FieldViewSet> resultados = new ArrayList<FieldViewSet>();
		final StringBuilder fieldSetValues = new StringBuilder();
		boolean first = true;
		List<IFieldLogic> fieldCollection = new ArrayList<IFieldLogic>();
		fieldCollection.addAll(fieldViewSet.getEntityDef().getFieldSet().values());
		Collections.sort(fieldCollection, new FieldLogicComparator());
		for (final IFieldLogic field : fieldCollection) {
			if (field.isVolatile()) {
				continue;
			}
			if (field.getAbstractField().isBlob() || fieldViewSet.getFieldvalue(field).isNull() || fieldViewSet.getFieldvalue(field).isEmpty()) {
				continue;
			}
			if (!first) {
				fieldSetValues.append(PCMConstants.COMMA);
			} else {
				first = false;
			}
			fieldSetValues.append(field.getName().toUpperCase()).append(PCMConstants.EQUALS).append(ARG);
		}

		final Collection<Map<String, Serializable>> sqlParameters = new ArrayList<Map<String, Serializable>>();
		final Map<String, Integer> whereForEntityAndArgs = this.getEntityFilter(fieldViewSet, sqlParameters, this.auditActivated,
				this.auditFieldSet);
		final Map.Entry<String, Integer> entryWhereForEntity = whereForEntityAndArgs.entrySet().iterator().next();
		final String whereForEntity = entryWhereForEntity.getKey();

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			// "SELECT #FIELD# FROM #TABLE# #FIELDSET#"
			String sql = SQLUtils.replaceSelectByEntitySql(CONSULTA_WITH_DISTINCT, fieldViewSet.getEntityDef().getName(), whereForEntity);
			sql = sql.replaceAll("#FIELD#", fieldName);
			sql = sql.replaceFirst("#ORDER#", order_);
			int contador = 1;
			pstmt = conn.prepareStatement(sql);
			for (final IFieldLogic field : fieldCollection) {
				if (field.isVolatile()) {
					continue;
				}
				if (fieldViewSet.getFieldvalue(field).isNull() || fieldViewSet.getFieldvalue(field).isEmpty()) {
					continue;
				}
				final IFieldValue fieldValue = fieldViewSet.getFieldvalue(field);
				Iterator<String> iteValues = fieldValue.getValues().iterator();
				while (iteValues.hasNext()) {
					String value = iteValues.next();
					if (!field.getAbstractField().isBlob()) {
						if (field.getAbstractField().isNumeric() && value != null){
							value = value.toString().replaceAll(PCMConstants.REGEXP_POINT, "");
							value = value.toString().replaceAll(",", ".");
						}else if (field.getAbstractField().isString()) {							
							value = value.replaceAll(PCMConstants.ASTERISC_SCAPED, PCMConstants.PERCENTAGE_SCAPED);
							value = getSpecialCharsConversion(value);
						}
						pstmt.setObject(contador++, value);
					} else if (field.getAbstractField().isBlob()) {
						if (PCMConstants.EMPTY_.equals(value)) {
							// nothing
						} else {
							final byte[] binary_ = CommonUtils.getPrimitiveByteArrayFromUploadedFile(new File(value.toString()));
							if (binary_ != null) {
								pstmt.setBytes(contador++, binary_);
							}
						}
					}
				}// while
			}
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				Serializable serialValue = (Serializable) resultSet.getObject(1);
				if (serialValue == null || CommonUtils.cleanWhitespaces(serialValue.toString()).equals("")) {
					continue;
				}
				FieldViewSet record = fieldViewSet.copyOf();
				record.setValue(fieldViewSet.getEntityDef().searchField(fieldIndex).getName(), serialValue);
				resultados.add(record);
			}// while
		}
		catch (final Throwable exc) {
			// AnsiSQLAbstractDAOImpl.log.error(exc);
			throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, exc);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			}
			catch (final Throwable ezc) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, ezc);
			}
		}
		return resultados;

	}

	@Override
	public final long countAll(final FieldViewSet fieldViewSet, final DAOConnection conn) 
			throws DatabaseException, ParseException {
		final StringBuilder fieldSetValues = new StringBuilder();
		boolean first = true;
		List<IFieldLogic> fieldCollection = new ArrayList<IFieldLogic>();
		fieldCollection.addAll(fieldViewSet.getEntityDef().getFieldSet().values());
		Collections.sort(fieldCollection, new FieldLogicComparator());
		for (final IFieldLogic field : fieldCollection) {
			if (field.isVolatile()) {
				continue;
			}
			if (field.getAbstractField().isBlob() || fieldViewSet.getFieldvalue(field).isNull() || fieldViewSet.getFieldvalue(field).isEmpty()) {
				continue;
			}
			if (!first) {
				fieldSetValues.append(PCMConstants.COMMA);
			} else {
				first = false;
			}
			fieldSetValues.append(field.getName().toUpperCase()).append(PCMConstants.EQUALS).append(ARG);
		}

		final Collection<Map<String, Serializable>> sqlParameters_ = new ArrayList<Map<String, Serializable>>();
		final Map<String, Integer> whereForEntityAndArgs = this.getEntityFilter(fieldViewSet, sqlParameters_, this.auditActivated,
				this.auditFieldSet);
		final Map.Entry<String, Integer> entryWhereForEntity = whereForEntityAndArgs.entrySet().iterator().next();
		final String whereForEntity = entryWhereForEntity.getKey();

		int total = -1;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(SQLUtils.replaceSelectByEntitySql(CONSULTA_COUNT_ALL, fieldViewSet.getEntityDef().getName(),
					whereForEntity));
			int contador = 1;			
			for (final IFieldLogic field : fieldCollection) {
				if (field.isVolatile()) {
					continue;
				}
				if (fieldViewSet.getFieldvalue(field).isNull() || fieldViewSet.getFieldvalue(field).isEmpty()) {
					continue;
				}
				Serializable value = fieldViewSet.getFieldvalue(field).getValue();
				if (!field.getAbstractField().isBlob()) {
					if (field.getAbstractField().isNumeric() && value != null){
						value = value.toString().replaceAll(PCMConstants.REGEXP_POINT, "");
						value = value.toString().replaceAll(",", ".");
					}else if (field.getAbstractField().isString()) {							
						String value_ = ((String)value).replaceAll(PCMConstants.ASTERISC_SCAPED, PCMConstants.PERCENTAGE_SCAPED);
						value = getSpecialCharsConversion(value_);
					}
					pstmt.setObject(contador++, value);
				} else if (field.getAbstractField().isBlob()) {
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
			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				total = resultSet.getInt(1);
			}
		}
		catch (final SQLException exc) {
			// //AnsiSQLAbstractDAOImpl.log.error(exc);
			throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, exc);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			}
			catch (final Throwable ezc) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, ezc);
			}
		}
		return total;

	}
	
	
	@Override
	public final List<FieldViewSetCollection> queryWithPagination(final boolean firstAndLastOnly_,
			final List<FieldViewSetCollection> entidadesCollection_, final int tamPaginacion_, final int offset_,
			final String[] orderFields_, final String orderDirec_, final DAOConnection conn) throws DatabaseException, ParseException {
		final List<FieldViewSetCollection> resultado = new ArrayList<FieldViewSetCollection>();
		
		String[] orderFieldsAlias_ = new String[orderFields_.length];
		String parentNameOrderBy = null;
		String[] fieldNameOrderByArr = new String[orderFields_.length];
		for (int o_ = 0; o_ < orderFields_.length; o_++) {
			final String orderField_ = orderFields_[o_];
			if (orderField_==null) {
				continue;
			}
			String[] splitter = orderField_.split(PCMConstants.REGEXP_POINT);
			parentNameOrderBy = splitter[0];
			fieldNameOrderByArr[o_] = splitter[1];
		}
		int o_ = 0;
		if (entidadesCollection_.size() != 2) {
			throw new DatabaseException(InternalErrorsConstants.CRITERIA_BBDD_ERROR);
		}
		final Iterator<FieldViewSetCollection> collectionsIte = entidadesCollection_.iterator();
		final Collection<FieldViewSet> entidades_Criteria = (collectionsIte.next()).getFieldViewSets();
		final FieldViewSetCollection entityResult = collectionsIte.next();
		final Collection<FieldViewSet> todosLosFieldViewSet = new ArrayList<FieldViewSet>();
		todosLosFieldViewSet.addAll(entityResult.getFieldViewSets());
		todosLosFieldViewSet.addAll(entidades_Criteria);

		ResultSet resultSet = null, resultSetCount = null;
		Collection<String> leftsJoins = new ArrayList<String>();
		PreparedStatement pstmt_ = null, pstmtCount = null;
		int counterAlias = 0;
		final Map<String, Map<String, String>> fieldAliases = new HashMap<String, Map<String, String>>();
		StringBuilder sqlEntityFieldsToGet = new StringBuilder(), tablesInQuery = new StringBuilder();
		try {			
			
			final Iterator<FieldViewSet> iteTodos = todosLosFieldViewSet.iterator();
			while (iteTodos.hasNext()) {
				final FieldViewSet candidatoPadre = iteTodos.next();
				final Iterator<FieldViewSet> iteResto = SQLUtils.devolutionOfRestOfFViewSets(candidatoPadre, todosLosFieldViewSet)
						.iterator();
				while (iteResto.hasNext()) {
					FieldViewSet siguienteResto = iteResto.next();
					if (siguienteResto.getEntityDef().getParentEntities().contains(candidatoPadre.getEntityDef())) {
						String aliasTableMaster = SQLUtils.getAlias(candidatoPadre.getEntityDef().getName());
						String aliasTableDetail = SQLUtils.getAlias(siguienteResto.getEntityDef().getName());
						String aliasesInThisleftJoin = aliasTableMaster.concat(" ").concat(aliasTableDetail);
						if (!leftsJoins.contains(aliasesInThisleftJoin)) {
							if (tablesInQuery.indexOf(candidatoPadre.getEntityDef().getName()) == -1) {
								tablesInQuery.append(candidatoPadre.getEntityDef().getName()).append(PCMConstants.STRING_SPACE)
										.append(aliasTableMaster);
							}
							tablesInQuery.append(LEFT_JOIN_).append(siguienteResto.getEntityDef().getName())
									.append(PCMConstants.STRING_SPACE);
							tablesInQuery.append(aliasTableDetail).append(ON_);
							final Collection<FieldViewSet> leftJoin = new ArrayList<FieldViewSet>();
							leftJoin.add(new FieldViewSet(candidatoPadre.getEntityDef()));// master
							leftJoin.add(new FieldViewSet(siguienteResto.getEntityDef()));// detail
							tablesInQuery.append(SQLUtils.getJoin(leftJoin, null, null,null));
							leftsJoins.add(aliasesInThisleftJoin);
						}// if
					}// if
				}// while
			}// while			
			
			// el prototipo siempre es el oltimo FieldViewSetCollection
			FieldViewSetCollection prototypeResult = new FieldViewSetCollection();
			prototypeResult.getFieldViewSets().addAll((entidadesCollection_.get(entidadesCollection_.size() - 1).getFieldViewSets()));
			Iterator<FieldViewSet> iteEntitiesPattern = todosLosFieldViewSet.iterator();
			while (iteEntitiesPattern.hasNext()) {
				FieldViewSet fSetCandidate = iteEntitiesPattern.next();
				boolean esta = false;
				for (int k = 0; k < prototypeResult.getFieldViewSets().size() && !esta; k++) {
					FieldViewSet fSet = prototypeResult.getFieldViewSets().get(k);
					if (fSetCandidate.getEntityDef().getName().equals(fSet.getEntityDef().getName())) {
						esta = true;
						// miramos si hemos de incluir en fSet algon FieldView de este
						// fSetCandidate que no esto ya metido
						Iterator<IFieldView> fieldsDeCandidatoIterator = fSetCandidate.getFieldViews().iterator();
						while (fieldsDeCandidatoIterator.hasNext()) {
							IFieldView fieldViewCandidate = fieldsDeCandidatoIterator.next();
							if (fieldViewCandidate.getEntityField().isVolatile()) {
								continue;
							}
							if (fSet.getFieldView(fieldViewCandidate.getEntityField().getName()) != null) {
								Iterator<IFieldView> iteFieldViewsOfset = fSet.getFieldViews().iterator();
								List<IFieldView> nuevaListaFViews = new ArrayList<IFieldView>();
								while (iteFieldViewsOfset.hasNext()) {
									IFieldView fieldViewOfSet = iteFieldViewsOfset.next();
									if (!(fieldViewOfSet.getEntityField().getName().equals(fieldViewCandidate.getEntityField().getName()) && fieldViewOfSet
											.isRankField())) {
										nuevaListaFViews.add(fieldViewOfSet);
									}
								}// while
								nuevaListaFViews.add(fieldViewCandidate.copyOf());
								fSet.getFieldViews().clear();
								fSet.getFieldViews().addAll(nuevaListaFViews);
							} else {
								fSet.getFieldViews().add(fieldViewCandidate.copyOf());
							}
						}
					}
				}// for
				if (!esta) {
					prototypeResult.getFieldViewSets().add(fSetCandidate.copyOf());
				}
			}// while
								
			List<IEntityLogic> differentEntities = new ArrayList<IEntityLogic>();
			todosLosFieldViewSet.addAll(entidadesCollection_.get(entidadesCollection_.size() - 1).getFieldViewSets());
			final Iterator<FieldViewSet> fieldVSetsIte = todosLosFieldViewSet.iterator();
			while (fieldVSetsIte.hasNext()) {
				final FieldViewSet fieldViewSet = fieldVSetsIte.next();
				IEntityLogic entity_ = fieldViewSet.getEntityDef();
				if (!differentEntities.contains(entity_)) {
					differentEntities.add(entity_);
				}else {
					continue;
				}

				String entityName_ = entity_ != null ? entity_.getName() : "";
				String tableAlias_ = SQLUtils.getAlias(entityName_);
				
				StringBuilder idEnTableDetail_aliasTablePadre = new StringBuilder();
				Map<String, String> entityAliases = new HashMap<String, String>();
				Iterator<IFieldView> fieldViewsIte = fieldViewSet.getFieldViews().iterator();

				while (fieldViewsIte.hasNext()) {
					final IFieldView fieldView = fieldViewsIte.next();
					if (fieldView.getEntityField() == null) {
						throw new DatabaseException(
								"Este fieldView  ha sido manipulado y no trae relleno la informacion del modelo de persistencia entityField");
					}else if (fieldView.getEntityField().isVolatile())  {
						continue;
					}
					
					String fieldNameOfEntity_ = fieldView.getEntityField().getName();					
					String _aliasForField = new StringBuilder(ALIAS_).append(String.valueOf(counterAlias++)).toString();
										
					entityAliases.put(entityName_.concat(PCMConstants.POINT).concat(fieldNameOfEntity_), _aliasForField);
					if (counterAlias > 1) {
						sqlEntityFieldsToGet.append(PCMConstants.COMMA).append(PCMConstants.STRING_SPACE);
					}
					if (fieldView.isAggregate()) {
						sqlEntityFieldsToGet.append(PCMConstants.STRING_SPACE).append(PCMConstants.BEGIN_PARENTHESIS)
								.append(SQLUtils.getSQLAggregated(fieldView));
						sqlEntityFieldsToGet.append(PCMConstants.END_PARENTHESIS).append(PCMConstants.STRING_SPACE)
								.append(_aliasForField);
					} else {
						sqlEntityFieldsToGet.append(tableAlias_).append(PCMConstants.POINT).append(fieldNameOfEntity_)
								.append(PCMConstants.STRING_SPACE).append(_aliasForField);
					}
					if (offset_ != -1 && o_ < fieldNameOrderByArr.length) {
						String orderEntityName = entityName_;
						String orderFieldName = fieldView.getEntityField().getName();
						for (int c = 0; c < fieldNameOrderByArr.length; c++) {
							if (parentNameOrderBy.equals(orderEntityName) && orderFieldName.equals(fieldNameOrderByArr[c])) {
								orderFieldsAlias_[c] = _aliasForField;
							}
						}
					}
					if (tablesInQuery.indexOf(tableAlias_) == -1) {
						tablesInQuery.append(PCMConstants.COMMA).append(PCMConstants.STRING_SPACE).append(entityName_)
								.append(PCMConstants.STRING_SPACE).append(tableAlias_);
					}
					
					if (SQLUtils.fieldForeignKey(fieldView)) {
						IEntityLogic entityFK = EntityLogicFactory.getFactoryInstance().getEntityDef(this.appDomain.getResourcesConfiguration().getEntitiesDictionary(),
								fieldView.getFieldAndEntityForThisOption().getEntityFromCharge());
						
						IFieldLogic pkFieldOfParent = entityFK.getFieldKey().getPkFieldSet().iterator().next();
						int pkOrder = pkFieldOfParent.getMappingTo();
						
						int[] descrMappings = fieldView.getFieldAndEntityForThisOption().getFieldDescrMappingTo();						
						//aoado al descrMappings, el pk de esta entidad, si no estuviera ya contenido						
						int i = 0;
						boolean found = false;
						for (i = 0; i < descrMappings.length; i++) {
							if (descrMappings[i] == pkOrder){
								found = true;
								break;
							}
						}
						int[] descrMappings_ = null;
						if (!found){
							descrMappings_ = new int[descrMappings.length+1];
							for (i = 0; i < descrMappings.length; i++) {
								descrMappings_[i] = descrMappings[i];
							}
							descrMappings_[descrMappings.length] = pkOrder;
						}else{
							descrMappings_ = new int[descrMappings.length];
							for (i = 0; i < descrMappings.length; i++) {
								descrMappings_[i] = descrMappings[i];
							}
						}						
						
						String entityNameFK = fieldView.getFieldAndEntityForThisOption().getEntityFromCharge();
						String aliasTablePadre = SQLUtils.getAlias(entityNameFK);						
						Map<String, String> entityFKAliases = new HashMap<String, String>();						
						for (i = 0; i < descrMappings_.length; i++) {
							IFieldLogic fieldFK = entityFK.searchField(descrMappings_[i]);
							if (fieldFK.isVolatile())  {
								continue;
							}
							String fieldNameFK = fieldFK.getName();
							
							String aliasForField_FK = new StringBuilder(ALIAS_).append(String.valueOf(counterAlias++)).toString();							
							FieldViewSet tableDetail = new FieldViewSet(fieldView.getEntityField().getEntityDef());
							String aliasTableDetail = SQLUtils.getAlias(tableDetail.getEntityDef().getName());
							
							if (!idEnTableDetail_aliasTablePadre.toString().equals("")){
								boolean added2Prototype = false;
								//procesamos cada idCampoFK concatenado con su tabla FK_Padre
								String idFKIesimo = fieldView.getEntityField().getName();
								String[] idFK_tablaPadre_arr = idEnTableDetail_aliasTablePadre.toString().split(";");
								for (int k=idFK_tablaPadre_arr.length-1;k>=0;k--){
									String idFK_tablaPadre = idFK_tablaPadre_arr[k];
									String idFK = idFK_tablaPadre.split(":")[0];
									String tablaPadre = idFK_tablaPadre.split(":")[1];
									if (tablaPadre.equals(aliasTablePadre) && idFKIesimo.equals(idFK)){
										break;
									}else if (!idFKIesimo.equals(idFK) && tablaPadre.equals(aliasTablePadre)){
										//aoadimos esta entidad al prototipo, dado que se trata de otra ocurrencia de relacion entre estas dos entidades
										prototypeResult.getFieldViewSets().add(new FieldViewSet(entityFK));
										added2Prototype = true;
										aliasTablePadre = aliasTablePadre.concat(String.valueOf(i));
										break;
									} else if (idFKIesimo.equals(idFK) && tablaPadre.contains(aliasTablePadre)){
										aliasTablePadre = tablaPadre;
										break;
									}
								}
								if (!added2Prototype && !idEnTableDetail_aliasTablePadre.toString().contains(aliasTablePadre)){
									prototypeResult.getFieldViewSets().add(new FieldViewSet(entityFK));
								}
							}else{
								//aoadimos esta entidad al prototipo
								prototypeResult.getFieldViewSets().add(new FieldViewSet(entityFK));
							}

							entityFKAliases.put(entityFK.getName().concat(PCMConstants.POINT).concat(fieldNameFK), aliasForField_FK);
							if (counterAlias > 1) {
								sqlEntityFieldsToGet.append(PCMConstants.COMMA).append(PCMConstants.STRING_SPACE);
							}
							sqlEntityFieldsToGet.append(aliasTablePadre).append(PCMConstants.POINT).append(fieldNameFK)
									.append(PCMConstants.STRING_SPACE).append(aliasForField_FK);
							String aliasesInThisleftJoin = aliasTablePadre.concat(" ").concat(aliasTableDetail);
							if (!leftsJoins.contains(aliasesInThisleftJoin) && !aliasTablePadre.equals(aliasTableDetail)) {
								tablesInQuery.append(LEFT_JOIN_).append(entityFK.getName()).append(PCMConstants.STRING_SPACE)
										.append(aliasTablePadre).append(ON_);
								final Collection<FieldViewSet> leftJoin = new ArrayList<FieldViewSet>();
								leftJoin.add(new FieldViewSet(entityFK));// master
								leftJoin.add(tableDetail);// detail
								tablesInQuery.append(SQLUtils.getJoin(leftJoin, aliasTablePadre, null, fieldView.getEntityField()));
								leftsJoins.add(aliasesInThisleftJoin);
								idEnTableDetail_aliasTablePadre.append(fieldView.getEntityField().getName()+ ":" + aliasTablePadre);
								idEnTableDetail_aliasTablePadre.append(";");
							}
							if (offset_ != -1 && o_ < fieldNameOrderByArr.length) {
								String orderEntityName = tableDetail.getEntityDef().getName();
								String orderFieldName = fieldView.getEntityField().getName();
								for (int c = 0; c < fieldNameOrderByArr.length; c++) {
									if (parentNameOrderBy.equals(orderEntityName) && orderFieldName.equals(fieldNameOrderByArr[c])) {
										orderFieldsAlias_[o_++] = aliasForField_FK;
									} else if (parentNameOrderBy.equals(entityNameFK) && fieldNameOrderByArr[c].equals(fieldNameFK)) {
										orderFieldsAlias_[o_++] = aliasForField_FK;
									}
								}
							}// if
							String claveInMapOfAlias = aliasTablePadre.concat("|").concat(entityNameFK);
							if (fieldAliases.get(claveInMapOfAlias) == null && !entityFKAliases.isEmpty()) {
								fieldAliases.put(claveInMapOfAlias, entityFKAliases);
								entityFKAliases = new HashMap<String, String>();
							} else if (!entityFKAliases.isEmpty()) {
								Map<String, String> fieldsOfThisEntity = fieldAliases.get(claveInMapOfAlias);
								fieldsOfThisEntity.putAll(entityFKAliases);
								entityFKAliases = new HashMap<String, String>();
							}
						}// for
						Map<String, String> entryFK_in_MasterEntity = new HashMap<String, String>();
						entryFK_in_MasterEntity.put(entityName_.concat(PCMConstants.POINT).concat(fieldNameOfEntity_), _aliasForField);

					} // else
				}// while fields iteration
				String claveInMapOfAlias = tableAlias_.concat("|").concat(entityName_);
				if (fieldAliases.get(claveInMapOfAlias) == null && entityAliases != null && !entityAliases.isEmpty()) {
					fieldAliases.put(claveInMapOfAlias, entityAliases);
				} else if (entityAliases != null && !entityAliases.isEmpty()) {
					Map<String, String> fieldsOfThisEntity = fieldAliases.get(claveInMapOfAlias);
					fieldsOfThisEntity.putAll(entityAliases);
				}
			}// while

			int nArgs_ = 0;
			final StringBuilder sqlWhereClausule = new StringBuilder();
			final Collection<Map<String, Serializable>> sqlParameters = new ArrayList<Map<String, Serializable>>();
			final Iterator<FieldViewSet> fViewSetsCritYProtoIte = todosLosFieldViewSet.iterator();
			while (fViewSetsCritYProtoIte.hasNext()) {
				final FieldViewSet fieldViewSetCriteria = fViewSetsCritYProtoIte.next();
				final Map<String, Integer> whereForEntityAndArgs = this.getEntityFilter(fieldViewSetCriteria, sqlParameters,
						this.auditActivated, this.auditFieldSet);
				final Map.Entry<String, Integer> entryWhereForEntity = whereForEntityAndArgs.entrySet().iterator().next();
				final String whereForEntity = entryWhereForEntity.getKey();
				nArgs_ += entryWhereForEntity.getValue().intValue();
				sqlWhereClausule.append(sqlWhereClausule.length() > 0 && !whereForEntity.equals(PCMConstants.EMPTY_) ? IDAOImpl.AND_
						: PCMConstants.EMPTY_);
				sqlWhereClausule.append(whereForEntity);
			}
			String sql_ = null, sql_withoutPagination = PCMConstants.EMPTY_;
			if (offset_ != -1) {
				
				sql_ = SQLUtils.replaceSQLWithPagination(this.getQueryPagination(offset_), sqlEntityFieldsToGet.toString(),
						FROM_.concat(tablesInQuery.toString()), sqlWhereClausule.toString(), orderFieldsAlias_, orderDirec_);
				if (!hasCounterSQLEmbbeded()) {
					sql_withoutPagination = SQLUtils.replaceSQLWithoutPagination(this.getQueryPagination(-1),
							sqlEntityFieldsToGet.toString(), FROM_.concat(tablesInQuery.toString()), sqlWhereClausule.toString());
					pstmtCount = conn.prepareStatement("SELECT COUNT(*) FROM (".concat(sql_withoutPagination).concat(") v"));
				}
			} else {
				sql_ = SQLUtils.replaceSQLWithoutPagination(this.getQueryPagination(offset_), sqlEntityFieldsToGet.toString(),
						FROM_.concat(tablesInQuery.toString()), sqlWhereClausule.toString());
				sql_withoutPagination = sql_;
			}

			//System.out.println("\nSQL.: ".concat(sql_));

			boolean isLocked = true;
			while (isLocked) {
				try {
					pstmt_ = conn.prepareStatement(sql_);
					isLocked = false;
				}
				catch (SQLException iSqlExc1) {
					if (conn.isResourceLocked(iSqlExc1)) {
						Thread.sleep(500);
					} else {
						iSqlExc1.printStackTrace();
						throw iSqlExc1;
					}
				}
			}

			int contadorArgumentos = 0;
			final Iterator<Map<String, Serializable>> iteratorValuesFilter = sqlParameters.iterator();
			while (iteratorValuesFilter.hasNext()) {
				final Map<String, Serializable> valueOfWhere = iteratorValuesFilter.next();
				if (valueOfWhere.keySet().iterator().hasNext()) {
					final String fieldTypeAndMaxLengthkey = valueOfWhere.keySet().iterator().next();
					final String[] fieldTypeAndMaxLength = fieldTypeAndMaxLengthkey.split(",");
					final String fieldType = fieldTypeAndMaxLength[0];
					//final int fieldMaxlength = Integer.parseInt(fieldTypeAndMaxLength[1]);
					if (new FieldLogic(fieldType).isVolatile()) {
						continue;
					}
					IFieldAbstract fieldAbstract = new FieldLogic(fieldType).getAbstractField();
					String val_ = valueOfWhere.get(fieldTypeAndMaxLengthkey).toString();
					if (!PCMConstants.EMPTY_.equals(val_) && !PCMConstants.CLASSIC_SEPARATOR.equals(val_)) {
						if (!fieldAbstract.isBlob()) {
							if (fieldAbstract.isString()) {
								val_ = val_.replaceAll(PCMConstants.ASTERISC_SCAPED, PCMConstants.PERCENTAGE_SCAPED);
								pstmt_.setObject(contadorArgumentos + 1, val_);
								if (!hasCounterSQLEmbbeded() && offset_ != -1) {
									pstmtCount.setObject(contadorArgumentos + 1, val_);
								} else if (hasCounterSQLEmbbeded() && hasDuplicatedCriteriaInEmbbededCounterSQL() && offset_ != -1) {
									pstmt_.setObject(((contadorArgumentos + 1) + nArgs_), val_);
								}
							} else if (fieldAbstract.isBoolean()) {//OJO: que puede venir un 1...
								pstmt_.setInt(contadorArgumentos + 1, Integer.valueOf(val_).intValue());
								if (!hasCounterSQLEmbbeded() && offset_ != -1) {
									pstmt_.setInt(contadorArgumentos + 1, Integer.valueOf(val_).intValue());
								} else if (hasCounterSQLEmbbeded() && hasDuplicatedCriteriaInEmbbededCounterSQL() && offset_ != -1) {
									pstmt_.setInt(((contadorArgumentos + 1) + nArgs_), Integer.valueOf(val_).intValue());
								}								
								
							} else if (fieldAbstract.isDate() || fieldAbstract.isTimestamp()) {
								if (!PCMConstants.EMPTY_.equals(val_) && val_.length() > 10) {
									java.sql.Date date_SQ = new java.sql.Date(CommonUtils.myDateFormatter.parse(val_).getTime());
									pstmt_.setDate(contadorArgumentos + 1, date_SQ);
									if (!hasCounterSQLEmbbeded() && offset_ != -1) {
										pstmtCount.setDate(contadorArgumentos + 1, date_SQ);
									} else if (hasCounterSQLEmbbeded() && hasDuplicatedCriteriaInEmbbededCounterSQL() && offset_ != -1) {
										pstmt_.setDate(((contadorArgumentos + 1) + nArgs_), date_SQ);
									}
								} else if (!PCMConstants.EMPTY_.equals(val_) && val_.length() <= 10) {
									java.sql.Date date_SQ = new java.sql.Date(CommonUtils.myDateFormatter.parse(val_).getTime());
									pstmt_.setDate(contadorArgumentos + 1, date_SQ);
									if (!hasCounterSQLEmbbeded() && offset_ != -1) {
										pstmtCount.setDate(contadorArgumentos + 1, date_SQ);
									} else if (hasCounterSQLEmbbeded() && hasDuplicatedCriteriaInEmbbededCounterSQL() && offset_ != -1) {
										pstmt_.setDate(((contadorArgumentos + 1) + nArgs_), date_SQ);
									}
								}
							} else {
								pstmt_.setObject(contadorArgumentos + 1, val_);
								if (!hasCounterSQLEmbbeded() && offset_ != -1) {
									pstmtCount.setObject(contadorArgumentos + 1, val_);
								} else if (hasCounterSQLEmbbeded() && hasDuplicatedCriteriaInEmbbededCounterSQL() && offset_ != -1) {
									pstmt_.setObject(((contadorArgumentos + 1) + nArgs_), val_);
								}
							}
						} else {
							if (PCMConstants.EMPTY_.equals(val_)) {
								pstmt_.setBinaryStream(contadorArgumentos + 1, null, 0);
								if (!hasCounterSQLEmbbeded() && offset_ != -1) {
									// nothing
								}
							} else {
								final byte[] binary_ = CommonUtils.getPrimitiveByteArrayFromUploadedFile(new File(val_.toString()));
								if (binary_ != null) {
									pstmtCount.setBytes(contadorArgumentos + 1, binary_);
								}
							}
						}
						contadorArgumentos++;
					}// si viaja no vacoo ni nulo
				}
			}
			int tamPaginacion = tamPaginacion_ == -1 ? Integer.MAX_VALUE : tamPaginacion_;
			if (hasCounterSQLEmbbeded() && offset_ != -1) {
				final int formulaLimiteMin = ((offset_ - 1) * tamPaginacion), formulaLimiteMax = ((offset_) * tamPaginacion);
				if (hasCounterSQLEmbbeded() && hasDuplicatedCriteriaInEmbbededCounterSQL() && offset_ != -1) {
					pstmt_.setInt((nArgs_ * 2) + 1, isUpperLimitBefore() ? formulaLimiteMax : formulaLimiteMin);
					pstmt_.setInt((nArgs_ * 2) + 2, tamPaginacion);
				} else {
					pstmt_.setInt((contadorArgumentos + 1), isUpperLimitBefore() ? formulaLimiteMax : formulaLimiteMin);
					pstmt_.setInt((contadorArgumentos + 2), tamPaginacion);
				}
			}

			resultSet = pstmt_.executeQuery();
			int rowCont_ = 0;
			long ultimaPos = -1;
			while (resultSet.next()) {
				rowCont_++;
				if (hasCounterSQLEmbbeded() && offset_ != -1 && ultimaPos == -1) {
					ultimaPos = resultSet.getLong(TOTALREG);
				}
				if (firstAndLastOnly_ && rowCont_ != 1 && rowCont_ != ultimaPos) {
					continue;
				}

				final FieldViewSetCollection row = getRow(resultSet, prototypeResult, fieldAliases, sqlEntityFieldsToGet);
				row.setRowPosition(rowCont_);
				if (hasCounterSQLEmbbeded() && offset_ != -1 && rowCont_ == 1) {
					row.setTotalRecords(resultSet.getLong(TOTALREG));
				}
				resultado.add(row);
				if (rowCont_ == tamPaginacion) {
					break;
				}				

			}// while resultados consulta

			if (!hasCounterSQLEmbbeded() && resultado.size() > 0) {
				try {
					resultSetCount = pstmtCount.executeQuery();
					if (resultSetCount.next()) {
						resultado.iterator().next().setTotalRecords(resultSetCount.getLong(1));
					}
				}
				catch (final SQLException sqlExcc) {// nullpointer or sqlexception
					if (conn.isClosed()) {
						throw new DatabaseException(InternalErrorsConstants.BBDD_CONNECT_EXCEPTION, sqlExcc);
					}
					throw new SQLException(sqlExcc.getMessage().concat("error al recoger el total de la consulta de count(*)"));
				} finally {
					if (resultSetCount != null) {
						resultSetCount.close();
					}
					if (pstmtCount != null) {
						pstmtCount.close();
					}
				}
			}
		}
		catch (final SQLException sqlExc) {
			try {
				if (conn.isClosed()) {
					throw new DatabaseException(InternalErrorsConstants.BBDD_CONNECT_EXCEPTION, sqlExc);
				}
				throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, sqlExc);
			}
			catch (final Throwable innerSql) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, innerSql);
			}
		}
		catch (final PCMConfigurationException cfgExc) {
			try {
				throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, cfgExc);
			}
			catch (final Throwable innerSql) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, innerSql);
			}
		}
		catch (final NullPointerException nullExc) {
			try {
				throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, nullExc);
			}
			catch (final Throwable innerSql) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, innerSql);
			}
		}
		catch (final DatabaseException dataComposeExc1) {
			try {
				throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, dataComposeExc1);
			}
			catch (final Throwable innerSql) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_QUERY_EXCEPTION, innerSql);
			}
		}
		catch (final Exception formatExc) {
			throw new DatabaseException(formatExc);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pstmt_ != null) {
					pstmt_.close();
				}
			}
			catch (final Throwable ezc) {
				throw new DatabaseException(InternalErrorsConstants.BBDD_FREE_EXCEPTION, ezc);
			}
		}
		if (firstAndLastOnly_ && resultado.size() == 1) {
			resultado.add(resultado.get(0));
		}
		return resultado;
	}

	
	
	
	private final FieldViewSetCollection getRow(final ResultSet resultSet, final FieldViewSetCollection prototypeResult,
			final Map<String, Map<String, String>> fieldAliases_, final StringBuilder sqlEntityFieldsToGet) throws DatabaseException, ParseException {
		
		Collection<String> descartes = new ArrayList<String>();
		
		final FieldViewSetCollection row = prototypeResult.copyOf();
		final Iterator<FieldViewSet> iterador = row.getFieldViewSets().iterator();
		while (iterador.hasNext()) {
			final FieldViewSet fieldSet2Fill = iterador.next();
			
			final Map<String,Map<String, String>> aliasFieldEntryMap = SQLUtils.searchAliasFields(fieldAliases_, fieldSet2Fill.getEntityDef().getName(), descartes);
			Map.Entry<String,Map<String, String>> entryOfMap = aliasFieldEntryMap.entrySet().iterator().next();
			String key = entryOfMap.getKey().split("\\|")[0];
			Map<String, String> aliasFieldMap = entryOfMap.getValue();
			
			//Importante: lo aoado a la lista de descartes para que el motodo que busca los conjuntos de alias de una entidad no lo vuelva a retornar
			descartes.add(key);
			
			final Iterator<Map.Entry<String, String>> iteradorDeAlias = aliasFieldMap.entrySet().iterator();
			
			while (iteradorDeAlias.hasNext()) {
				Map.Entry<String, String> entry = iteradorDeAlias.next();
				String alias = entry.getValue();
				if (sqlEntityFieldsToGet.toString().indexOf(" ".concat(alias==null?"":alias)) == -1) {
					continue;
				}
				String fieldKey = entry.getKey();
				IFieldLogic fieldLogic = fieldSet2Fill.getEntityDef().searchByName(fieldKey);
				if (fieldLogic.isVolatile()) {
					continue;
				}
				IFieldView fieldView = fieldSet2Fill.getFieldView(fieldLogic.getName());
				if (fieldView == null || 
						(fieldSet2Fill.getValue(fieldView.getQualifiedContextName()) != null && 
							!"".equals(fieldSet2Fill.getValue(fieldView.getQualifiedContextName()).toString()))) {
					continue;
				}
				if (fieldView.isRankField()) {
					fieldView = new FieldView(fieldLogic);
				}
				try {
					if (alias != null) {
						try{
							 resultSet.findColumn(alias);
						}catch (SQLException notFoundColumn){
							continue;
						}
						IFieldAbstract fieldAbstract = fieldLogic.getAbstractField();
						Serializable finalresult = null;
						if (fieldAbstract.isDate() || fieldAbstract.isTimestamp()) {
							finalresult = getTimestamp(resultSet, alias);
						} else if (fieldAbstract.isLong()) {
							if ( resultSet.getObject(alias)!= null && !"".contentEquals(resultSet.getString(alias))) {
								finalresult = Long.valueOf(resultSet.getString(alias));
							}
						} else if (fieldAbstract.isBoolean()) {
							Object val_ = resultSet.getObject(alias);
							finalresult = "true".equals(val_.toString().toLowerCase()) || "1".equals(val_.toString()) ? Boolean.TRUE : Boolean.FALSE;
						} else {						
							finalresult = (Serializable) resultSet.getObject(alias);
						}
						fieldSet2Fill.setValue(fieldView.getQualifiedContextName(), finalresult == null ? "" : finalresult);
					}
				}
				catch (Throwable sqlExcc) {
					AnsiSQLAbstractDAOImpl.log.log(Level.SEVERE, "ERROR al obtener campo..." + fieldLogic.getName(), sqlExcc);
					throw new DatabaseException("Error al obtener campo..." + fieldLogic.getName() + " de la query");
				}
			}
		}// while iterador de campos
		return row;
	}

	@Override
	protected String getLeftPartPreffixForString(String entityName, String fieldName) {
		StringBuilder leftPartOfSqlExpression = new StringBuilder("rtrim(ltrim(");
		leftPartOfSqlExpression.append(SQLUtils.getAlias(entityName)).append(PCMConstants.POINT).append(fieldName);
		leftPartOfSqlExpression.append("))");
		return leftPartOfSqlExpression.toString();
	}

}
