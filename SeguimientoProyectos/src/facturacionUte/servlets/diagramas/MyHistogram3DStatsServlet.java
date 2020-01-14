package facturacionUte.servlets.diagramas;

import pcm.comunication.dispatcher.RequestWrapper;
import pcm.comunication.dispatcher.stats.graphs.GenericHistogram3DServlet;
import pcm.context.logicmodel.definitions.IFieldLogic;
import facturacionUte.common.UnitsForFields;

public class MyHistogram3DStatsServlet extends GenericHistogram3DServlet {

	private static final long serialVersionUID = 158971895179444444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final RequestWrapper request_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, request_);
	}
	

}
