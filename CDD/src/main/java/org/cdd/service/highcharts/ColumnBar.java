package org.cdd.service.highcharts;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.Translator;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.dto.Datamap;
import org.cdd.service.highcharts.utils.HistogramUtils;
import org.json.simple.JSONArray;

public class ColumnBar extends BarChart {
	
	private String typeOfgraph = "column";
	
	public ColumnBar() {
		this.typeOfgraph = "column";
	}
	
	public ColumnBar(final String typeOfgraph_) {
		this.typeOfgraph = typeOfgraph_;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, String> generateJSON(final List<Map<FieldViewSet, Map<String,Double>>> listaValoresAgregados, final Datamap data_,
			final FieldViewSet filtro_, final IFieldLogic[] agregados, final IFieldLogic[] fieldsCategoriaDeAgrupacion, 
			final IFieldLogic orderBy, final String aggregateFunction) {
		
		if (fieldsCategoriaDeAgrupacion.length> 1) {
			throw new RuntimeException("No hay categoría de agrupación definida");
		}
		String lang = data_.getLanguage();
		Double acumuladorTotalPointsTotal = 0.0;
		Map<Serializable, Double> totalizacionbarrasHoriz = new HashMap<Serializable, Double>(listaValoresAgregados.size());
		
		final Map<String, Map<String, Number>> registros = new HashMap<String, Map<String, Number>>();
		JSONArray jsArrayEjeAbcisas = new JSONArray();
		for (int k=0;k<listaValoresAgregados.size();k++) {
			Map<FieldViewSet, Map<String,Double>> serieIesima = listaValoresAgregados.get(k);
			FieldViewSet filtroConValorAgrupacion = serieIesima.keySet().iterator().next();
			Serializable valorPorElQueagrupamos = filtroConValorAgrupacion.getValue(fieldsCategoriaDeAgrupacion[0].getMappingTo());
			
			if (fieldsCategoriaDeAgrupacion[0].getParentFieldEntities() != null && !fieldsCategoriaDeAgrupacion[0].getParentFieldEntities().isEmpty()){
				IFieldLogic fieldLogicAssociated = fieldsCategoriaDeAgrupacion[0].getParentFieldEntities().get(0);
				FieldViewSet fSetParent = new FieldViewSet(fieldLogicAssociated.getEntityDef());
				fSetParent.setValue(fieldLogicAssociated.getEntityDef().getFieldKey().getPkFieldSet().iterator().next().getMappingTo(), valorPorElQueagrupamos);
				try {
					fSetParent = this._dataAccess.searchEntityByPk(fSetParent);
					if (fSetParent != null) {
						IFieldLogic descField = fSetParent.getDescriptionField();
						valorPorElQueagrupamos = fSetParent.getValue(descField.getMappingTo()).toString();
					}
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			}
			double totalSerie = 0.0;
			int numTuplas = 0;
			Map<String, Number> newDuplas = new HashMap<String, Number>();
			Collection<Map<String,Double>> coleccionFirstDeAgregados = serieIesima.values();
			Map<String,Double> entryMap = coleccionFirstDeAgregados.iterator().next();
			Iterator<Map.Entry<String,Double>> iteratorDuplas = entryMap.entrySet().iterator();
			while (iteratorDuplas.hasNext()){
				Map.Entry<String,Double> dupla = iteratorDuplas.next();
				String dimensionName = dupla.getKey();
				Double value = dupla.getValue();
				dimensionName = Translator.traduceDictionaryModelDefined(data_.getLanguage(), filtro_.getEntityDef().getName().concat(".").concat(dimensionName));
				if (!jsArrayEjeAbcisas.contains(dimensionName)) {
					jsArrayEjeAbcisas.add(dimensionName);
				}
				newDuplas.put(dimensionName, value);
				totalSerie += value;
				numTuplas++;
			}
			double valor =  aggregateFunction.contentEquals(OPERATION_AVERAGE) ? (totalSerie == 0 ? 0 : totalSerie/numTuplas) : totalSerie;
			acumuladorTotalPointsTotal += valor;
			totalizacionbarrasHoriz.put(valorPorElQueagrupamos.toString(), valor);
			
			registros.put(valorPorElQueagrupamos.toString(), newDuplas);
		}
		
		String agrupadoPor = Translator.traduceDictionaryModelDefined(data_.getLanguage(), filtro_.getEntityDef().getName().concat(".").concat(fieldsCategoriaDeAgrupacion[0].getName()));
		String itemGrafico = Translator.traduceDictionaryModelDefined(data_.getLanguage(), filtro_.getEntityDef().getName().concat(".").concat(filtro_.getEntityDef().getName()));
		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(JSON_OBJECT), regenerarListasSucesos(fieldsCategoriaDeAgrupacion[0].getEntityDef().getName(), registros, jsArrayEjeAbcisas, false/*stack_Z*/, data_));
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CATEGORIES), jsArrayEjeAbcisas.toJSONString());
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("agrupadoPor"), agrupadoPor);		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(CHART_TITLE), getTitle(data_, itemGrafico));		
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat(IS_BAR_INTERNAL_LABELED), "false");
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("minEjeRef"), 0.0);
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("typeOfgraph"), this.typeOfgraph);
		String visionado = data_.getParameter(filtro_.getNameSpace().concat(".").concat(HistogramUtils.VISIONADO_PARAM));
		data_.setAttribute(data_.getParameter("idPressed")+getScreenRendername().concat("visionado"), visionado==null?"2D": visionado);
		
		double promedioPorCategoria = CommonUtils.roundWith2Decimals(acumuladorTotalPointsTotal/(listaValoresAgregados.size()));
		//acumuladorTotalPointsTotal = getTotal(listaValoresAgregados);
		//System.out.println("COLUMNBAR: PROMEDIO SE HA CALCULADO EN BASE A LOS DATOS COMO : " +  acumuladorTotalPoints + "/(" + listaValoresAgregados.size() + " series) ==> " + promedioTotal );
		String txtPromedio = "promedio por ";
		if (fieldsCategoriaDeAgrupacion != null) {
			String nameOfCategory = "";
			IFieldLogic agrupacion_ = fieldsCategoriaDeAgrupacion[0];
			if (agrupacion_ != null) {
				nameOfCategory = Translator.traduceDictionaryModelDefined(lang, agrupacion_.getName());
			}
			txtPromedio += CommonUtils.singularOfterm(nameOfCategory);
		}
		txtPromedio += ": <b>" + CommonUtils.numberFormatter.format(promedioPorCategoria) + "</b>";
		
		String txtTotal = "total para todas las categorías: <b>" + CommonUtils.numberFormatter.format(acumuladorTotalPointsTotal) + "</b>";
		Map<String, String> retorno = new HashMap<String, String>();
		retorno.put(txtPromedio, txtTotal);
		return retorno;
	}
	
	protected String getTitle(final Datamap data_, String itemGrafico) {
		return CommonUtils.obtenerPlural(itemGrafico);
	}
	
	@Override
	public String getScreenRendername() {
		
		return "columnbar";
	}
	
	
}
