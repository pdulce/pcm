package facturacionUte.servlets;

import domain.service.highcharts.IStats;
import facturacionUte.dashboards.Dashboard;
import webservlet.CDDWebController;

public class MyServlet extends CDDWebController {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1800351789357L;

	protected IStats getDashboardImpl() {
		return new Dashboard();
	}
}
