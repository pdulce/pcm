package facturacionUte.servlets.diagramas;

import cdd.domain.dataccess.definitions.IFieldLogic;
import cdd.domain.dataccess.dto.Data;
import cdd.webapp.stats.graphs.GenericSpiderChartServlet;
import facturacionUte.common.UnitsForFields;

public class MySpiderWebStatsServlet extends GenericSpiderChartServlet {

	private static final long serialVersionUID = 158970004444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final Data data_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, data_);
	}
}
