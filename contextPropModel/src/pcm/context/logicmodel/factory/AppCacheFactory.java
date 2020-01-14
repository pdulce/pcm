/**
 * 
 */
package pcm.context.logicmodel.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 99GU3997
 */
public class AppCacheFactory {

	private final Map<String, Map<String, String>> appProperties;

	private static AppCacheFactory appCacheFactory_ = new AppCacheFactory();

	private AppCacheFactory() {
		this.appProperties = new HashMap<String, Map<String, String>>();
	}

	public static AppCacheFactory getFactoryInstance() {
		if (AppCacheFactory.appCacheFactory_ == null) {
			AppCacheFactory.appCacheFactory_ = new AppCacheFactory();
		}
		return AppCacheFactory.appCacheFactory_;
	}

	public Map<String, String> getAppCache(final String app) {
		if (this.appProperties.get(app) == null) {
			this.appProperties.put(app, new HashMap<String, String>());
		}
		return this.appProperties.get(app);
	}

	public boolean isInitiated(final String app) {
		return (this.appProperties.get(app) != null);
	}

	public void initAppCache(final String app, final Map<String, String> appProp) {
		this.appProperties.put(app, appProp);
	}

}
