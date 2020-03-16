package domain.dataccess.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import domain.component.definitions.FieldViewSet;
import domain.component.definitions.IFieldView;


/**
 * <h1>DataCache</h1> The DataCache class maintains in memory the map with all of the collections of
 * entity data used intensively in some of views.
 * <p>
 * 
 * @author Pedro Dulce
 * @version 1.0
 * @since 2014-03-31
 */

public class DataCache implements Serializable, IDataCache {

	private static final long serialVersionUID = 699900343000999L;

	private Map<String, Collection<FieldViewSet>> cache = new Hashtable<String, Collection<FieldViewSet>>();

	@Override
	public List<FieldViewSet> getAllItems(final FieldViewSet fieldViewSet) {
		if (this.cache == null) {
			this.cache = new Hashtable<String, Collection<FieldViewSet>>();
			return new ArrayList<FieldViewSet>(210);
		}
		final Collection<FieldViewSet> collection = this.cache.get(fieldViewSet.getEntityDef().getName());
		if (collection == null || fieldViewSet.getContextName().equals(fieldViewSet.getEntityDef().getName())) {
			return new ArrayList<FieldViewSet>(/* collection */);
		}
		final List<FieldViewSet> resultados = new ArrayList<FieldViewSet>(210);
		final Iterator<FieldViewSet> cacheTablaIte = collection.iterator();
		while (cacheTablaIte.hasNext()) {
			final FieldViewSet inCache = cacheTablaIte.next();
			final FieldViewSet resultado = fieldViewSet.copyOf();
			final Iterator<IFieldView> fieldIte = resultado.getFieldViews().iterator();
			while (fieldIte.hasNext()) {
				final IFieldView fieldView = fieldIte.next();
				resultado.setValues(fieldView.getQualifiedContextName(), inCache.getFieldvalue(fieldView.getEntityField()).getValues());
			}
			resultados.add(resultado);
		}
		return resultados;
	}

	@Override
	public void addAllItems(final List<FieldViewSet> recordsToCache) {
		if (this.cache == null) {
			this.cache = new Hashtable<String, Collection<FieldViewSet>>();
		}
		if (!recordsToCache.isEmpty()) {
			this.cache.put(recordsToCache.iterator().next().getEntityDef().getName(), recordsToCache);
		}
	}

	@Override
	public FieldViewSet getItem(final FieldViewSet itemABuscar) {
		List<FieldViewSet> items = getAllItems(itemABuscar);
		return (items.isEmpty() ? null : items.get(0));
	}

	@Override
	public void addItem(final FieldViewSet recordToCache) {
		if (this.cache == null) {
			this.cache = new Hashtable<String, Collection<FieldViewSet>>();
		}
		this.cache.get(recordToCache.getEntityDef().getName()).add(recordToCache);
	}

	@Override
	public void removeItem(final FieldViewSet recordToCache) {
		if (this.cache == null) {
			this.cache = new Hashtable<String, Collection<FieldViewSet>>();
		}
		final Collection<FieldViewSet> cacheTabla = this.cache.get(recordToCache.getEntityDef().getName());
		final Iterator<FieldViewSet> fieldViewSetIte = cacheTabla.iterator();
		while (fieldViewSetIte.hasNext()) {
			final FieldViewSet fieldViewSet = fieldViewSetIte.next();
			if (fieldViewSet.isEqualsPk(recordToCache)) {
				cacheTabla.remove(fieldViewSet);
				break;
			}
		}
	}

}
