package facturacionUte.common;

import java.io.Serializable;
import java.util.Comparator;

import domain.service.component.definitions.FieldViewSet;
import facturacionUte.utils.GeneradorPresentaciones;


public class ComparatorBySameProjectAndEpigrafe implements Comparator<FieldViewSet>, Serializable {
	
	private static final long serialVersionUID = 235999829222211L;

	@Override
	public final int compare(final FieldViewSet entry1, final FieldViewSet entry2) {
		
		String prjName1 = (String) entry1.getValue(entry1.getEntityDef().searchField(ConstantesModelo.INCIDENCIASPROYECTO_27_PROYECTO_NAME).getName());
		String prjName2 = (String) entry2.getValue(entry2.getEntityDef().searchField(ConstantesModelo.INCIDENCIASPROYECTO_27_PROYECTO_NAME).getName());
		
		String servicio1 = GeneradorPresentaciones.APP_AREA.get(prjName1);
		String servicio2 = GeneradorPresentaciones.APP_AREA.get(prjName2);
		
		int resultado = 0;
		if (servicio1.compareTo(servicio2)  < 0) {
			resultado = -1;
		} else if (servicio1.compareTo(servicio2)  > 0) {
			resultado = 1;		
		}else{
			if (prjName1.compareTo(prjName2)  < 0) {
				resultado = -1;
			} else if (prjName1.compareTo(prjName2)  > 0) {
				resultado = 1;
			} else {
				// si pertenecen al mismo proyecto, ordenamos por el estado, y luego, por el epografe
				String status1_ = (String) entry1.getValue(entry1.getEntityDef().searchField(ConstantesModelo.INCIDENCIASPROYECTO_30_ANYO_MES).getName());
				String status2_ = (String) entry2.getValue(entry2.getEntityDef().searchField(ConstantesModelo.INCIDENCIASPROYECTO_30_ANYO_MES).getName());			
				
				String epigrafe1 = (String) entry1.getValue(entry1.getEntityDef().searchField(ConstantesModelo.INCIDENCIASPROYECTO_13_TIPO).getName());
				String epigrafe2 = (String) entry2.getValue(entry2.getEntityDef().searchField(ConstantesModelo.INCIDENCIASPROYECTO_13_TIPO).getName());
							
				if (status1_.equals("2")){
					String estadoTareaGlobal_1 = (String) entry1.getValue(entry1.getEntityDef().searchField(ConstantesModelo.INCIDENCIASPROYECTO_7_ESTADO).getName());
					if (estadoTareaGlobal_1.equals("Desestimada")){
						status1_ = "1";
					}
				}
				
				
				if (status2_.equals("2")){
					String estadoTareaGlobal_2 = (String) entry2.getValue(entry1.getEntityDef().searchField(ConstantesModelo.INCIDENCIASPROYECTO_7_ESTADO).getName());			
					if (estadoTareaGlobal_2.equals("Desestimada")){
						status2_ = "1";
					}
				}			
				
				Integer status1 = Integer.valueOf(status1_.concat(epigrafe1));
				Integer status2 = Integer.valueOf(status2_.concat(epigrafe2));
				if (status1 < status2) {
					resultado = -1;
				}else if (status1 > status2) {
					resultado = 1;
				}
			}
		}
		return resultado;
	}
}
