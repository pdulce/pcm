package cdd.domain.service.event.validators;

import cdd.common.PCMConstants;
import cdd.common.exceptions.MessageException;
import cdd.domain.component.components.IViewComponent;
import cdd.domain.service.event.IAction;
import cdd.domain.service.event.Parameter;

/**
 * <h1>IntegerValidator</h1> The IntegerValidator class is used for validating data of 'int' type.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class IntegerValidator implements IValidator {

	private IntegerValidator() {
	}

	public static boolean isValid(final String val_, final MessageException msg, final String nombreQ_, final boolean obligatorio,
			final Double minValue, final Double maxValue) {
		String val = val_ == null ? PCMConstants.EMPTY_ : val_.trim();
		if (PCMConstants.EMPTY_.equals(val.trim()) && obligatorio) {
			msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NEEDED));
			msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
			return false;
		}
		if (!PCMConstants.EMPTY_.equals(val)) {
			val = val.replaceAll(PCMConstants.REGEXP_POINT, PCMConstants.EMPTY_);
			try {
				final Double b = Double.valueOf(val);
				if (minValue != null && b.compareTo(minValue) < 0) {
					msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NO_ALCANZA_MINVAL));
					msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
					msg.addParameter(new Parameter(IViewComponent.ONE, String.valueOf(minValue == null ? 0 : minValue.intValue())));
					return false;
				} else if (maxValue != null && b.compareTo(maxValue) > 0) {
					msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_SUPERA_MAXVAL));
					msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
					msg.addParameter(new Parameter(IViewComponent.ONE, String.valueOf(maxValue == null ? 0 : maxValue.intValue())));
					return false;
				}
			}
			catch (final NumberFormatException castExc) {
				msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NO_CORRECT_FORMAT));
				msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
				final StringBuilder sb = new StringBuilder(IValidator.INT_MSG).append(minValue == null ? 0 : minValue.intValue());
				sb.append(PCMConstants.CHAR_SIMPLE_SEPARATOR).append(maxValue == null ? 0 : maxValue.intValue())
						.append(PCMConstants.CHAR_END_CORCH);
				msg.addParameter(new Parameter(IViewComponent.ONE, sb.toString()));
				return false;
			}
		}
		return true;
	}

}
