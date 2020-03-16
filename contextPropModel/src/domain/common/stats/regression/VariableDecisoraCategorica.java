package domain.common.stats.regression;

import java.util.Map;

public class VariableDecisoraCategorica extends VariableDecisora{
	public VariableDecisoraCategorica(final String name_, final Range range_, 
			final Map<String, Integer> literalMappingTo_){
		setName(name_);
		setRange(range_);
		setLiteralMappingTo(literalMappingTo_);
		setCategories(range_.getAmplitudRango());
	}	
}
