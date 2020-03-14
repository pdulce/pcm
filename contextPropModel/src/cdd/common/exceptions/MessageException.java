package cdd.common.exceptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import cdd.common.PCMConstants;
import cdd.domain.component.Translator;
import cdd.domain.component.html.IViewComponent;
import cdd.domain.component.html.XmlUtils;
import cdd.domain.service.event.IAction;
import cdd.domain.service.event.Parameter;


public class MessageException {

	public static final int INFO = 0, ERROR = 1, AVISO = 2;

	private static final String REGEXP_ARG_ = "\\$", BINDING_MSG_LITERAL = " Binding Entity ", RECORD_MSG_LITERAL = " Record ",
			GRAL_MSG_LITERAL = " General: ";

	/** Ctes. de literal errors (en inglos): son genoricos e internos de la api PCM **/

	private int nivelError;
	private final String code;
	private boolean appRule;	
	private Collection<Parameter> params = new ArrayList<Parameter>();

	public Collection<Parameter> getParams() {
		return this.params;
	}	

	public MessageException(final String cod_) {
		this.code = cod_;
		this.appRule = false;
		this.nivelError = ERROR;
	}

	public MessageException(final String cod_, boolean userRule_) {
		this.code = cod_;
		this.appRule = userRule_;
		this.nivelError = ERROR;
	}

	public MessageException(final String cod_, boolean userRule_, int nivelError_) {
		this.code = cod_;
		this.appRule = userRule_;
		this.nivelError = nivelError_;
	}

	public boolean isNotNull() {
		return !this.params.isEmpty();
	}

	public boolean isAppRule() {
		return this.appRule;
	}

	public int getNivelError() {
		return this.nivelError;
	}

	public void setAppRule(boolean b) {
		this.appRule = b;
	}

	public void addParameter(final Parameter param) {
		if (this.params == null) {
			this.params = new ArrayList<Parameter>();
		}
		this.params.add(param);
	}

	public final String getCode() {
		return this.code;
	}

	/**
	 * Para colores y hexadecimal function HexDecConv(){ this.hexvalue=""; this.decvalue=0;
	 * this.hexadecimales = "0123456789ABCDEF"; }
	 * HexDecConv.prototype.hexToDec=function(__hex){ return parseInt(__hex,16); }
	 * HexDecConv.prototype.decToHex=function(__dec){ _low = __dec%16; _high=(__dec-_low)/16; return
	 * ""
	 * + this.hexadecimales.charAt(high) + this.hexadecimales.charAt(low); } var hexDecConv = new
	 * HexDecConv();
	 * 
	 * @param lang
	 * @return
	 */
	public String toXML(final String lang) {
		StringBuilder msg = new StringBuilder();
		XmlUtils.openXmlNode(msg, IViewComponent.DIV_LAYER);
		switch (this.getNivelError()) {
			case INFO:
				XmlUtils.openXmlNode(msg, IViewComponent.LABEL_INFO);
				break;
			case AVISO:
				XmlUtils.openXmlNode(msg, IViewComponent.LABEL_WARNING);
				break;
			default:// error
				XmlUtils.openXmlNode(msg, IViewComponent.LABEL_ERROR);
		}
		if (this.isAppRule()) {
			// saco todos los parometros, los necesito para formar el mensaje
			final Object[] messageArguments = new Object[this.params.size()];
			int i = 0;
			final Iterator<Parameter> paramsIte = this.params.iterator();
			while (paramsIte.hasNext()) {
				messageArguments[i++] = "'".concat(paramsIte.next().getValue()).concat("'");
			}
			msg.append(Translator.traduceRuleAppDefined(lang, this.getCode(), messageArguments));
		} else {
			/*** TRADUCCION DEL CoDIGO DEL MENSAJE ***/
			msg.append(PCMConstants.CHAR_BEGIN_CORCH).append(Translator.traducePCMDefined(lang, this.getCode()))
					.append(PCMConstants.CHAR_END_CORCH).append(PCMConstants.STRING_SPACE);
			final Iterator<Parameter> paramsIte = this.params.iterator();
			while (paramsIte.hasNext()) {
				final Parameter p = paramsIte.next();
				if (this.esNumerico(p.getKey())) {
					final StringBuilder strb = new StringBuilder();
					XmlUtils.openXmlNode(strb, IViewComponent.STRONG_HTML);
					XmlUtils.openXmlNode(strb, IViewComponent.I_HTML);
					strb.append((Translator.traduceDictionaryModelDefined(lang, p.getValue())));
					XmlUtils.closeXmlNode(strb, IViewComponent.I_HTML);
					XmlUtils.closeXmlNode(strb, IViewComponent.STRONG_HTML);
					final String toReplace = new StringBuilder(MessageException.REGEXP_ARG_).append(p.getKey()).toString();
					msg = new StringBuilder(msg.toString().replaceAll(toReplace, strb.toString()+""));
				} else {
					msg.append(Translator.traducePCMDefined(lang, p.getValue()));
					if (p.getKey().equals(IAction.BINDING_CONCRETE_MSG) || p.getKey().equals(IAction.ERROR_SEMANTHIC_CODE)
							|| p.getKey().equals(IAction.CONFIG_PARAM)) {
						// tratamos la sustitucion de los parometros $0, $1,...
						continue;
					}
					if (paramsIte.hasNext()) {
						msg.append(PCMConstants.COMMA).append(PCMConstants.STRING_SPACE);
					}
					if (p.getKey().equals(PCMConstants.ENTIYY_PARAM)) {
						msg.append(MessageException.BINDING_MSG_LITERAL);
					} else if (p.getKey().equals(IAction.INSTANCE_PARAM)) {
						msg.append(MessageException.RECORD_MSG_LITERAL);
					} else {
						msg.append(MessageException.GRAL_MSG_LITERAL);
					}
				}
			}
		}
		XmlUtils.closeXmlNode(msg, IViewComponent.LABEL_HTML);
		XmlUtils.closeXmlNode(msg, IViewComponent.DIV_LAYER);
		return msg.toString();
	}

	private boolean esNumerico(final String paramKey) {
		if (paramKey == null || PCMConstants.EMPTY_.equals(paramKey.trim())) {
			return false;
		}
		int lengthOfP = paramKey.length();
		for (int i = 0; i < lengthOfP; i++) {
			if (!Character.isDigit(paramKey.charAt(i))) {
				return false;
			}
		}
		return true;
	}

}
