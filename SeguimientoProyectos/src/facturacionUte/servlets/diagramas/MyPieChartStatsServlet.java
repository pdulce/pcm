package facturacionUte.servlets.diagramas;

import cdd.domain.dataccess.definitions.IFieldLogic;
import cdd.dto.Data;
import cdd.webapp.stats.graphs.GenericPieChartServlet;
import facturacionUte.common.UnitsForFields;

public class MyPieChartStatsServlet extends GenericPieChartServlet {

	private static final long serialVersionUID = 158971895179444444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final Data data_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, data_);
	}

}
