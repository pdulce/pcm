package facturacionUte.strategies.concursos;

import java.util.Collection;
import java.util.Iterator;

import pcm.common.PCMConstants;
import pcm.common.exceptions.PCMConfigurationException;
import pcm.common.exceptions.StrategyException;
import pcm.common.utils.CommonUtils;
import pcm.comunication.actions.Event;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.logicmodel.definitions.IEntityLogic;
import pcm.context.logicmodel.factory.EntityLogicFactory;
import pcm.context.viewmodel.definitions.FieldViewSet;
import pcm.strategies.DefaultStrategyRequest;
import facturacionUte.common.ConstantesModelo;

public class StrategyEncajarNuevaApp extends DefaultStrategyRequest {
	
	@Override
	protected void validParameters(RequestWrapper req) throws StrategyException {
		// OK
	}

	
	@Override
	public void doBussinessStrategy(final RequestWrapper req, final IDataAccess dataAccess, final Collection<FieldViewSet> fieldViewSets) throws StrategyException,
			PCMConfigurationException {
		
		if (Event.isTransactionalEvent(req.getParameter(PCMConstants.EVENT))){
			return;
		}
		
		FieldViewSet datosDptoRequest = null;
		Iterator<FieldViewSet> iteFieldSets = fieldViewSets.iterator();
		if (iteFieldSets.hasNext()) {
			datosDptoRequest = iteFieldSets.next();
		}
		if (datosDptoRequest == null) {
			throw new PCMConfigurationException("Error objeto recibido de request es nulo", new Exception("null object"));
		}
		String lang = CommonUtils.getEntitiesDictionary(req);

		try {
			final IEntityLogic dptoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.DEPARTAMENTO_ENTIDAD);
			final IEntityLogic appEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(lang, ConstantesModelo.PROYECTO_ENTIDAD);
			
			String dptoIdParam = dptoEntidad.getName().concat(".").concat(dptoEntidad.getFieldKey().getPkFieldSet().iterator().next().getName());
			String idDptoInReq = req.getParameter(dptoIdParam);
			if (idDptoInReq != null && !"".equals(idDptoInReq)){
				Long idDpto = new Long((String) idDptoInReq);							
				datosDptoRequest.setValue(appEntidad.searchField(ConstantesModelo.PROYECTO_7_DEPARTAMENTO).getName(), idDpto);
			}
			
		} catch (PCMConfigurationException e2) {
			throw new PCMConfigurationException("error2", e2);
		}
	}

}
