/**
 * 
 */
package domain.service.highcharts;

import domain.service.DomainService;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Datamap;

/**
 * @author 99GU3997
 */
public interface IStats {
	
	public static final String JSON_OBJECT = "series";
	
	public static final String CATEGORIES = "categories";

	public static final String ABSCISAS = "abscisas";

	public static final String OPERATION_FIELD_PARAM = "operation";

	public static final String FIELD_4_GROUP_BY = "fieldForGroupBy";

	public static final String DECIMALES = "decimals";

	public static final String FIELD_FOR_FILTER = "filteredByField";
	
	public static final String FIELD_FOR_DIFFERENCE_IN_CORRELATION = "atrDiferenciador";

	public static final String VALUE_FOR_FILTER = "filteredByFieldValues";

	public static final String OPERATION_SUM = "SUM";

	public static final String OPERATION_COUNT = "COUNT";

	public static final String OPERATION_AVERAGE = "AVG";

	public static final String ENTIDAD_GRAFICO_PARAM = "entidadGrafico";

	public static final String AGGREGATED_FIELD_PARAM = "agregado";

	public static final String LIGHT_COLOR_FIELD_PARAM = "minColor";

	public static final String DARK_COLOR_FIELD_PARAM = "maxColor";

	public static final String ORDER_BY_FIELD_PARAM = "orderBy";

	public static final String CATEGORIA_FIELD_PARAM = "categoria";

	public static final String UNITS_ATTR = "units";

	public static final String SUBTILE_ATTR = "subtitle";
	
	public static final String CHART_TITLE = "chart_Title";

	public static final String ADDITIONAL_INFO_ATTR = "addedInfo";

	public static final String TITLE_ATTR = "title";

	public static final String TEXT_X_AXIS = "titulo_EJE_X";

	public static final String TEXT_Y_AXIS = "titulo_EJE_Y";
	
	public void generateStatGraphModel(final IDataAccess dataAccess_, final DomainService domainService, final Datamap _data);
	
	public String getScreenRendername();

}
