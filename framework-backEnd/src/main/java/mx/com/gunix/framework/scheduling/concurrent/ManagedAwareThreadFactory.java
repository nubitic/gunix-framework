package mx.com.gunix.framework.scheduling.concurrent;

import javax.enterprise.concurrent.ManagedThreadFactory;

import org.springframework.scheduling.concurrent.DefaultManagedAwareThreadFactory;

public class ManagedAwareThreadFactory extends DefaultManagedAwareThreadFactory implements ManagedThreadFactory {
	private static final long serialVersionUID = 1L;

}
