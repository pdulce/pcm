package facturacionUte.servlets.diagramas;

import cdd.comunication.bus.Data;
import cdd.comunication.dispatcher.stats.graphs.GenericHistogram3DServlet;
import cdd.logicmodel.definitions.IFieldLogic;
import facturacionUte.common.UnitsForFields;

public class MyHistogram3DStatsServlet extends GenericHistogram3DServlet {

	private static final long serialVersionUID = 158971895179444444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final Data data_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, data_);
	}
	

}
