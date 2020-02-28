package cdd.comunication.dispatcher;

import javax.servlet.http.HttpServletResponse;

import cdd.common.exceptions.PCMConfigurationException;


/**
 * <h1>IRenderInterface</h1> The IRenderInterface interface is used for defining the rendering
 * method for response-data.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public interface IRenderInterface {

	public void renderHTMLToResponse(HttpServletResponse response, String xmldoc) throws PCMConfigurationException;

}
