package cdd.domain.common.stats.regression;

public class Range {
	
Number min, max;
	
	/** min y max inclusive **/
	public int getAmplitudRango(){
		return this.max.intValue() - this.min.intValue() + 1;
	}
	
	public Range(Number min_, Number max_){
		this.min = min_;
		this.max = max_;
	}
	public Number getMin() {
		return min;
	}

	public void setMin(Number min) {
		this.min = min;
	}

	public Number getMax() {
		return max;
	}

	public void setMax(Number max) {
		this.max = max;
	}
	
	public boolean isValid(Number numb){
		return (numb != null) && (numb.doubleValue() >= this.min.doubleValue()) && (numb.doubleValue() <= this.max.doubleValue());
	}
	
	@Override
	public String toString() {
		return "rango [min=" + min + ", max=" + max + "]";
	}
	

}
