package facturacionUte.servlets.diagramas;


import pcm.comunication.dispatcher.RequestWrapper;
import pcm.comunication.dispatcher.stats.graphs.GenericScatterChartServlet;
import pcm.context.logicmodel.definitions.IFieldLogic;
import facturacionUte.common.UnitsForFields;

public class MyScatterChartStatsServlet extends GenericScatterChartServlet {

	private static final long serialVersionUID = 1589009998989444444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final RequestWrapper request_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, request_);
	}
	

}
