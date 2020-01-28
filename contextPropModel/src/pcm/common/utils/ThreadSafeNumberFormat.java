package pcm.common.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import pcm.common.PCMConstants;

/**
 * <h1>ThreadSafeNumberFormat</h1> The ThreadSafeNumberFormat class
 * is used for thread-safe use of a number formatter.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ThreadSafeNumberFormat {

	public final static long serialVersionUID = 698054222000L;

	private static ThreadSafeNumberFormat myFormatter;

	private final NumberFormat formatter, intFormatter, longFormatter;

	private ThreadSafeNumberFormat() {
		this.formatter = NumberFormat.getNumberInstance(new Locale(PCMConstants.ES_CHARCODE));
		this.formatter.setMaximumFractionDigits(2);
		this.formatter.setMinimumFractionDigits(2);
		this.intFormatter = NumberFormat.getIntegerInstance(new Locale(PCMConstants.ES_CHARCODE));
		this.intFormatter.setMaximumFractionDigits(0);
		this.intFormatter.setMinimumFractionDigits(0);
		
		this.longFormatter = NumberFormat.getNumberInstance(new Locale(PCMConstants.ES_CHARCODE));
		this.longFormatter.setMaximumFractionDigits(0);
		this.longFormatter.setMinimumFractionDigits(0);
	}

	public static synchronized final ThreadSafeNumberFormat getUniqueInstance() {
		if (ThreadSafeNumberFormat.myFormatter == null) {
			ThreadSafeNumberFormat.myFormatter = new ThreadSafeNumberFormat();
		}
		return ThreadSafeNumberFormat.myFormatter;
	}

	public synchronized String format(final long number) {
		return this.intFormatter.format(number);
	}

	public synchronized String format(final int number) {
		return this.intFormatter.format(number);
	}

	public synchronized String formatBigData(final double number) {
		return this.longFormatter.format(new BigDecimal(number));
	}

	public synchronized String formatBigData(final Double number) {
		return this.longFormatter.format(new BigDecimal(number.doubleValue()));
	}

	public synchronized String formatBigData(final BigDecimal number) {
		return this.longFormatter.format(number);
	}	
	
	public synchronized String format(final double number) {
		return this.formatter.format(new BigDecimal(number));
	}

	public synchronized String format(final Double number) {
		return this.formatter.format(new BigDecimal(number.doubleValue()));
	}

	public synchronized String format(final BigDecimal number) {
		return this.formatter.format(number);
	}


	public synchronized Double parse(Serializable val) throws ParseException {
		if (val == null) {
			return Double.valueOf("0");
		} else if (val instanceof java.lang.String) {			
			return parse(val.toString());
		} else if (val instanceof java.math.BigDecimal) {
			return parse((java.math.BigDecimal) val);
		} else if (val instanceof java.lang.Number) {
			return parse((java.lang.Number) val);
		}
		return Double.valueOf("0");
	}
	
	public synchronized Double parse(java.lang.String val) throws ParseException {
		if (val == null) {
			return Double.valueOf("0");
		} else  {
			if (val.toString().indexOf(PCMConstants.POINT) != -1 && val.toString().indexOf(PCMConstants.COMMA) == -1) {
				// formato 78988.8798789788, hacemos un Double.of
				return Double.valueOf(val.toString());
			}
			return Double.valueOf(this.formatter.parse(val.toString()).doubleValue());
		} 		
	}
	
	public synchronized Double parse(java.math.BigDecimal val) throws ParseException {
		if (val == null) {
			return Double.valueOf("0");
		} else {
			return Double.valueOf(val.doubleValue());
		}
	}
	
	public synchronized Double parse(java.lang.Number val) throws ParseException {
		if (val == null) {
			return Double.valueOf("0");		
		} else {
			return Double.valueOf(val.doubleValue());
		}
	}
	

	public static void main(String[] args) {
		try {
			System.out.println("valor1: " + ThreadSafeNumberFormat.getUniqueInstance().parse(Double.valueOf(898.98)));
			System.out.println("valor1: " + ThreadSafeNumberFormat.getUniqueInstance().parse(new BigDecimal("128.98")));
			System.out.println("valor2: " + ThreadSafeNumberFormat.getUniqueInstance().parse("8.989.823,64"));
			System.out.println("valor3: " + ThreadSafeNumberFormat.getUniqueInstance().parse("808,8"));
			System.out.println("valor4: " + ThreadSafeNumberFormat.getUniqueInstance().parse("18823.897978997"));
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
