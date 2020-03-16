package facturacionUte.servlets.diagramas;

import domain.dataccess.definitions.IFieldLogic;
import domain.dataccess.dto.Data;
import facturacionUte.common.UnitsForFields;
import webservlet.stats.graphs.GenericSpiderChartServlet;

public class MySpiderWebStatsServlet extends GenericSpiderChartServlet {

	private static final long serialVersionUID = 158970004444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final Data data_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, data_);
	}
}
