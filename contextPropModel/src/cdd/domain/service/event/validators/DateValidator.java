package cdd.domain.service.event.validators;

import java.text.ParseException;

import cdd.common.PCMConstants;
import cdd.common.exceptions.MessageException;
import cdd.common.utils.CommonUtils;
import cdd.domain.component.html.IViewComponent;
import cdd.domain.service.event.IAction;
import cdd.domain.service.event.Parameter;


/**
 * <h1>DateValidator</h1> The DateValidator class is used for validating data of 'date' type.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class DateValidator implements IValidator {

	private DateValidator() {
	}

	public static boolean isValid(final String val_, final MessageException msg, final String nombreQ_, final boolean obligatorio) {
		final String val = val_ == null ? PCMConstants.EMPTY_ : val_.trim();
		if (PCMConstants.EMPTY_.equals(val.trim()) && obligatorio) {
			msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NEEDED));
			msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
			return false;
		}
		if (!PCMConstants.EMPTY_.equals(val.trim())) {
			try {
				CommonUtils.myDateFormatter.parse(val);
			}
			catch (final ParseException castExc) {
				msg.addParameter(new Parameter(IAction.BINDING_CONCRETE_MSG, IValidator.DATA_NO_CORRECT_FORMAT));
				msg.addParameter(new Parameter(IViewComponent.ZERO, nombreQ_));
				final StringBuilder fec = new StringBuilder(IValidator.DATE_MSG);
				fec.append("dia/mes/aoo").append(PCMConstants.CHAR_END_CORCH);
				msg.addParameter(new Parameter(IViewComponent.ONE, fec.toString()));
				return false;
			}
		}
		return true;
	}

}
