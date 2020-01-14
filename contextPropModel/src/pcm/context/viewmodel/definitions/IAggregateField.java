package pcm.context.viewmodel.definitions;

import pcm.context.logicmodel.definitions.EntityLogic;

public interface IAggregateField {

	public String getFormulaSQL();

	public EntityLogic getParentEntity();

	public int getDimension();

	public void setParentEntity(EntityLogic parentEntity);

	public void setFormulaSQL(String formul_);

	public void setDimension(int dimension);

}
