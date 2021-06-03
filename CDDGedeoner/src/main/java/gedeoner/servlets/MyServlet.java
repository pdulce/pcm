package gedeoner.servlets;

import org.cdd.service.highcharts.IStats;
import org.cdd.webservlet.CDDWebController;

import gedeoner.dashboards.Dashboard;

public class MyServlet extends CDDWebController {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1800351789357L;

	protected IStats getDashboardImpl() {
		return new Dashboard();
	}
}