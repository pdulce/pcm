package facturacionUte.servlets.diagramas;


import domain.dataccess.definitions.IFieldLogic;
import domain.dataccess.dto.Data;
import facturacionUte.common.UnitsForFields;
import webservlet.stats.graphs.GenericScatterChartServlet;

public class MyScatterChartStatsServlet extends GenericScatterChartServlet {

	private static final long serialVersionUID = 1589009998989444444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final Data data_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, data_);
	}
	

}
