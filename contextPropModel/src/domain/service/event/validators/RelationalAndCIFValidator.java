package domain.service.event.validators;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import domain.common.utils.CommonUtils;
import domain.service.component.IViewComponent;


/**
 * <h1>RelationalAndCIFValidator</h1> The RelationalAndCIFValidator class is used for validating
 * relational operator between two variables, and for validating CIF, NIE and NIF format.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public final class RelationalAndCIFValidator {

	/** Formato del DNI , NIF sin letra */
	private static final String FORMATO_DNI = "[0-9]{1,8}";

	/** Letras validas del DNI/NIF */
	private static final String LETRAS_NIF = "TRWAGMYFPDXBNJZSQVHLCKEtrwagmyfpdxbnjzsqvhlcke";

	/** Formato NIF */
	private static final String FORMATO_NIF = new StringBuilder(RelationalAndCIFValidator.FORMATO_DNI).append("[")
			.append(RelationalAndCIFValidator.LETRAS_NIF).append("]{1}").toString();

	/** Formato NIE */
	private static final String FORMATO_NIE = new StringBuilder("[X]{1}").append(RelationalAndCIFValidator.FORMATO_NIF).toString();

	/** Prefijos validos para el CIF */
	private static final String PREFIJO_CIF = "ABCDEFGHKLMNPQSabcdefghklmnpqs";

	/** Sufijos de tipo caracter validos para el CIF */
	private static final String SUFIJO_LETRA_CIF = "JABCDEFGHIjabcdefghi";

	/** Sufijos de tipo nomerico volidos para el CIF */
	private static final String SUFIJO_NUM_CIF = "0123456789";

	/** Formato CIF */
	private static final String FORMATO_CIF = "[" + RelationalAndCIFValidator.PREFIJO_CIF + "]{1}[0-9]{1,7}["
			+ RelationalAndCIFValidator.SUFIJO_LETRA_CIF + RelationalAndCIFValidator.SUFIJO_NUM_CIF + "]{1}";

	/**
	 * Calculo de la letra de un DNI
	 * 
	 * @param valor
	 *            String convertible a entero de longitud entre 1 y 8 mayor que cero
	 * @return Letra del DNI , null en caso de que el valor no se corresponda con los digitos de un
	 *         DNI.
	 */
	public static final String letraNIF(final String valor) {
		try {
			final long nif = Long.parseLong(valor);
			if (nif > 0) {
				final int posicion = ((int) nif % 23);
				return RelationalAndCIFValidator.LETRAS_NIF.substring(posicion, posicion + 1);
			}
			return null;
		}
		catch (final IndexOutOfBoundsException outExc) {
			return null;
		}
	}

	/**
	 * Comprobamos si el DNI (Documeto Nacional de Identidad) es volido
	 * 
	 * @param valor
	 *            String que representa el DNI
	 * @return true si es correcto , false en caso contrario.
	 */
	public static final boolean esDNI(final String valor) {
		return valor != null && valor.matches(RelationalAndCIFValidator.FORMATO_DNI);
	}

	/**
	 * Comprobamos si el NIF (Numero de Identificacion Fiscal)
	 * 
	 * @param valor
	 *            String que representa el NIF
	 * @return True si es correcto , null en caso contrario.
	 */
	public static final boolean esNIF(final String valor) {
		try {
			if (valor != null && valor.matches(RelationalAndCIFValidator.FORMATO_NIF)) {
				final String letraValor = valor.substring(valor.length() - 1).toUpperCase();
				final String letraNIF = RelationalAndCIFValidator.letraNIF(valor.substring(0, valor.length() - 1));
				if (letraNIF != null) {
					return letraNIF.equals(letraValor);
				}
			}
			return false;
		}
		catch (final IndexOutOfBoundsException outExc) {
			return false;
		}
	}

	/**
	 * Method for semanthic validation of range-values fieldviews, when both of values are != null
	 * 
	 * @param dataValues
	 */
	public static final boolean relationalDateValidation(final Serializable minorValue_, final Serializable mayorValue_) {
		if (minorValue_ == null || mayorValue_ == null){
			return false;
		}
		Date minorValue= null, mayorValue=null;
		if (minorValue_ instanceof java.lang.String){
			try {
				minorValue= CommonUtils.myDateFormatter.parse(minorValue_);
				mayorValue= CommonUtils.myDateFormatter.parse(mayorValue_);
				if (minorValue_ == null || mayorValue_ == null){
					return false;
				}else{
					return minorValue.compareTo(mayorValue) <= 0;
				}
			} catch (ParseException e) {
				throw new RuntimeException("Parsing date field");				
			}
		}
		return false;		
	}

	public static final boolean relationalNumberValidation(final Serializable minorValue, final Serializable mayorValue) {
		try {
			return new BigDecimal(CommonUtils.numberFormatter.parse(minorValue.toString()).doubleValue()).compareTo(new BigDecimal(
					CommonUtils.numberFormatter.parse(mayorValue.toString()).doubleValue())) <= 0;
		}
		catch (final ParseException e) {
			return false;
		}
	}

	/**
	 * Validacion del NIE (Numero Identificacion Extranjeria) o tarjeta de residente. La validacion
	 * es la misma que la de un NIF ,con una X al principio
	 * 
	 * @param valor
	 *            String que se validaro
	 * @return True si es un NIE correcto , false en caso contrario.
	 */
	public static final boolean esNIE(final String valor) {
		try {
			return RelationalAndCIFValidator.esNIF(valor.substring(1)) ? valor.matches(RelationalAndCIFValidator.FORMATO_NIE) : false;
		}
		catch (final IndexOutOfBoundsException outExc) {
			return false;
		}
	}

	/**
	 * Validacion de un CIF (Codigo de Identificacion Fiscal)
	 * 
	 * @param valor
	 *            String que se validaro como CIF
	 * @return True si el valor es un CIF valido , false en caso contrario
	 */
	public static final boolean esCIF(final String valor) {
		if (valor != null && valor.matches(RelationalAndCIFValidator.FORMATO_CIF)) {
			final int tam = valor.length();
			final String digitosValor = valor.substring(1, tam - 1);
			final String sufijoValor = valor.substring(tam - 1);
			try {
				final int digitosValori = Integer.parseInt(digitosValor);
				if (digitosValori > 0) {
					int sumaPares = 0, sumaImpares = 0;
					int digitosValorCount = digitosValor.length();
					for (int i = 1; i <= digitosValorCount; i++) {
						final int digito = Integer.parseInt(digitosValor.substring(i - 1, i));
						if ((i % 2) == 0) {
							sumaPares += digito;
						} else {
							final StringBuilder imparX2 = new StringBuilder(IViewComponent.ZERO).append(String.valueOf(digito * 2));
							final int imparX2long = imparX2.length();
							sumaImpares += Integer.parseInt(imparX2.substring(imparX2long - 2, imparX2long - 1))
									+ Integer.parseInt(imparX2.substring(imparX2long - 1, imparX2long));
						}
					}
					final int indiceSufijo = 10 - ((sumaImpares + sumaPares) % 10);
					final String letraSufijo = RelationalAndCIFValidator.SUFIJO_LETRA_CIF.substring(indiceSufijo, indiceSufijo + 1);
					final String numSufijo = RelationalAndCIFValidator.SUFIJO_NUM_CIF.substring(indiceSufijo, indiceSufijo + 1);
					return (letraSufijo.equals(sufijoValor) || numSufijo.equals(sufijoValor));
				}
			}
			catch (final NumberFormatException ne) {
				return false;
			}
			catch (final IndexOutOfBoundsException outExc) {
				return false;
			}
		}
		return false;
	}
}
