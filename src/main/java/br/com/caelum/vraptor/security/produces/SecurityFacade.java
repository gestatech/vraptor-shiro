package br.com.caelum.vraptor.security.produces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton 
public class SecurityFacade {

	@Inject private Realm realm;
	@Inject @Any private Instance<AuthenticationListener> authenticationListeners;
	@Inject @Any private Instance<SessionListener> sessionListeners;
	
	private static final Logger log = LoggerFactory.getLogger(SecurityFacade.class);
	
	@PostConstruct
	public void init() {
		log.info("Initializing Shiro SecurityManager");

		//TODO: Tornar a criptografia dos passwords opcional
		((AuthorizingRealm)realm).setCredentialsMatcher(new PasswordMatcher());
		
		ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
		authenticator.setAuthenticationListeners(toCollection(authenticationListeners));
		authenticator.setRealms(Arrays.asList(realm));
		
		DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
		sessionManager.setSessionListeners(toCollection(sessionListeners));

		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(Arrays.asList(realm));
		securityManager.setAuthenticator(authenticator);
		securityManager.setSessionManager(sessionManager);
		
		SecurityUtils.setSecurityManager(securityManager);
	}
	
	private <E> Collection<E> toCollection(Iterable<E> iterable) {
		Collection<E> list = new ArrayList<E>();
		for (E item : iterable) {
	    	list.add(item);
		} 
		return list;
	}

	@Produces
	public SecurityManager getSecurityManager() {
		return SecurityUtils.getSecurityManager();
	}

	@Produces
	public Subject getSubject() {
		return SecurityUtils.getSubject();
	}

	@Produces
	public Session getSession() {
		return SecurityUtils.getSubject().getSession();
	}

	@Produces
	public PasswordService getPasswordService() {
		return new DefaultPasswordService();
	}
}
