package cdd.domain.entitymodel;

import java.util.Collection;

import org.w3c.dom.Element;

/**
 * <h1>DataAccess</h1> The ILogicDataMetamodel interface for LogicDataMetamodel class.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public interface ILogicDataMetamodel {

	public String getName();

	public Collection<Element> getAllEntities();

}
