package mx.com.gunix.framework.ui.vaadin.spring;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.vaadin.spring.navigator.ViewProviderAccessDelegate;
import org.vaadin.spring.navigator.internal.VaadinViewScope;
import org.vaadin.spring.navigator.internal.ViewCache;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.ui.UI;

/**
 * Basada en {@link org.vaadin.spring.navigator.SpringViewProvider}
 */
public class SpringViewProvider implements ViewProvider {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringViewProvider.class);
	private final Map<String, Set<String>> viewNameToBeanNamesMap = new ConcurrentHashMap<String, Set<String>>();
	private final ApplicationContext applicationContext;
	private final BeanDefinitionRegistry beanDefinitionRegistry;

	private Class<? extends View> accessDeniedViewClass;

	@Autowired
	public SpringViewProvider(ApplicationContext applicationContext, BeanDefinitionRegistry beanDefinitionRegistry) {
		this.applicationContext = applicationContext;
		this.beanDefinitionRegistry = beanDefinitionRegistry;
	}

	public Class<? extends View> getAccessDeniedViewClass() {
		return accessDeniedViewClass;
	}

	public void setAccessDeniedViewClass(Class<? extends View> accessDeniedViewClass) {
		this.accessDeniedViewClass = accessDeniedViewClass;
	}

	@PostConstruct
	void init() {
		LOGGER.info("Looking up VaadinViews");
		int count = 0;
		final String[] viewBeanNames = applicationContext.getBeanNamesForAnnotation(GunixVaadinView.class);
		for (String beanName : viewBeanNames) {
			final Class<?> type = applicationContext.getType(beanName);
			if (View.class.isAssignableFrom(type)) {
				final GunixVaadinView annotation = applicationContext.findAnnotationOnBean(beanName, GunixVaadinView.class);
				final String viewName = annotation.tipo().equals(GunixVaadinView.INDEX) ? "" : type.getName();

				LOGGER.debug("Found VaadinView bean [{}] with view name [{}]", beanName, viewName);
				if (applicationContext.isSingleton(beanName)) {
					throw new IllegalStateException("VaadinView bean [" + beanName + "] must not be a singleton");
				}
				Set<String> beanNames = viewNameToBeanNamesMap.get(viewName);
				if (beanNames == null) {
					beanNames = new ConcurrentSkipListSet<String>();
					viewNameToBeanNamesMap.put(viewName, beanNames);
				}
				beanNames.add(beanName);
				count++;
			}
		}
		if (count == 0) {
			LOGGER.warn("No VaadinViews found");
		} else if (count == 1) {
			LOGGER.info("1 VaadinView found");
		} else {
			LOGGER.info("{} VaadinViews found", count);
		}
	}

	@Override
	public View getView(String viewName) {
		try {
			View view = getTargetObject(doGetView(viewName), View.class);
			return view;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getViewName(String viewAndParameters) {
		LOGGER.trace("Extracting view name from [{}]", viewAndParameters);
		String viewName = null;
		if (isViewNameValidForCurrentUI(viewAndParameters)) {
			viewName = viewAndParameters;
		} else {
			int lastSlash = -1;
			String viewPart = viewAndParameters;
			while ((lastSlash = viewPart.lastIndexOf('/')) > -1) {
				viewPart = viewPart.substring(0, lastSlash);
				LOGGER.trace("Checking if [{}] is a valid view", viewPart);
				if (isViewNameValidForCurrentUI(viewPart)) {
					viewName = viewPart;
					break;
				}
			}
		}
		if (viewName == null) {
			LOGGER.trace("Found no view name in [{}]", viewAndParameters);
		} else {
			LOGGER.trace("[{}] is a valid view", viewName);
		}
		return viewName;
	}

	private boolean isViewNameValidForCurrentUI(String viewName) {
		final Set<String> beanNames = viewNameToBeanNamesMap.get(viewName);
		if (beanNames != null) {
			for (String beanName : beanNames) {
				if (isViewBeanNameValidForCurrentUI(beanName)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isViewBeanNameValidForCurrentUI(String beanName) {
		try {
			final Class<?> type = applicationContext.getType(beanName);

			Assert.isAssignable(View.class, type, "bean did not implement View interface");

			final UI currentUI = UI.getCurrent();
			final GunixVaadinView annotation = applicationContext.findAnnotationOnBean(beanName, GunixVaadinView.class);
			final String viewName = annotation.tipo().equals(GunixVaadinView.INDEX) ? "" : applicationContext.getType(beanName).getName();

			Assert.notNull(annotation, "class did not have a VaadinView annotation");

			if (annotation.ui().length == 0) {
				LOGGER.trace("View class [{}] with view name [{}] is available for all UI subclasses", type.getCanonicalName(), viewName);
				return true;
			} else {
				for (Class<? extends UI> validUI : annotation.ui()) {
					if (validUI == currentUI.getClass()) {
						LOGGER.trace("View class [%s] with view name [{}] is available for UI subclass [{}]", type.getCanonicalName(), viewName, validUI.getCanonicalName());
						return true;
					}
				}
			}
			return false;
		} catch (NoSuchBeanDefinitionException ex) {
			return false;
		}
	}

	private View doGetView(String viewName) {
		final Set<String> beanNames = viewNameToBeanNamesMap.get(viewName);
		if (beanNames != null) {
			for (String beanName : beanNames) {
				if (isViewBeanNameValidForCurrentUI(beanName)) {
					return getViewFromApplicationContext(viewName, beanName);
				}
			}
		}
		LOGGER.warn("Found no view with name [{}]", viewName);
		return null;
	}

	private View getViewFromApplicationContext(String viewName, String beanName) {
		View view = null;
		if (isAccessGrantedToBeanName(beanName)) {
			final BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanName);
			if (beanDefinition.getScope().equals(VaadinViewScope.VAADIN_VIEW_SCOPE_NAME)) {
				LOGGER.trace("View [{}] is view scoped, activating scope", viewName);
				final ViewCache viewCache = VaadinViewScope.getViewCacheRetrievalStrategy().getViewCache(applicationContext);
				viewCache.creatingView(viewName);
				try {
					view = getViewFromApplicationContextAndCheckAccess(beanName);
				} finally {
					viewCache.viewCreated(viewName, view);
				}
			} else {
				view = getViewFromApplicationContextAndCheckAccess(beanName);
			}
		}
		if (view != null) {
			return view;
		} else {
			return getAccessDeniedView();
		}
	}

	private View getViewFromApplicationContextAndCheckAccess(String beanName) {
		final View view = (View) applicationContext.getBean(beanName);
		if (isAccessGrantedToViewInstance(beanName, view)) {
			return view;
		} else {
			return null;
		}
	}

	private View getAccessDeniedView() {
		if (accessDeniedViewClass != null) {
			return applicationContext.getBean(accessDeniedViewClass);
		} else {
			return null;
		}
	}

	private boolean isAccessGrantedToBeanName(String beanName) {
		final UI currentUI = UI.getCurrent();
		final Map<String, ViewProviderAccessDelegate> accessDelegates = applicationContext.getBeansOfType(ViewProviderAccessDelegate.class);
		for (ViewProviderAccessDelegate accessDelegate : accessDelegates.values()) {
			if (!accessDelegate.isAccessGranted(beanName, currentUI)) {
				LOGGER.debug("Access delegate [{}] denied access to view with bean name [{}]", accessDelegate, beanName);
				return false;
			}
		}
		return true;
	}

	private boolean isAccessGrantedToViewInstance(String beanName, View view) {
		final UI currentUI = UI.getCurrent();
		final Map<String, ViewProviderAccessDelegate> accessDelegates = applicationContext.getBeansOfType(ViewProviderAccessDelegate.class);
		for (ViewProviderAccessDelegate accessDelegate : accessDelegates.values()) {
			if (!accessDelegate.isAccessGranted(beanName, currentUI, view)) {
				LOGGER.debug("Access delegate [{}] denied access to view [{}]", accessDelegate, view);
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getTargetObject(Object proxy, Class<T> targetClass) throws Exception {
		if (AopUtils.isJdkDynamicProxy(proxy)) {
			return (T) ((Advised) proxy).getTargetSource().getTarget();
		} else {
			return (T) proxy; // expected to be cglib proxy then, which is
								// simply a specialized class
		}
	}
}
