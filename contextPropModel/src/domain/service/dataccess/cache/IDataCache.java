package domain.service.dataccess.cache;

import java.util.List;

import domain.service.component.definitions.FieldViewSet;


/**
 * <h1>IDataCache</h1> The IDataCache interface for DataCache.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public interface IDataCache {

	public void addAllItems(List<FieldViewSet> recordsToCache);

	public List<FieldViewSet> getAllItems(FieldViewSet fieldViewSet);

	public void addItem(FieldViewSet recordToCache);

	public FieldViewSet getItem(FieldViewSet entityToCache);

	public void removeItem(FieldViewSet entityToCache);

}
