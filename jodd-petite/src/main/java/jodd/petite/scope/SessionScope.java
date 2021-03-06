// Copyright (c) 2003-present, Jodd Team (jodd.org). All Rights Reserved.

package jodd.petite.scope;

import jodd.petite.BeanData;
import jodd.petite.BeanDefinition;
import jodd.petite.PetiteException;
import jodd.servlet.RequestContextListener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Session scope stores unique object instances per single http session.
 * Upon creation, new session listener is registered (dynamically) that will
 * keep track on active sessions. {@link RequestContextListener} is used for accessing
 * the request and the session.
 */
public class SessionScope extends ShutdownAwareScope {

	// ---------------------------------------------------------------- destory

	protected static final String SESSION_BEANS_NAME = SessionScope.class.getName() + ".SESSION_BEANS.";

	/**
	 * Registers new session destroy callback if not already registered.
	 */
	protected Map<String, BeanData> registerSessionBeans(HttpSession httpSession) {
	    SessionBeans sessionBeans = new SessionBeans();
		httpSession.setAttribute(SESSION_BEANS_NAME, sessionBeans);
		return sessionBeans.getBeanMap();
	}

	/**
	 * Returns instance map from http session.
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, BeanData> getSessionMap(HttpSession session) {
		SessionBeans sessionBeans = (SessionBeans) session.getAttribute(SESSION_BEANS_NAME);
		if (sessionBeans == null) {
			return null;
		}
		return sessionBeans.getBeanMap();
	}


	/**
	 * Session beans holder and manager.
	 */
	public class SessionBeans implements HttpSessionBindingListener, Serializable {

		protected Map<String, BeanData> beanMap = new HashMap<String, BeanData>();

		/**
		 * Returns bean map used in this session.
		 */
		public Map<String, BeanData> getBeanMap() {
			return beanMap;
		}

		public void valueBound(HttpSessionBindingEvent event) {
			// do nothing
		}

		/**
		 * Session is destroyed.
		 */
		public void valueUnbound(HttpSessionBindingEvent event) {
			for (BeanData beanData : beanMap.values()) {
				destroyBean(beanData);
			}
		}
	}

	// ---------------------------------------------------------------- scope

	/**
	 * Shutdowns the Session scope. Calls destroyable methods on
	 * all destroyable beans available in this moment.
	 */
	@Override
	public void shutdown() {
		super.shutdown();
	}

	public Object lookup(String name) {
		HttpSession session = getCurrentHttpSession();
		Map<String, BeanData> map = getSessionMap(session);
		if (map == null) {
			return null;
		}

		BeanData beanData = map.get(name);
		if (beanData == null) {
			return null;
		}
		return beanData.getBean();
	}

	public void register(BeanDefinition beanDefinition, Object bean) {
		HttpSession session = getCurrentHttpSession();
		Map<String, BeanData> map = getSessionMap(session);
		if (map == null) {
			map = registerSessionBeans(session);
		}

		BeanData beanData = new BeanData(beanDefinition, bean);
		map.put(beanDefinition.getName(), beanData);

		registerDestroyableBeans(beanData);
	}

	public void remove(String name) {
		if (totalRegisteredDestroyableBeans() == 0) {
			return;
		}
		HttpSession session = getCurrentHttpSession();
		Map<String, BeanData> map = getSessionMap(session);
		if (map != null) {
			map.remove(name);
		}
	}

	public boolean accept(Scope referenceScope) {
		Class<? extends Scope> refScopeType = referenceScope.getClass();

		if (refScopeType == SingletonScope.class) {
			return true;
		}

		if (refScopeType == SessionScope.class) {
			return true;
		}

		return false;
	}

	// ---------------------------------------------------------------- util

	/**
	 * Returns request from current thread.
	 */
	protected HttpSession getCurrentHttpSession() {
		HttpServletRequest request = RequestContextListener.getRequest();
		if (request == null) {
			throw new PetiteException("No HTTP request bound to the current thread. Is RequestContextListener registered?");
		}
		return request.getSession();
	}

}