package domain.service.highcharts;

public class LineSeries extends Histogram3D {

	

	@Override
	protected boolean is3D() {
		return false;
	}

	@Override
	public String getScreenRendername() {

		return "lineseries";
	}

}
