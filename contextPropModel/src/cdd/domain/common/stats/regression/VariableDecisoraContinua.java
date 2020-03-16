package cdd.common.stats.regression;

import java.util.HashMap;

public class VariableDecisoraContinua extends VariableDecisora{
	public VariableDecisoraContinua(final String name_, final Range range_){
		setName(name_);
		setRange(range_);
		setLiteralMappingTo(new HashMap<String, Integer>());
		setCategories(0);
	}
}
