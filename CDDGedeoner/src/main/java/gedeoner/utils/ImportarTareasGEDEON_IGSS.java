/**
 * 
 */
package gedeoner.utils;


import org.cdd.service.dataccess.IDataAccess;



public class ImportarTareasGEDEON_IGSS extends ImportarTareasGEDEON{
	
	public ImportarTareasGEDEON_IGSS(IDataAccess dataAccess_) {
		super(dataAccess_);
	}

	protected String getDGFactory () {
		return "FACTDG06";
	}
	
	protected String getORIGEN_FROM_SG_TO_CD () {
		return "IGSS";
	}
	
	protected String getORIGEN_FROM_CD_TO_AT () {
		return "CDIGSS";
	}
	
	protected String getORIGEN_FROM_AT_TO_DESARR_GESTINADO () {
		return "SDG";
	}
	
	protected String getCD () {
		return "Centro de Desarrollo del IGSS";
	}
	
	protected String getCONTRATO_DG () {
		return "Desarrollo Gestionado 7206/18 L3";
	}
	
	

}
