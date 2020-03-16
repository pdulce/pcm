package cdd.domain.service.event.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cdd.domain.common.PCMConstants;
import cdd.domain.common.exceptions.MessageException;
import cdd.domain.component.IViewComponent;
import cdd.domain.service.event.IAction;
import cdd.domain.service.event.Parameter;


public class StringValidator implements IValidator {

	private StringValidator() {
		// private
	}

	public static boolean isValid(final String val_, final MessageException msg, final String nombreQ_, final boolean obligatorio, final int minLength, final int maxLength,
			final String regexp) {
		final String val = val_ == null ? PCMConstants.EMPTY_ : val_.trim();
		if (PCMConstants.EMPTY_.equals(val.trim()) && obligatorio) {
			msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NEEDED));
			msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
			return false;
		}
		if (!PCMConstants.EMPTY_.equals(val.trim())) {
			if (regexp != null && obligatorio) {
				final Pattern regExprPattern = Pattern.compile(regexp);
				final Matcher fit = regExprPattern.matcher(val);
				if (!fit.matches()) {
					msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NEEDED));
					msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
					msg.addParameter(new Parameter(IViewComponent.ONE, regexp));
					return false;
				}
			}
			if (val.length() > maxLength) {
				msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_SUPERA_MAXLENGTH));
				msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
				msg.addParameter(new Parameter(IViewComponent.ONE, String.valueOf(maxLength)));
				return false;
			} else if (val.length() < minLength) {
				msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NO_ALCANZA_MINLENGTH));
				msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
				msg.addParameter(new Parameter(IViewComponent.ONE, String.valueOf(minLength)));
				return false;
			}
		}
		return true;
	}

}
