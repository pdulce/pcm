package facturacionUte.servlets.diagramas;

import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Datamap;
import facturacionUte.common.UnitsForFields;
import webservlet.stats.graphs.GenericMapStatsServlet;

public class MyMapStatsServlet extends GenericMapStatsServlet {

	private static final long serialVersionUID = 158971895179444444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final Datamap data_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, data_);
	}

}
