/**
 * 
 */
package cdd.logicmodel.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 99GU3997
 */
public class AppCacheFactory {

	private final Map<String, String> appProperties;

	private static AppCacheFactory appCacheFactory_ = new AppCacheFactory();

	private AppCacheFactory() {
		this.appProperties = new HashMap<String, String>();
	}

	public static AppCacheFactory getFactoryInstance() {
		if (AppCacheFactory.appCacheFactory_ == null) {
			AppCacheFactory.appCacheFactory_ = new AppCacheFactory();
		}
		return AppCacheFactory.appCacheFactory_;
	}

	public Map<String, String> getAppCache() {
		return this.appProperties;
	}

	public boolean isInitiated() {
		return !this.appProperties.isEmpty();
	}

	public void initAppCache(final Map<String, String> appProp) {
		this.appProperties.putAll(appProp);
	}

}
