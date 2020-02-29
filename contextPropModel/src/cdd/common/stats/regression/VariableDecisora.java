package cdd.common.stats.regression;

import java.util.Map;

public class VariableDecisora {
	protected String name;
	protected Range range;
	protected Map<String, Integer> literalMappingTo;
	protected Integer categories;
	
	protected VariableDecisora(){		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Range getRange() {
		return range;
	}
	public void setRange(Range range) {
		this.range = range;
	}
	public Map<String, Integer> getLiteralMappingTo() {
		return literalMappingTo;
	}
	public void setLiteralMappingTo(Map<String, Integer> literalMappingTo) {
		this.literalMappingTo = literalMappingTo;
	}

	public Integer getCategories() {
		return categories;
	}

	public void setCategories(Integer categories) {
		this.categories = categories;
	}
	
	
}

