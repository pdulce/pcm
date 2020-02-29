package facturacionUte.servlets.diagramas;

import cdd.comunication.dispatcher.RequestWrapper;
import cdd.comunication.dispatcher.stats.graphs.GenericPieChartServlet;
import cdd.logicmodel.definitions.IFieldLogic;
import facturacionUte.common.UnitsForFields;

public class MyPieChartStatsServlet extends GenericPieChartServlet {

	private static final long serialVersionUID = 158971895179444444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final RequestWrapper request_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, request_);
	}

}
