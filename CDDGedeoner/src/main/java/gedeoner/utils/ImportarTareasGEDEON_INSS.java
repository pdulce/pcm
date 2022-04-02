/**
 * 
 */
package gedeoner.utils;

import org.cdd.service.dataccess.IDataAccess;


public class ImportarTareasGEDEON_INSS extends ImportarTareasGEDEON{
	
	public ImportarTareasGEDEON_INSS(IDataAccess dataAccess_) {
		super(dataAccess_);
	}

	protected String getDGFactory () {
		return "FACTDG05";
	}
	
	protected String getORIGEN_FROM_SG_TO_CD () {
		return "INSS";
	}
	
	protected String getORIGEN_FROM_CD_TO_AT () {
		return "CDINSS";
	}
	
	protected String getORIGEN_FROM_AT_TO_DESARR_GESTINADO () {
		return "SDG";
	}
	
	protected String getCD () {
		return "Centro de Desarrollo del INSS";
	}
	
	protected String getCONTRATO_DG () {
		return "7201 17G L1";
	}

}
