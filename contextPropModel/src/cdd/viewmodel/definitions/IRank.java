package cdd.viewmodel.definitions;

public interface IRank {

	public static final String LOWER_RANK_ATTR = "lowerRank", UPPER_RANK_ATTR = "upperRank", RELATIONAL_OPE_ATTR = "relationalOpe", DESDE_SUFFIX = "_DESDE",
			HASTA_SUFFIX = "_HASTA", EQUALS_OPE = "EQUALS", MINOR_OPE = "MINOR", MINOR_EQUALS_OPE = "MINOR_EQ", MAYOR_OPE = "MAYOR", MAYOR_EQUALS_OPE = "MAYOR_EQ";

	public String getRelationalOpe();

	public String getName();

	public boolean isMinorInRange();

}
