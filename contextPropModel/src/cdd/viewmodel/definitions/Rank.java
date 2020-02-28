package cdd.viewmodel.definitions;

import java.io.Serializable;

import org.w3c.dom.Element;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.common.exceptions.PCMConfigurationException;


public class Rank implements IRank, Serializable {

	private static final long serialVersionUID = 69000002129999L;

	private String relationalOpe, name;

	public Rank(final Element nodeVirtualField, final String nameBase) throws PCMConfigurationException {
		try {
			this.relationalOpe = nodeVirtualField.getAttribute(IRank.RELATIONAL_OPE_ATTR);
			if (!IRank.MINOR_EQUALS_OPE.equals(this.getRelationalOpe()) && !IRank.MINOR_OPE.equals(this.getRelationalOpe())
					&& !IRank.MAYOR_EQUALS_OPE.equals(this.getRelationalOpe()) && !IRank.MAYOR_OPE.equals(this.getRelationalOpe())) {
				final StringBuilder strMsg = new StringBuilder(InternalErrorsConstants.ERROR_OPERATOR_USED);
				strMsg.append(IRank.MINOR_OPE).append(PCMConstants.CHAR_COMMA);
				strMsg.append(IRank.MINOR_EQUALS_OPE).append(PCMConstants.CHAR_COMMA);
				strMsg.append(IRank.MAYOR_OPE).append(PCMConstants.CHAR_COMMA);
				strMsg.append(IRank.MAYOR_EQUALS_OPE);
				throw new PCMConfigurationException(strMsg.toString());
			}
			final StringBuilder nameS = new StringBuilder(nameBase);
			if (IRank.MINOR_EQUALS_OPE.equals(this.getRelationalOpe()) || IRank.MINOR_OPE.equals(this.getRelationalOpe())) {
				nameS.append(IRank.DESDE_SUFFIX);
			} else {
				nameS.append(IRank.HASTA_SUFFIX);
			}
			this.name = nameS.toString();
		} catch (final NullPointerException exc) {
			throw new PCMConfigurationException(InternalErrorsConstants.ERROR_RANKFIELD_DEFINED, exc);
		}
	}

	public Rank(final String nameBase, final String relationalOpe_) {
		this.relationalOpe = relationalOpe_;
		final StringBuilder nameS = new StringBuilder(nameBase);
		nameS.append(this.isMinorInRange() ? IRank.DESDE_SUFFIX : IRank.HASTA_SUFFIX);
		this.name = nameS.toString();
	}

	@Override
	public final String getRelationalOpe() {
		return this.relationalOpe;
	}

	@Override
	public boolean isMinorInRange() {
		return IRank.MINOR_EQUALS_OPE.equals(this.getRelationalOpe()) || IRank.MINOR_OPE.equals(this.getRelationalOpe());
	}

	@Override
	public String getName() {
		return this.name;
	}

}
