/**
 * 
 */
package domain.service.event.validators;

/**
 * <h1>IValidator</h1> The IValidator interface is used for defining the validating methods for
 * datamap.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public interface IValidator {

	public static final String DATA_NEEDED = "DATA_NEEDED";

	public static final String DATA_SUPERA_MAXVAL = "DATA_SUPERA_MAXVAL";

	public static final String DATA_NO_ALCANZA_MINVAL = "DATA_NO_ALCANZA_MINVAL";

	public static final String DATA_SUPERA_MAXLENGTH = "DATA_SUPERA_MAXLENGTH";

	public static final String DATA_NO_ALCANZA_MINLENGTH = "DATA_NO_ALCANZA_MINLENGTH";

	public static final String DATA_NO_CUMPLE_REGEXP = "DATA_NO_CUMPLE_REGEXP";

	public static final String DATA_NO_CORRECT_FORMAT = "DATA_NO_CORRECT_FORMAT";

	public static final String DATA_RANGE_INVALID = "DATA_RANGE_INVALID";

	public static final String BYTE_MSG = "Byte[0-255]";

	public static final String DATE_MSG = "Fecha[";

	public static final String DOUBLE_MSG = "Double[";

	public static final String INT_MSG = "Integer[";

	public static final String LONG_MSG = "Long[";

}
