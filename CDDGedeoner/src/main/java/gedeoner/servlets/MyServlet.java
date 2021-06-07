package gedeoner.servlets;

import org.cdd.application.ApplicationDomain;
import org.cdd.service.highcharts.IStats;
import org.cdd.webservlet.CDDWebController;

import gedeoner.dashboards.Dashboard;
import gedeoner.threads.AlarmaTareasProntoFin;

public class MyServlet extends CDDWebController {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1800351789357L;

	protected IStats getDashboardImpl() {
		return new Dashboard();
	}
	
	protected void arrancarThreads(ApplicationDomain domain) {
		AlarmaTareasProntoFin alarmCheck = new AlarmaTareasProntoFin("#tareas pronto fin search", domain);
		alarmCheck.start();
	}
}
