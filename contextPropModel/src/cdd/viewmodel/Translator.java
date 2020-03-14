package cdd.viewmodel;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import cdd.common.InternalErrorsConstants;
import cdd.common.PCMConstants;
import cdd.viewmodel.components.IViewComponent;


public class Translator {

	private static final String MESSAGE_APP_BUNDLE = "messagesApp/MessageBundle";

	private static final String MESSAGE_PCM_BUNDLE = "cdd/messagesPCM/MessageBundle";

	private static Translator languageTraductor;

	private final Map<String, ResourceBundle> hResourceBundle;

	private final Map<String, MessageFormat> hFormatter;

	private final Map<String, Locale> hLocales;
	
	protected static Logger log = Logger.getLogger(Translator.class.getName());
	
	static {
		if (log.getHandlers().length == 0) {
			try {
				StreamHandler strdout = new StreamHandler(System.out, new SimpleFormatter());
				log.addHandler(strdout);
				log.setLevel(Level.INFO);
				log.log(Level.INFO, "Logger activado");
			}
			catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}


	private Translator() {
		this.hResourceBundle = new HashMap<String, ResourceBundle>();
		this.hFormatter = new HashMap<String, MessageFormat>();
		this.hLocales = new HashMap<String, Locale>();
	}

	private static Translator getUniqueInstance() {
		if (Translator.languageTraductor == null) {
			Translator.languageTraductor = new Translator();
		}
		return Translator.languageTraductor;
	}

	private ResourceBundle getResourceBundle(final String lang_, final String name) {
		String lang = lang_.replace("_", "");
		if (this.hLocales.get(lang) == null) {
			final Locale locale = new Locale(lang.toUpperCase());
			this.hLocales.put(lang, locale);
		}
		final StringBuilder nameLang = new StringBuilder(name);
		nameLang.append(PCMConstants.UNDERSCORE);
		nameLang.append(lang);
		if (this.hResourceBundle.get(nameLang.toString()) == null) {
			final ResourceBundle messages = ResourceBundle.getBundle(name, this.hLocales.get(lang));
			this.hResourceBundle.put(nameLang.toString(), messages);
		}
		return this.hResourceBundle.get(nameLang.toString());
	}

	private MessageFormat getMessageFormat(final String lang_) {
		String lang = lang_.replace("_", "");
		if (this.hFormatter.get(lang) == null) {
			Locale locale = this.hLocales.get(lang);
			if (locale == null) {
				locale = new Locale(lang.toUpperCase());
				this.hLocales.put(lang, locale);
			}
			final MessageFormat formatter = new MessageFormat(PCMConstants.EMPTY_);
			formatter.setLocale(locale);
			this.hFormatter.put(lang, formatter);
		}
		return this.hFormatter.get(lang);
	}

	public static String traduceDictionaryModelDefined(final String lang, final String txt) {
		return Translator.traduceDictionaryModelDefined(lang, txt, new Object[] {});
	}

	private static String traduceDictionaryModelDefined(final String lang_, final String txt, final Object[] messageArguments) {
		if (txt == null || txt.equals("")) {
			return "";
		}
		String lang = lang_.replace("_", "");
		final ResourceBundle messages = Translator.getUniqueInstance().getResourceBundle(lang, IViewComponent.LANG_DICT);
		final MessageFormat formatter = Translator.getUniqueInstance().getMessageFormat(lang);
		StringBuilder traduced = new StringBuilder();
		String newTxt = txt, suffix = "";
		try {
			formatter.applyPattern(messages.getString(newTxt.replaceAll(" ", ".")));
			traduced.append(formatter.format(messageArguments != null ? messageArguments : new Object[] {}));
		}
		catch (final MissingResourceException resExc) {
			try{
				formatter.applyPattern(messages.getString(newTxt.toUpperCase()));
				traduced.append(formatter.format(messageArguments != null ? messageArguments : new Object[] {}));
			}catch (final MissingResourceException resExc12) {
				
				String[] partsWithPoint = newTxt.split(PCMConstants.REGEXP_POINT);
				if (partsWithPoint.length == 2){
					try{
						formatter.applyPattern(messages.getString(partsWithPoint[1]));
						traduced.append(formatter.format(messageArguments != null ? messageArguments : new Object[] {}));
						return traduced.toString();						
					}catch (final MissingResourceException resExc133) {
						//continue
					}					
				}
				
				String[] parts = newTxt.split(" ");
				if (parts.length == 1) {
					if (newTxt.split(String.valueOf(PCMConstants.CHAR_POINT)).length == 2 && newTxt.length() == newTxt.trim().length()) {
						traduced = new StringBuilder(newTxt.substring(newTxt.indexOf(PCMConstants.CHAR_POINT) + 1));
					} else {
						traduced = new StringBuilder(newTxt);
					}
					if (traduced.toString().length() > 1) {
						return traduced.toString().substring(0, 1).toUpperCase().concat(traduced.toString().substring(1)).concat(suffix)
								.replaceAll(PCMConstants.UNDERSCORE, PCMConstants.STRING_SPACE);
					}
				} else {
					traduced = new StringBuilder();
					for (int i = 0; i < parts.length; i++) {
						try {
							formatter.applyPattern(messages.getString(parts[i]));
							String txtTraduced = formatter.format(messageArguments != null ? messageArguments : new Object[] {});
							traduced.append(txtTraduced);
							if ((i + 1) < parts.length) {
								traduced.append(" ");
							}
						}
						catch (final MissingResourceException resExc2) {
							traduced.append(parts[i]);
							if ((i + 1) < parts.length) {
								traduced.append(" ");
							}
						}
					}
				}
			}
			return traduced.toString();
		}
		catch (final StringIndexOutOfBoundsException exc1) {
			Translator.log.log(Level.SEVERE,
					InternalErrorsConstants.TRAD_LABEL_ERROR.replaceFirst(InternalErrorsConstants.ARG_1, newTxt == null ? "" : newTxt),
					exc1);
			traduced = new StringBuilder(newTxt);
		}
		catch (final Throwable exc) {
			Translator.log
					.log(Level.SEVERE, InternalErrorsConstants.TRAD_LABEL_ERROR.replaceFirst(InternalErrorsConstants.ARG_1,
							newTxt == null ? "" : newTxt), exc);
			traduced = new StringBuilder(newTxt);
		}
		String cadena_sin_limpiar = traduced.toString();
		return cadena_sin_limpiar;
	}

	public static String traduceRuleAppDefined(final String lang, final String txt) {
		return Translator.traduceRuleAppDefined(lang, txt, new Object[] {});
	}

	public static String traduceRuleAppDefined(final String lang_, final String txt, final Object[] messageArguments) {
		if (txt == null || txt.equals("")) {
			return "";
		}
		String lang = lang_.replace("_", "");
		final ResourceBundle messages = Translator.getUniqueInstance().getResourceBundle(lang, Translator.MESSAGE_APP_BUNDLE);
		final MessageFormat formatter = Translator.getUniqueInstance().getMessageFormat(lang);
		String traduced = PCMConstants.EMPTY_;
		try {
			formatter.applyPattern(messages.getString(txt));
			traduced = formatter.format(messageArguments);
		}
		catch (final Throwable exc) {
			//BasePCMServlet.log.log(Level.SEVERE,
			//	InternalErrorsConstants.TRAD_LABEL_ERROR.replaceFirst(InternalErrorsConstants.ARG_1, txt == null ? "" : txt), exc);
			traduced = txt;
		}
		return traduced;
	}

	public static String traducePCMDefined(final String lang, final String txt) {
		return Translator.traducePCMDefined(lang, txt, new Object[] {});
	}

	private static String traducePCMDefined(final String lang_, final String txt, final Object[] messageArguments) {
		if (txt == null || txt.equals("")) {
			return "";
		}
		String lang = lang_.replace("_", "");
		final ResourceBundle messages = Translator.getUniqueInstance().getResourceBundle(lang, Translator.MESSAGE_PCM_BUNDLE);
		final MessageFormat formatter = Translator.getUniqueInstance().getMessageFormat(lang);
		String traduced = PCMConstants.EMPTY_;
		try {
			formatter.applyPattern(messages.getString(txt));
			traduced = formatter.format(messageArguments);
		}
		catch (final MissingResourceException resExc) {
			traduced = txt;
		}
		catch (final Throwable exc) {
			Translator.log.log(Level.SEVERE,
					InternalErrorsConstants.TRAD_LABEL_ERROR.replaceFirst(InternalErrorsConstants.ARG_1, txt == null ? "" : txt), exc);
			traduced = txt;
		}
		return traduced;
	}

}
