/**
 * 
 */
package pcm.comunication.dispatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import pcm.common.InternalErrorsConstants;
import pcm.common.PCMConstants;
import pcm.comunication.actions.Parameter;
import pcm.context.viewmodel.IViewMetamodel;

import com.oreilly.servlet.MultipartRequest;

/**
 * <h1>RequestWrapper</h1> The RequestWrapper class is used for encapsuling various HTTPRequest
 * types, such multipart/format-data and text-plain/format-data.
 * This class follows an approach based on the facade pattern of the HttpRequest class.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class RequestWrapper {

	private static final String MULTIPART_DATA = "multipart/form-data";

	private final HttpServletRequest httpReq;

	private MultipartRequest multiReq;

	private int pageSize;

	public RequestWrapper(final HttpServletRequest req, final String uploadDir, int page_) {
		this.httpReq = req;
		this.pageSize = page_;
		final String contentType = this.httpReq.getContentType() == null ? PCMConstants.EMPTY_ : this.httpReq.getContentType();
		if (contentType.startsWith(RequestWrapper.MULTIPART_DATA)) {
			try {
				this.multiReq = new MultipartRequest(this.httpReq, uploadDir, 9999999);
			}
			catch (final IOException ioE) {
				BasePCMServlet.log.log(Level.SEVERE, InternalErrorsConstants.TMP_ACCESS_EXCEPTION, ioE);
			}
			catch (final Throwable e) {
				BasePCMServlet.log.log(Level.SEVERE, InternalErrorsConstants.TMP_ACCESS_EXCEPTION, e);
			}
		}
	}
	
	public HttpServletRequest getOriginalHttpRequest(){
		return httpReq;
	}
	
	public Collection<String> getParametersNames() {
		return getParametersNames("");
	}

	@SuppressWarnings("rawtypes")
	public Collection<String> getParametersNames(String preffix) {
		Enumeration params = null;
		Collection<String> arr = new ArrayList<String>();
		params = (this.multiReq != null) ? this.multiReq.getParameterNames() : this.httpReq.getParameterNames();
		while (params.hasMoreElements()) {
			String paramName = (String) params.nextElement();
			if (preffix.equals("") || paramName.startsWith(preffix)) {
				arr.add(paramName);
			}
		}
		return arr;
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public final String getOriginalEvent() {
		final String ev = this.getParameter(IViewMetamodel.EVENT_ATTR);
		return ev != null ? ev : PCMConstants.EMPTY_;
	}

	public final String getDictionaryName() {
		return (String) this.httpReq.getAttribute(PCMConstants.APP_DICTIONARY);
	}

	public String getParameter(final String nameParam) {
		String[] parameters = getParameterValues(nameParam);
		if (parameters != null && parameters.length > 0) {
			return parameters[0];
		}
		return null;
	}

	public String[] getParameterValues(final String nameParam) {
		List<String> listaValores = new ArrayList<String>();
		String[] values_ = (this.multiReq != null) ? this.multiReq.getParameterValues(nameParam) : this.httpReq.getParameterValues(nameParam);
		for (int i=0;values_ != null && i<values_.length;i++){
			if (!listaValores.contains(values_[i])){
				listaValores.add(values_[i]);
			}
		}		
		if (listaValores.isEmpty()){
			return null;
		}else{
			String[] retorno = new String[listaValores.size()];
			for (int i=0;i<listaValores.size();i++){
				retorno[i] = listaValores.get(i);
			}
			return retorno;
		}
	}

	@SuppressWarnings("unchecked")
	public String getLanguageParameter() {
		if (this.multiReq != null) {
			final Enumeration<Object> pNames = this.multiReq.getParameterNames();
			while (pNames.hasMoreElements()) {
				final String pName = pNames.nextElement().toString();
				if (pName.endsWith(PCMConstants.LANGUAGE)) {
					return this.multiReq.getParameter(pName);
				}
			}
		} else {
			final Enumeration<String> pNames = this.httpReq.getParameterNames();
			while (pNames.hasMoreElements()) {
				final String pName = pNames.nextElement();
				if (pName.endsWith(PCMConstants.LANGUAGE)) {
					return this.httpReq.getParameter(pName);
				}
			}
		}
		return null;
	}

	public List<Parameter> getListParameter(final String nameParam) {
		final List<Parameter> arr = new ArrayList<Parameter>();
		String[] values = (this.multiReq != null) ? this.multiReq.getParameterValues(nameParam) : this.httpReq
				.getParameterValues(nameParam);
		if (values == null) {
			return arr;
		}
		for (final String value : values) {
			final Parameter p = new Parameter(nameParam, value);
			arr.add(p);
		}
		return arr;
	}

	public HttpSession getSession() {
		return this.httpReq.getSession();
	}

	public File getFile(final String reqParamN) {
		return this.multiReq != null ? this.multiReq.getFile(reqParamN) : null;
	}

	public Object getAttribute(final String reqAttrN) {
		return this.httpReq.getAttribute(reqAttrN);
	}

	public void setAttribute(final String reqAttrN, final Object val) {
		this.httpReq.setAttribute(reqAttrN, val);
	}

	public String getServletPath() {
		return this.httpReq.getServletPath();
	}

}
