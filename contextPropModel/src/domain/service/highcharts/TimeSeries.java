package domain.service.highcharts;

public class TimeSeries extends Histogram3D {

	

	@Override
	protected boolean is3D() {
		return false;
	}

	@Override
	public String getScreenRendername() {

		return "timeseries";
	}

}
