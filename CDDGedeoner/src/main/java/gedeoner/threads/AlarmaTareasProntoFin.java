package gedeoner.threads;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.cdd.application.ApplicationDomain;
import org.cdd.common.exceptions.DatabaseException;
import org.cdd.common.exceptions.PCMConfigurationException;
import org.cdd.common.utils.CommonUtils;
import org.cdd.service.component.definitions.FieldViewSet;
import org.cdd.service.component.definitions.IFieldView;
import org.cdd.service.component.definitions.IRank;
import org.cdd.service.component.definitions.Rank;
import org.cdd.service.dataccess.IDataAccess;
import org.cdd.service.dataccess.definitions.IEntityLogic;
import org.cdd.service.dataccess.definitions.IFieldLogic;
import org.cdd.service.dataccess.factory.EntityLogicFactory;

import gedeoner.common.ConstantesModelo;

public class AlarmaTareasProntoFin extends Thread {
	
	public IEntityLogic peticionesEntidad, aplicativoEntidad;
	
	ApplicationDomain domain;
	IDataAccess dataAccess;
	
	/*** Construye un nuevo hil **/
	public AlarmaTareasProntoFin(String nombre, ApplicationDomain domain_) {
		super(nombre);
		try {
			domain = domain_;
			dataAccess = domain_.getDataAccess();		
		} catch (PCMConfigurationException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		//inicializamos las entidades a usar
		try {
			peticionesEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
					ConstantesModelo.PETICIONES_ENTIDAD);
			aplicativoEntidad = EntityLogicFactory.getFactoryInstance().getEntityDef(dataAccess.getDictionaryName(),
					ConstantesModelo.APLICATIVO_ENTIDAD);
		}catch (PCMConfigurationException e) {
			throw new RuntimeException("error charging entities", e);
		}

	}

	// Punto de entrada del hilo
	public void run() {
		
		//System.out.println(getName() + " iniciando...");
		try {
			String infoMessage = obtenerListaFinalizanPronto();
			int contador = 1;
			while (!domain.isObsoleteAlert()) {				
				if (!"".contentEquals(infoMessage)) {
					domain.setAlertMessages(infoMessage);
				}
				Thread.sleep(60000);//1 minuto entre cada despertar
				if (contador % 2 == 0) {// a los dos minutos, borramos las alertas
					domain.deleteAlertMessages();
					domain.setObsoleteAlert(true);
					//Thread.sleep(300000);//30 minutos hasta que vuelvo a recorrer el bucle
				}
				contador++;
			}
		} catch (InterruptedException exc) {
			System.out.println(getName() + " ha sido interrumpido.");
			throw new RuntimeException(exc.getMessage(), exc);
		}finally {
		
			if (dataAccess != null && dataAccess.getConn() != null){
				try {
					domain.getResourcesConfiguration().getDataSourceFactoryImplObject().freeConnection(dataAccess.getConn());
					//System.out.println("..." + getName() + " finalizado.");		
					domain.deleteAlertMessages();
					domain.setObsoleteAlert(true);
				} catch (PCMConfigurationException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}
	}
	
	private String obtenerListaFinalizanPronto()  {
		
		try {			
			final Collection<IFieldView> fieldViews4FilterFecAndUts_ = new ArrayList<IFieldView>();
			
			final IFieldLogic fieldfecFinPrevisto = peticionesEntidad.searchField(ConstantesModelo.PETICIONES_23_DES_FECHA_PREVISTA_FIN);
			IFieldView fViewFinPrevisto =  new FieldViewSet(peticionesEntidad).getFieldView(fieldfecFinPrevisto);			
			final IFieldView fViewMinorFinPrev = fViewFinPrevisto.copyOf();
			final Rank rankDesde = new Rank(fViewFinPrevisto.getEntityField().getName(), IRank.MINOR_EQUALS_OPE);
			fViewMinorFinPrev.setRankField(rankDesde);			
			final Rank rankHasta = new Rank(fViewFinPrevisto.getEntityField().getName(), IRank.MAYOR_EQUALS_OPE);
			final IFieldView fViewMayorFinPrev = fViewFinPrevisto.copyOf();			
			fViewMayorFinPrev.setRankField(rankHasta);
				
			fieldViews4FilterFecAndUts_.add(fViewMinorFinPrev);
			fieldViews4FilterFecAndUts_.add(fViewMayorFinPrev);
			
			//Establecemos el rango de fechas para la búsqueda
			Calendar calFechaDesde = Calendar.getInstance();
			calFechaDesde.add(Calendar.DATE, -2);
			Calendar calFechaHasta = Calendar.getInstance();
			calFechaHasta.add(Calendar.DATE, 2);
			
			FieldViewSet filterPeticiones = new FieldViewSet(dataAccess.getDictionaryName(), peticionesEntidad.getName(), fieldViews4FilterFecAndUts_);
			filterPeticiones.setValue(fViewMinorFinPrev.getQualifiedContextName(), calFechaDesde.getTime());
			filterPeticiones.setValue(fViewMayorFinPrev.getQualifiedContextName(), calFechaHasta.getTime());
			
			List<String> situaciones = new ArrayList<String>();
			situaciones.add("En curso");
			situaciones.add("Tramitada");
			situaciones.add("Trabajo en curso");
			situaciones.add("Entrega en redacción (en CD)");
			//situaciones.add("Trabajo pte. validar por CD");
			situaciones.add("Trabajo estimado");
			situaciones.add("Pendiente de estimación");
			situaciones.add("Trabajo listo para iniciar");
			filterPeticiones.setValues(peticionesEntidad.searchField(ConstantesModelo.PETICIONES_7_ESTADO).getName(), situaciones); 
						
			Collection<FieldViewSet> peticionesProntoFin = dataAccess.searchByCriteria(filterPeticiones, new String []{peticionesEntidad.getName() + ".id"}, "asc");
			StringBuilder strBuilder = new StringBuilder(peticionesProntoFin.isEmpty()? "": "¡¡AVISO!! Estas peticiones finalizan en breve:<br><ul>");
			Iterator<FieldViewSet> itePets = peticionesProntoFin.iterator();
			while (itePets.hasNext()) {
				FieldViewSet peticionSearched = itePets.next();
				Date fechaFinPrevistoPet = (Date) peticionSearched.getValue(ConstantesModelo.PETICIONES_23_DES_FECHA_PREVISTA_FIN);
				Long codGEDEON = (Long) peticionSearched.getValue(ConstantesModelo.PETICIONES_46_COD_GEDEON);
				Long idAplicativo = (Long) peticionSearched.getValue(ConstantesModelo.PETICIONES_26_ID_APLICATIVO);
				FieldViewSet aplicativoVO = new FieldViewSet(aplicativoEntidad);
				aplicativoVO.setValue(ConstantesModelo.APLICATIVO_1_ID, idAplicativo);
				aplicativoVO = dataAccess.searchEntityByPk(aplicativoVO);
				if (aplicativoVO == null) {
					//throw new RuntimeException("error");
					continue;
				}
				String nombreAplicativo = (String) aplicativoVO.getValue(ConstantesModelo.APLICATIVO_2_ROCHADE);
				
				String centroDestino = (String) peticionSearched.getValue(ConstantesModelo.PETICIONES_11_CENTRO_DESTINO);
				if (centroDestino.indexOf("DG") != -1) {
					centroDestino = "DG";
				}
				 				
				String situacionPeticion = (String) peticionSearched.getValue(ConstantesModelo.PETICIONES_7_ESTADO);
				String lineOfBuffer = "<li>Petición " + codGEDEON + "("+ centroDestino +")[" + nombreAplicativo + 
							"] en situación " + situacionPeticion + " => fec.prevista fin: " + ((fechaFinPrevistoPet == null)? "sin planificar":CommonUtils.myDateFormatter.format(fechaFinPrevistoPet)) + "</li>"; 
				strBuilder.append(lineOfBuffer);
				if (!itePets.hasNext()) {
					strBuilder.append("</ul>");
				}
			}
			return "";//strBuilder.toString();
			
		} catch (DatabaseException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		
	}
}