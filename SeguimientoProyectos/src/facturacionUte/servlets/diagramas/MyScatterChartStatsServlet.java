package facturacionUte.servlets.diagramas;


import cdd.data.bus.Data;
import cdd.logicmodel.definitions.IFieldLogic;
import cdd.webapp.stats.graphs.GenericScatterChartServlet;
import facturacionUte.common.UnitsForFields;

public class MyScatterChartStatsServlet extends GenericScatterChartServlet {

	private static final long serialVersionUID = 1589009998989444444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final Data data_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, data_);
	}
	

}
