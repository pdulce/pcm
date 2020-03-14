package cdd.domain.service.event.validators;

import cdd.common.PCMConstants;
import cdd.common.exceptions.MessageException;
import cdd.domain.component.components.IViewComponent;
import cdd.domain.service.event.IAction;
import cdd.domain.service.event.Parameter;

/**
 * <h1>ByteValidator</h1> The ByteValidator class is used for validating data of 'byte' type.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class ByteValidator implements IValidator {

	private ByteValidator() {
	}

	public static boolean isValid(final String val_, final MessageException msg, final String nombreQ_, final boolean obligatorio,
			final byte minValue, final byte maxValue) {
		final String val = val_ == null ? PCMConstants.EMPTY_ : val_.trim();
		if (PCMConstants.EMPTY_.equals(val.trim()) && obligatorio) {
			msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NEEDED));
			msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
			return false;
		}
		if (!PCMConstants.EMPTY_.equals(val.trim())) {
			try {
				final Byte b = Byte.valueOf(val);
				if (b.longValue() < minValue) {
					msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NO_ALCANZA_MINVAL));
					msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
					msg.addParameter(new Parameter(IViewComponent.ONE, String.valueOf(minValue)));
					return false;
				} else if (b.longValue() > maxValue) {
					msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_SUPERA_MAXVAL));
					msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
					msg.addParameter(new Parameter(IViewComponent.ONE, String.valueOf(maxValue)));
					return false;
				}
			}
			catch (final NumberFormatException castExc) {
				msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NO_CORRECT_FORMAT));
				msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
				msg.addParameter(new Parameter(IViewComponent.ONE, IValidator.BYTE_MSG));
				return false;
			}
		}
		return true;
	}

}
