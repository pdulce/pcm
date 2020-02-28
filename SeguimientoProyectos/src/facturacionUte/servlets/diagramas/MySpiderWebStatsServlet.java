package facturacionUte.servlets.diagramas;

import cdd.comunication.dispatcher.RequestWrapper;
import cdd.comunication.dispatcher.stats.graphs.GenericSpiderChartServlet;
import cdd.logicmodel.definitions.IFieldLogic;
import facturacionUte.common.UnitsForFields;

public class MySpiderWebStatsServlet extends GenericSpiderChartServlet {

	private static final long serialVersionUID = 158970004444L;

	@Override
	protected String getUnitName(final IFieldLogic aggregateField, final IFieldLogic fieldForCategoriaDeAgrupacion,
			final String aggregateFunction, final RequestWrapper request_) {
		return UnitsForFields.getUnitName(aggregateField, fieldForCategoriaDeAgrupacion, aggregateFunction, request_);
	}
}
