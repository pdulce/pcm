package facturacionUte.servlets.diagramas;

import domain.service.dataccess.definitions.IFieldLogic;
import domain.service.dataccess.dto.Data;
import facturacionUte.common.UnitsForFields;
import webservlet.stats.graphs.GenericHistogram3DServlet;

public class MyHistogram3DStatsServlet extends GenericHistogram3DServlet {

	private static final long serialVersionUID = 158971895179444444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final Data data_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, data_);
	}
	

}
