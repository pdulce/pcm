package facturacionUte.strategies.concursos;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.common.utils.CommonUtils;
import cdd.comunication.dispatcher.RequestWrapper;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.definitions.IEntityLogic;
import cdd.logicmodel.factory.EntityLogicFactory;
import cdd.strategies.DefaultStrategyRequest;
import cdd.viewmodel.definitions.FieldViewSet;


import facturacionUte.common.ConstantesModelo;

public class StrategyControlAgregadosConcurso extends DefaultStrategyRequest {

	// errores para controlar los agregados de la entidad concurso
	public static final String ERR_SUMA_RECURSOS_ = "ERR_SUMA_RECURSOS_", ERR_SUMA_HORAS_POR_TIPO_RECURSO_ = "ERR_SUMA_HORAS_POR_TIPO_RECURSO_",
			ERR_SUMA_IMPORTES_SIN_IVA_DE_RECURSOS_EJERCICIOS__ = "ERR_SUMA_IMPORTES_SIN_IVA_DE_RECURSOS_EJERCICIOS__",
			ERR_SUMA_IMPORTES_CON_IVA_DE_RECURSOS_EJERCICIOS__ = "ERR_SUMA_IMPORTES_CON_IVA_DE_RECURSOS_EJERCICIOS__";

	// errores para controlar los agregados de la entidad datos_economicos_contrato
	public static final String ERR_SUMA_HORAS_POR_EJERCICIO_ = "ERR_SUMA_HORAS_POR_EJERCICIO_",

	ERR_SUMA_IMPORTES_POR_EJERCICIO_ = "ERR_SUMA_IMPORTES_POR_EJERCICIO_", ERR_SUMA_IMPORTES_TOTAL_ = "ERR_SUMA_IMPORTES_TOTAL_";

	@Override
	public void doBussinessStrategy(final RequestWrapper req_, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		comprobarAgregadosConcurso(req_, dataAccess, fieldViewSets);
	}
		
	public void comprobarAgregadosConcurso(final RequestWrapper req_, final IDataAccess dataAccess_, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
	PCMConfigurationException {
		FieldViewSet datosConcursoRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosConcursoRequest = iteFieldSets.next();
		}
		if (datosConcursoRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de request es nulo", new Exception("null object"));
		}
		String lang = CommonUtils.getEntitiesDictionary(req_);

		try {
			final IEntityLogic concursoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.CONCURSO_ENTIDAD);

			int recursos_por_categoria_C_int = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_7_RECURSOS_POR_CATEGORIA_C).getName())).intValue();
			int recursos_por_categoria_CJ_int = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_8_RECURSOS_POR_CATEGORIA_CJ).getName())).intValue();
			int recursos_por_categoria_AF_int = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_9_RECURSOS_POR_CATEGORIA_AF).getName())).intValue();
			int recursos_por_categoria_AP_int = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_10_RECURSOS_POR_CATEGORIA_AP).getName())).intValue();
			int recursos_totales_int = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_11_RECURSOS_TOTALES).getName())).intValue();

			int suma_calculada_recursos = (recursos_por_categoria_C_int + recursos_por_categoria_CJ_int + recursos_por_categoria_AF_int + recursos_por_categoria_AP_int);
			if (recursos_totales_int != suma_calculada_recursos) {
				final Collection<Object> messageArguments = new ArrayList<Object>();
				messageArguments.add(String.valueOf(suma_calculada_recursos));
				messageArguments.add(String.valueOf(recursos_totales_int));
				throw new StrategyException(ERR_SUMA_RECURSOS_, messageArguments);
			}

			double horas_por_categoria_C_int = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_12_HORAS_TOTALES_POR_CATEGORIA_C).getName())).doubleValue();
			double horas_por_categoria_CJ_int = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_13_HORAS_TOTALES_POR_CATEGORIA_CJ).getName())).doubleValue();
			double horas_por_categoria_AF_int = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_14_HORAS_TOTALES_POR_CATEGORIA_AF).getName())).doubleValue();
			double horas_por_categoria_AP_int = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_15_HORAS_TOTALES_POR_CATEGORIA_AP).getName())).doubleValue();
			double horas_totales_int = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_16_HORAS_TOTALES).getName())).doubleValue();
			double suma_calculada_horas = (horas_por_categoria_C_int + horas_por_categoria_CJ_int + horas_por_categoria_AF_int + horas_por_categoria_AP_int);
			if (new BigDecimal(horas_totales_int).compareTo(new BigDecimal(suma_calculada_horas)) != 0) {
				final Collection<Object> messageArguments = new ArrayList<Object>();
				messageArguments.add(String.valueOf(suma_calculada_horas));
				messageArguments.add(String.valueOf(horas_totales_int));
				throw new StrategyException(ERR_SUMA_HORAS_POR_TIPO_RECURSO_, messageArguments);
			}

			double importe_por_categoria_C_double = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_17_IMPORTE_POR_CATEGORIA_C_SIN_IVA).getName())).doubleValue();
			double importe_por_categoria_CJ_double = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_18_IMPORTE_POR_CATEGORIA_CJ_SIN_IVA).getName())).doubleValue();
			double importe_por_categoria_AF_double = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_19_IMPORTE_POR_CATEGORIA_AF_SIN_IVA).getName())).doubleValue();
			double importe_por_categoria_AP_double = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_20_IMPORTE_POR_CATEGORIA_AP_SIN_IVA).getName())).doubleValue();
			double importe_total_SIN_IVA_double = CommonUtils.numberFormatter.parse(
					datosConcursoRequest.getValue(concursoEntidad.searchField(ConstantesModelo.CONCURSO_21_IMPORTE_TOTAL_SIN_IVA).getName())).doubleValue();

			double suma_calculada_importes_sin_IVA = (importe_por_categoria_C_double + importe_por_categoria_CJ_double + importe_por_categoria_AF_double + importe_por_categoria_AP_double);
			double difference = Math.abs(Double.valueOf(importe_total_SIN_IVA_double).doubleValue() - Double.valueOf(suma_calculada_importes_sin_IVA).doubleValue());
			if (difference > 0.009) {
				final Collection<Object> messageArguments = new ArrayList<Object>();
				messageArguments.add(CommonUtils.numberFormatter.format(new BigDecimal(suma_calculada_importes_sin_IVA)));
				messageArguments.add(CommonUtils.numberFormatter.format(new BigDecimal(importe_total_SIN_IVA_double)));
				throw new StrategyException(ERR_SUMA_IMPORTES_SIN_IVA_DE_RECURSOS_EJERCICIOS__, messageArguments);
			}

		} catch (ParseException e) {
			throw new PCMConfigurationException("error", e);
		}

	}
		

	@Override
	protected void validParameters(RequestWrapper req) throws StrategyException {
		// OK
	}

}
