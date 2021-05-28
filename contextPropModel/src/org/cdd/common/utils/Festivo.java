package org.cdd.common.utils;

public class Festivo {
	
	private int day, month, year;
	
	public Festivo(int day_, int month_, int year_) {
		this.day = day_;
		this.month = month_;
		this.year = year_;
	}
	
	public int getDay() {
		return day;
	}

	public void setDay(int day_) {
		this.day = day_;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month_) {
		this.month = month_;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year_) {
		this.year = year_;
	}
	
	
	
}
