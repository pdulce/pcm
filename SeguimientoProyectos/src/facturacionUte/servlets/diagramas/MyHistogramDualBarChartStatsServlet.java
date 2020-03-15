package facturacionUte.servlets.diagramas;

import cdd.domain.entitymodel.definitions.IFieldLogic;
import cdd.dto.Data;
import cdd.webapp.stats.graphs.GenericHistogramFreqChartServlet;
import facturacionUte.common.UnitsForFields;

public class MyHistogramDualBarChartStatsServlet extends GenericHistogramFreqChartServlet {

	private static final long serialVersionUID = 158971895179444444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final Data data_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, data_);
	}

}
