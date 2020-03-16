package facturacionUte.servlets.diagramas;

import domain.dataccess.definitions.IFieldLogic;
import domain.dataccess.dto.Data;
import facturacionUte.common.UnitsForFields;
import webservlet.stats.graphs.GenericMapStatsServlet;

public class MyMapStatsServlet extends GenericMapStatsServlet {

	private static final long serialVersionUID = 158971895179444444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final Data data_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, data_);
	}

}
