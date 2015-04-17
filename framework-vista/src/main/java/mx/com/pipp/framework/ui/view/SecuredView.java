package mx.com.pipp.framework.ui.view;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.vote.AuthenticatedVoter;

import com.vaadin.navigator.View;

@Secured(AuthenticatedVoter.IS_AUTHENTICATED_FULLY)
public interface SecuredView extends View {

}
