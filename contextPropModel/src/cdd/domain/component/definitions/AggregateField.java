package cdd.domain.component.definitions;

import java.io.Serializable;

import cdd.domain.dataccess.definitions.EntityLogic;


public class AggregateField implements IAggregateField, Serializable {

	private static final long serialVersionUID = 1444222221144444L;

	private String formulaSQL;

	private EntityLogic parentEntity;

	private int dimension;

	@Override
	public EntityLogic getParentEntity() {
		return this.parentEntity;
	}

	@Override
	public void setParentEntity(final EntityLogic parentEntity) {
		this.parentEntity = parentEntity;
	}

	public AggregateField(final String formulaSQL_) {
		this.setFormulaSQL(formulaSQL_);
	}

	@Override
	public final void setFormulaSQL(final String formul_) {
		this.formulaSQL = formul_;
	}

	@Override
	public String getFormulaSQL() {
		return this.formulaSQL;
	}

	@Override
	public int getDimension() {
		return this.dimension;
	}

	@Override
	public void setDimension(final int dimension) {
		this.dimension = dimension;
	}

}
