package domain.service.event.validators;

import domain.common.PCMConstants;
import domain.common.exceptions.MessageException;
import domain.common.utils.CommonUtils;
import domain.service.component.IViewComponent;
import domain.service.event.IAction;
import domain.service.event.Parameter;

/**
 * <h1>DoubleValidator</h1> The DoubleValidator class is used for validating data of 'double' type.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class DoubleValidator implements IValidator {

	private DoubleValidator() {
	}

	public static boolean isValid(final String val_, final MessageException msg, final String nombreQ_, final boolean obligatorio,
			final Double minValue, final Double maxValue) {
		String valDesplegado = val_ == null ? PCMConstants.EMPTY_ : val_.trim();
		if (PCMConstants.EMPTY_.equals(valDesplegado.trim()) && obligatorio) {
			msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NEEDED));
			msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
			return false;
		}
		if (!PCMConstants.EMPTY_.equals(valDesplegado)) {
			try {
				try {
					char[] charArr = valDesplegado.toCharArray();
					for (final char charAtPos : charArr) {
						if (Character.isLetter(charAtPos)) {
							throw new Exception("error: letra");
						}
					}
				}
				catch (Throwable excFor) {
					msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NO_CORRECT_FORMAT));
					msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
					final StringBuilder sb = new StringBuilder(IValidator.DOUBLE_MSG).append(CommonUtils.numberFormatter
							.format(minValue == null ? 0 : minValue.doubleValue()));
					sb.append(PCMConstants.CHAR_SIMPLE_SEPARATOR)
							.append(CommonUtils.numberFormatter.format(maxValue == null ? 0 : maxValue.doubleValue()))
							.append(PCMConstants.CHAR_END_CORCH);
					msg.addParameter(new Parameter(IViewComponent.ONE, sb.toString()));
					return false;
				}
				valDesplegado = val_;
				if (val_.indexOf(PCMConstants.POINT) != -1 && val_.indexOf(PCMConstants.COMMA) != -1) {
					valDesplegado = val_.replace(PCMConstants.POINT, PCMConstants.EMPTY_);
				}
				if (valDesplegado.indexOf(PCMConstants.COMMA) != -1) {
					valDesplegado = valDesplegado.replace(PCMConstants.COMMA, PCMConstants.POINT);
				}

				final Double b = Double.valueOf(valDesplegado);
				if (minValue != null && b.compareTo(minValue) < 0) {
					msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NO_ALCANZA_MINVAL));
					msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
					msg.addParameter(new Parameter(IViewComponent.ONE, String.valueOf(CommonUtils.numberFormatter
							.format(minValue == null ? 0 : minValue.doubleValue()))));
					return false;
				} else if (maxValue != null && b.compareTo(maxValue) > 0) {
					msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_SUPERA_MAXVAL));
					msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
					msg.addParameter(new Parameter(IViewComponent.ONE, String.valueOf(CommonUtils.numberFormatter
							.format(maxValue == null ? 0 : maxValue.doubleValue()))));
					return false;
				}
			}
			catch (final Throwable parseExc) {
				msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NO_CORRECT_FORMAT));
				msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
				final StringBuilder sb = new StringBuilder(IValidator.DOUBLE_MSG).append(CommonUtils.numberFormatter
						.format(minValue == null ? 0 : minValue.doubleValue()));
				sb.append(PCMConstants.CHAR_SIMPLE_SEPARATOR).append(maxValue).append(PCMConstants.CHAR_END_CORCH);
				msg.addParameter(new Parameter(IViewComponent.ONE, sb.toString()));
				return false;
			}
		}
		return true;
	}

}
