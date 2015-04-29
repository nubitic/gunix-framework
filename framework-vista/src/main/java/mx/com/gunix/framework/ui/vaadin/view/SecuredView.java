package mx.com.gunix.framework.ui.vaadin.view;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AuthenticatedVoter;

import com.vaadin.navigator.View;

@Secured(AuthenticatedVoter.IS_AUTHENTICATED_FULLY)
public interface SecuredView extends View, mx.com.gunix.framework.security.Secured {

}
