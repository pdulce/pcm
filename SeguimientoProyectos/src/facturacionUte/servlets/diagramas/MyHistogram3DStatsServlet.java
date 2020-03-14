package facturacionUte.servlets.diagramas;

import cdd.data.bus.Data;
import cdd.logicmodel.definitions.IFieldLogic;
import cdd.webapp.stats.graphs.GenericHistogram3DServlet;
import facturacionUte.common.UnitsForFields;

public class MyHistogram3DStatsServlet extends GenericHistogram3DServlet {

	private static final long serialVersionUID = 158971895179444444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final Data data_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, data_);
	}
	

}
