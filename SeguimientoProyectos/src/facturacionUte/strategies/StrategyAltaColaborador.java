package facturacionUte.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import cdd.common.exceptions.DatabaseException;
import cdd.common.exceptions.PCMConfigurationException;
import cdd.common.exceptions.StrategyException;
import cdd.comunication.bus.Data;
import cdd.logicmodel.IDataAccess;
import cdd.logicmodel.definitions.IEntityLogic;
import cdd.logicmodel.factory.EntityLogicFactory;
import cdd.strategies.DefaultStrategyRequest;
import cdd.viewmodel.definitions.FieldViewSet;
import cdd.viewmodel.definitions.IFieldView;

import facturacionUte.common.ConstantesModelo;

public class StrategyAltaColaborador extends DefaultStrategyRequest {

	public static final String ERR_COLABORADOR_NO_VALIDO = "ERR_COLABORADOR_NO_VALIDO";

	@Override
	public void doBussinessStrategy(final Data req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {

		try {
			Iterator<FieldViewSet> iteFviewsets = fieldViewSets.iterator();
			while (iteFviewsets.hasNext()) {
				FieldViewSet fieldViewSet = iteFviewsets.next();
				if (fieldViewSet.getEntityDef().getName().equals(ConstantesModelo.COLABORADOR_ENTIDAD)) {
					final IEntityLogic colaboradores = EntityLogicFactory.getFactoryInstance().getEntityDef(req.getEntitiesDictionary(),
							ConstantesModelo.COLABORADOR_ENTIDAD);
					String nombreReq = (String) fieldViewSet.getValue(colaboradores.searchField(ConstantesModelo.COLABORADOR_2_NOMBRE).getName());
					String apel1Req = (String) fieldViewSet.getValue(colaboradores.searchField(ConstantesModelo.COLABORADOR_3_APELLIDOS).getName());

					if (nombreReq != null && apel1Req != null && !"".equals(nombreReq) && !"".equals(apel1Req.trim())) {
						/** RECUPERAMOS DATOS DE BBDD * */

						final FieldViewSet filterColaboradores = new FieldViewSet(colaboradores);
						filterColaboradores.setValue(colaboradores.searchField(ConstantesModelo.COLABORADOR_2_NOMBRE).getName(), nombreReq.trim());// "nombre"
						filterColaboradores.setValue(colaboradores.searchField(ConstantesModelo.COLABORADOR_3_APELLIDOS).getName(), apel1Req.trim());// "apellido1"
						final Collection<FieldViewSet> resultados = dataAccess.searchByCriteria(filterColaboradores);

						// guardo las credenciales en sesion en el caso de que no vengan ya en sesion
						if (!resultados.isEmpty()) {
							final Collection<Object> messageArguments = new ArrayList<Object>();
							messageArguments.add(nombreReq);
							messageArguments.add(apel1Req);
							throw new StrategyException(ERR_COLABORADOR_NO_VALIDO, messageArguments);
						}
					} else if (nombreReq == null && apel1Req == null) {
						IFieldView campoExtinguida = fieldViewSet.getFieldView(colaboradores.searchField(ConstantesModelo.COLABORADOR_5_RELACION_EXTINGUIDA).getName());
						fieldViewSet.setValue(campoExtinguida.getQualifiedContextName(), Integer.valueOf(0));
					}
					break;
				}// if
			}// while

		} catch (final DatabaseException ecxx1) {
			throw new PCMConfigurationException(ERR_COLABORADOR_NO_VALIDO, ecxx1);
		} catch (final PCMConfigurationException ecxx2) {
			throw new PCMConfigurationException(ERR_COLABORADOR_NO_VALIDO, ecxx2);
		}
	}

	@Override
	protected void validParameters(Data req) throws StrategyException {
		// OK
	}

}
